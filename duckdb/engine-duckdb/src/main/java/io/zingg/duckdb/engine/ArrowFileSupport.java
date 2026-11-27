package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.sql.Types;
import java.util.List;
import java.util.Locale;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.dictionary.Dictionary;

/** Arrow IPC file ingress converted into a worker-shared, job-private table. */
final class ArrowFileSupport {
  @FunctionalInterface
  interface BatchObserver {
    void afterBatch(long importedRows) throws Exception;
  }

  private ArrowFileSupport() {}

  private record ArrowColumn(Field field, FieldVector dictionaryValues) {}

  static DuckFrame read(DuckJob job, Path path) {
    return read(job, path, false);
  }

  static DuckFrame readWithAppender(DuckJob job, Path path) {
    return read(job, path, true);
  }

  private static DuckFrame read(DuckJob job, Path path, boolean useAppender) {
    return read(job, path, useAppender, importedRows -> {});
  }

  static DuckFrame read(DuckJob job, Path path, boolean useAppender, BatchObserver observer) {
    java.util.Objects.requireNonNull(observer, "observer");
    String table = "zd_arrow_" + java.util.UUID.randomUUID().toString().replace("-", "");
    boolean tableCreated = false;
    long memoryLimit = job.budget().memoryLimitBytes() > 0 ? job.budget().memoryLimitBytes() : Runtime.getRuntime().maxMemory();
    try (var allocator = new RootAllocator(Math.max(1L, memoryLimit));
         var reader = openReader(path, allocator)) {
      if (reader instanceof ArrowFileReader fileReader) fileReader.initialize();
      VectorSchemaRoot root = reader.getVectorSchemaRoot();
      List<ArrowColumn> fields = root.getSchema().getFields().stream().map(field -> {
        var encoding = field.getDictionary();
        if (encoding == null) return new ArrowColumn(field, null);
        Dictionary dictionary = reader.lookup(encoding.getId());
        if (dictionary == null) throw new DuckException("Arrow dictionary is missing: " + encoding.getId());
        Field valuesField = dictionary.getVector().getField();
        var valuesType = valuesField.getFieldType();
        var logicalType = new org.apache.arrow.vector.types.pojo.FieldType(
            field.isNullable() || valuesType.isNullable(), valuesField.getType(), null, valuesType.getMetadata());
        return new ArrowColumn(new Field(field.getName(), logicalType, valuesField.getChildren()), dictionary.getVector());
      }).toList();
      if (fields.isEmpty()) throw new DuckException("Arrow file has no columns: " + path);
      var names = new java.util.HashSet<String>();
      for (ArrowColumn column : fields) {
        String normalizedName = column.field().getName().toLowerCase(Locale.ROOT);
        if (!names.add(normalizedName))
          throw new DuckException("duplicate Arrow field name (case-insensitive): " + column.field().getName());
      }
      String columns = fields.stream().map(c -> DuckExpr.quote(c.field().getName()) + " " + sqlType(c.field())
          + (c.field().isNullable() ? "" : " NOT NULL")).collect(java.util.stream.Collectors.joining(", "));
      try (var statement = job.createStatement()) {
        statement.execute("CREATE TABLE " + DuckExpr.quote(job.schemaName()) + "." + DuckExpr.quote(table) + " (" + columns + ")");
      }
      tableCreated = true;
      job.registerMaterialized(table);
      long importedRows = 0;
      if (useAppender) {
        try (var appender = job.connection().createAppender(job.schemaName(), table)) {
          while (reader.loadNextBatch()) {
            job.checkCancelled();
            long batchEnd = importedRows + root.getRowCount();
            if (batchEnd < importedRows) throw new DuckException("Arrow row count overflow");
            job.budget().enforceRows(batchEnd);
            for (int row = 0; row < root.getRowCount(); row++) {
              if ((row & 1023) == 0) job.checkCancelled();
              appender.beginRow();
              for (int col = 0; col < fields.size(); col++) {
                FieldVector vector = root.getVector(col);
                Object value = columnValue(fields.get(col), vector, row);
                appendValue(appender, value);
              }
              appender.endRow();
            }
            importedRows = batchEnd;
            observer.afterBatch(importedRows);
            job.checkCancelled();
          }
        }
      } else {
        String placeholders = String.join(",", java.util.Collections.nCopies(fields.size(), "?"));
        try (var insert = job.prepareStatement("INSERT INTO " + DuckExpr.quote(job.schemaName()) + "." + DuckExpr.quote(table) + " VALUES (" + placeholders + ")")) {
          while (reader.loadNextBatch()) {
            job.checkCancelled();
            long batchEnd = importedRows + root.getRowCount();
            if (batchEnd < importedRows) throw new DuckException("Arrow row count overflow");
            job.budget().enforceRows(batchEnd);
            for (int row = 0; row < root.getRowCount(); row++) {
              if ((row & 1023) == 0) job.checkCancelled();
              for (int col = 0; col < fields.size(); col++) {
                FieldVector vector = root.getVector(col);
                Object value = columnValue(fields.get(col), vector, row);
                insert.setObject(col + 1, value);
              }
              insert.addBatch();
            }
            insert.executeBatch();
            job.checkCancelled();
            importedRows = batchEnd;
            observer.afterBatch(importedRows);
            job.checkCancelled();
          }
        }
      }
      return new DuckFrame(job, "SELECT * FROM " + DuckExpr.quote(job.schemaName()) + "." + DuckExpr.quote(table), fields.stream().map(c -> c.field().getName()).toList());
    } catch (Exception e) {
      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
      if (tableCreated) {
        try {
          job.dropMaterialized(table);
        } catch (Exception cleanup) {
          e.addSuppressed(cleanup);
        }
      }
      if (e instanceof DuckException de) throw de;
      throw new DuckException("Arrow IPC ingestion failed: " + path, e);
    }
  }

  private static Object columnValue(ArrowColumn column, FieldVector vector, int row) {
    if (vector.isNull(row)) return nullValue(column);
    if (column.dictionaryValues() == null) return jdbcValue(vector.getObject(row), column.field());
    Object rawIndex = vector.getObject(row);
    if (!(rawIndex instanceof Number indexValue)) throw new DuckException("Arrow dictionary index is not numeric");
    long index = indexValue.longValue();
    FieldVector dictionary = column.dictionaryValues();
    if (index < 0 || index >= dictionary.getValueCount()) throw new DuckException("Arrow dictionary index is out of range: " + index);
    Object decoded = dictionary.isNull((int) index) ? null : dictionary.getObject((int) index);
    return decoded == null ? nullValue(column) : jdbcValue(decoded, column.field());
  }

  private static Object nullValue(ArrowColumn column) {
    if (!column.field().isNullable())
      throw new DuckException("null value in non-nullable Arrow field: " + column.field().getName());
    return null;
  }

  private static void appendValue(org.duckdb.DuckDBAppender appender, Object value) throws java.sql.SQLException {
    if (value == null) appender.appendNull();
    else if (value instanceof Boolean v) appender.append(v);
    else if (value instanceof Byte v) appender.append(v);
    else if (value instanceof Short v) appender.append(v);
    else if (value instanceof Integer v) appender.append(v);
    else if (value instanceof Long v) appender.append(v);
    else if (value instanceof Float v) appender.append(v);
    else if (value instanceof Double v) appender.append(v);
    else if (value instanceof Character v) appender.append(v);
    else if (value instanceof String v) appender.append(v);
    else if (value instanceof byte[] v) appender.append(v);
    else if (value instanceof java.math.BigDecimal v) appender.append(v);
    else if (value instanceof java.math.BigInteger v) appender.append(v);
    else if (value instanceof java.sql.Date v) appender.append(v.toLocalDate());
    else if (value instanceof java.sql.Timestamp v) appender.append(v.toLocalDateTime());
    else if (value instanceof java.time.LocalDate v) appender.append(v);
    else if (value instanceof java.time.LocalDateTime v) appender.append(v);
    else if (value instanceof java.time.OffsetDateTime v) appender.append(v);
    else throw new DuckException("unsupported DuckDB appender value: " + value.getClass().getName());
  }

  private static ArrowReader openReader(Path path, RootAllocator allocator) throws Exception {
    byte[] magic = new byte[6];
    FileChannel channel = FileChannel.open(path, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
    boolean transferred = false;
    try {
      ByteBuffer prefix = ByteBuffer.wrap(magic);
      long position = 0;
      while (prefix.hasRemaining()) {
        int read = channel.read(prefix, position);
        if (read < 0) break;
        if (read == 0) continue;
        position += read;
      }
      if (prefix.position() == magic.length
          && java.util.Arrays.equals(magic, new byte[] {'A','R','R','O','W','1'})) {
        ArrowFileReader reader = new ArrowFileReader(channel, allocator);
        transferred = true;
        return reader;
      }
      channel.position(0);
      ArrowStreamReader reader = new ArrowStreamReader(Channels.newInputStream(channel), allocator);
      transferred = true;
      return reader;
    } finally {
      if (!transferred) channel.close();
    }
  }

  private static String sqlType(Field field) {
    String type = field.getType().getTypeID().name().toUpperCase(Locale.ROOT);
    return switch (type) {
      case "BOOL" -> "BOOLEAN";
      case "INT" -> {
        var integer = (org.apache.arrow.vector.types.pojo.ArrowType.Int) field.getType();
        int width = integer.getBitWidth();
        if (!integer.getIsSigned() && width == 64) yield "DECIMAL(20, 0)";
        if (!integer.getIsSigned()) yield "BIGINT";
        yield switch (width) {
          case 8 -> "TINYINT";
          case 16 -> "SMALLINT";
          case 32 -> "INTEGER";
          case 64 -> "BIGINT";
          default -> throw new DuckException("unsupported Arrow integer width: " + width);
        };
      }
      case "FLOATINGPOINT" -> {
        var floating = (org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint) field.getType();
        yield switch (floating.getPrecision()) {
          case HALF, SINGLE -> "FLOAT";
          case DOUBLE -> "DOUBLE";
        };
      }
      case "DECIMAL" -> {
        var decimal = (org.apache.arrow.vector.types.pojo.ArrowType.Decimal) field.getType();
        yield "DECIMAL(" + decimal.getPrecision() + ", " + decimal.getScale() + ")";
      }
      case "DATE" -> "DATE";
      case "TIMESTAMP" -> ((org.apache.arrow.vector.types.pojo.ArrowType.Timestamp) field.getType()).getTimezone() == null
          ? "TIMESTAMP" : "TIMESTAMPTZ";
      case "BINARY", "LARGEBINARY" -> "BLOB";
      case "UTF8", "STRING", "LARGE_STRING", "LARGEUTF8" -> "VARCHAR";
      default -> throw new DuckException("unsupported Arrow field type: " + field.getType());
    };
  }

  private static Object jdbcValue(Object value, Field field) {
    String type = field.getType().getTypeID().name().toUpperCase(Locale.ROOT);
    if ("FLOATINGPOINT".equals(type)
        && ((org.apache.arrow.vector.types.pojo.ArrowType.FloatingPoint) field.getType()).getPrecision()
            == org.apache.arrow.vector.types.FloatingPointPrecision.HALF
        && value instanceof Number bits) {
      return halfToFloat(bits.shortValue());
    }
    if ("INT".equals(type)) {
      var integer = (org.apache.arrow.vector.types.pojo.ArrowType.Int) field.getType();
      if (!integer.getIsSigned()) {
        int width = integer.getBitWidth();
        if (width == 64) {
          if (value instanceof java.math.BigInteger bigInteger) return new java.math.BigDecimal(bigInteger);
          if (value instanceof Number number && number.longValue() < 0)
            return new java.math.BigDecimal(new java.math.BigInteger(Long.toUnsignedString(number.longValue())));
        } else if (value instanceof Character character) {
          return (long) character.charValue();
        } else if (value instanceof Number number) {
          return switch (width) {
            case 8 -> (long) Byte.toUnsignedInt(number.byteValue());
            case 16 -> (long) Short.toUnsignedInt(number.shortValue());
            case 32 -> number instanceof Long ? number.longValue() : (long) Integer.toUnsignedLong(number.intValue());
            default -> value;
          };
        }
      }
    }
    if ("DATE".equals(type) && value instanceof java.time.LocalDate date)
      return java.sql.Date.valueOf(date);
    if ("DATE".equals(type) && value instanceof LocalDateTime dateTime)
      return java.sql.Date.valueOf(dateTime.toLocalDate());
    if ("DATE".equals(type) && value instanceof Number n)
      return java.sql.Date.valueOf(java.time.LocalDate.ofEpochDay(n.longValue()));
    if ("DATE".equals(type)) {
      String text = value.toString();
      try {
        return java.sql.Date.valueOf(java.time.LocalDate.parse(text));
      } catch (java.time.format.DateTimeParseException dateOnlyParseFailure) {
        return java.sql.Date.valueOf(LocalDateTime.parse(text).toLocalDate());
      }
    }
    if ("TIMESTAMP".equals(type)) {
      var timestamp = (org.apache.arrow.vector.types.pojo.ArrowType.Timestamp) field.getType();
      if (value instanceof Number n) {
        Instant instant = switch (timestamp.getUnit()) {
          case SECOND -> Instant.ofEpochSecond(n.longValue());
          case MILLISECOND -> Instant.ofEpochMilli(n.longValue());
          case MICROSECOND -> Instant.ofEpochSecond(Math.floorDiv(n.longValue(), 1_000_000L),
              Math.floorMod(n.longValue(), 1_000_000L) * 1_000L);
          case NANOSECOND -> Instant.ofEpochSecond(Math.floorDiv(n.longValue(), 1_000_000_000L),
              Math.floorMod(n.longValue(), 1_000_000_000L));
        };
        // DuckDB TIMESTAMP/TIMESTAMPTZ has microsecond precision.
        instant = instant.truncatedTo(ChronoUnit.MICROS);
        return timestamp.getTimezone() == null
            ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
            : OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
      }
      if (value instanceof LocalDateTime local)
        return timestamp.getTimezone() == null ? local.truncatedTo(ChronoUnit.MICROS)
            : local.truncatedTo(ChronoUnit.MICROS).atOffset(ZoneOffset.UTC);
      if (value instanceof OffsetDateTime offset)
        return timestamp.getTimezone() == null
            ? offset.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime().truncatedTo(ChronoUnit.MICROS)
            : offset.withOffsetSameInstant(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }
    if (value == null || value instanceof Number || value instanceof Boolean
        || value instanceof String || value instanceof byte[]
        || value instanceof java.sql.Date || value instanceof java.sql.Timestamp) return value;
    return value.toString();
  }

  /** Converts Arrow's raw IEEE-754 binary16 short payload into an exact binary32 value. */
  private static float halfToFloat(short half) {
    int bits = Short.toUnsignedInt(half);
    int sign = (bits & 0x8000) << 16;
    int exponent = (bits >>> 10) & 0x1f;
    int fraction = bits & 0x03ff;
    int floatBits;
    if (exponent == 0) {
      if (fraction == 0) return Float.intBitsToFloat(sign);
      int floatExponent = 113;
      while ((fraction & 0x0400) == 0) {
        fraction <<= 1;
        floatExponent--;
      }
      fraction &= 0x03ff;
      floatBits = sign | (floatExponent << 23) | (fraction << 13);
    } else if (exponent == 0x1f) {
      floatBits = sign | 0x7f800000 | (fraction << 13);
    } else {
      floatBits = sign | ((exponent + 112) << 23) | (fraction << 13);
    }
    return Float.intBitsToFloat(floatBits);
  }
}
