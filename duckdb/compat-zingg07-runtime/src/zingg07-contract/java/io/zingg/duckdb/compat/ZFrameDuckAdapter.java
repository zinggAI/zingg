package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.Expression;
import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.Row;
import io.zingg.duckdb.api.Column;
import io.zingg.duckdb.api.ColumnResolver;
import io.zingg.duckdb.engine.DuckExpr;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import zingg.common.client.FieldData;
import zingg.common.client.ZFrame;

/**
 * Isolated bridge from the pinned Zingg v0.7 ZFrame contract to the DuckDB
 * relational API. Compile this class only with the zingg07-contract profile;
 * neither Zingg nor Scala types enter engine-api or engine-duckdb.
 */
public final class ZFrameDuckAdapter implements InvocationHandler {
  private static final String ID_PREFIX = "z_";
  private static final String SCORE = "z_score";
  private static final String MIN_SCORE = "z_minScore";
  private static final String MAX_SCORE = "z_maxScore";
  private static final Set<String> HANDLED_METHODS = Set.of(
      "cache", "columns", "select", "selectExpr", "distinct", "collectAsList", "collectFirstColumn",
      "toDF", "as", "join", "joinOnCol", "joinRight", "col", "count", "filter", "df", "withColumnRenamed",
      "dropDuplicates", "drop", "except", "intersect", "aggSum", "groupByMinMaxScore", "groupByCount",
      "union", "unionAll", "unionByName", "withColumn", "withColumns", "repartition", "coalesce", "sample",
      "gt", "equalTo", "notEqual", "concat", "not", "isNotNull", "and", "or", "show", "showSchema",
      "orderBy", "sortAscending", "sortDescending", "limit", "getAsString", "getAsDouble", "getAsInt",
      "getAsLong", "head", "getOnlyObjectFromRow", "isEmpty", "split", "explode", "fieldNames", "fieldIndex",
      "fields", "getMaxVal", "filterInCond", "filterNotNullCond", "filterNullCond", "countDistinct", "substr",
      "get", "getCols");

  private final Frame delegate;
  private final Object relationToken = new Object();
  private List<String> logicalNames;

  private List<String> columns() {
    return logicalNames == null ? delegate.columns() : logicalNames;
  }
  private List<Row> collect() {
    List<Row> rows=delegate.collect();
    return logicalNames==null?rows:rows.stream().map(row->new Row(logicalNames,row.values())).toList();
  }
  private String physicalName(String name) {
    if(logicalNames==null)return name;
    int index=logicalNames.indexOf(name);
    if(index<0)return name;
    if(logicalNames.lastIndexOf(name)!=index)
      throw new IllegalArgumentException("ambiguous column name after toDF: "+name);
    return delegate.columns().get(index);
  }
  private DuckZColumn column(String name) {
    String physical=physicalName(name);
    DuckZColumn value=DuckZColumn.column(relationToken,physical);
    return physical.equals(name)?value:value.as(name);
  }

  private ZFrameDuckAdapter(Frame delegate) {
    this.delegate = Objects.requireNonNull(delegate, "frame is required");
  }

  @SuppressWarnings("unchecked")
  public static ZFrame<Frame, Row, DuckZColumn> wrap(Frame frame) {
    return (ZFrame<Frame, Row, DuckZColumn>) Proxy.newProxyInstance(
        ZFrame.class.getClassLoader(), new Class<?>[] {ZFrame.class}, new ZFrameDuckAdapter(frame));
  }

  public static Frame unwrap(ZFrame<?, ?, ?> frame) {
    if (frame == null || !Proxy.isProxyClass(frame.getClass())) {
      throw new IllegalArgumentException("ZFrame was not created by the DuckDB adapter");
    }
    InvocationHandler handler = Proxy.getInvocationHandler(frame);
    if (!(handler instanceof ZFrameDuckAdapter adapter)) {
      throw new IllegalArgumentException("ZFrame was not created by the DuckDB adapter");
    }
    return adapter.delegate;
  }

  /** Returns a disposition for each exact reflected overload for the contract audit. */
  public static String disposition(Method method) {
    if (method.getDeclaringClass() == Object.class) return "OBJECT_METHOD";
    if (!HANDLED_METHODS.contains(method.getName())) return "UNSUPPORTED: no adapter mapping is defined";
    return "IMPLEMENTED: DuckDB relational adapter; differential semantics still require approval";
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    Object[] a = args == null ? new Object[0] : args;
    String name = method.getName();
    if (method.getDeclaringClass() == Object.class) {
      return switch (name) {
        case "toString" -> "ZFrameDuckAdapter[" + delegate.explain() + "]";
        case "hashCode" -> System.identityHashCode(proxy);
        case "equals" -> proxy == a[0];
        default -> throw unsupported(method);
      };
    }
    return switch (name) {
      case "as" -> alias((String) a[0]);
      case "cache" -> wrap(delegate.cache());
      case "columns", "fieldNames" -> columns().toArray(String[]::new);
      case "select" -> select(method, a);
      case "selectExpr" -> wrap(delegate.selectExpr((String[]) a[0]));
      case "distinct" -> wrap(delegate.distinct());
      case "collectAsList" -> collect();
      case "collectFirstColumn" -> firstColumn();
      case "toDF" -> renameColumns(method, a);
      case "join" -> join(method, a);
      case "joinOnCol" -> joinOnColumn(a);
      case "joinRight" -> joinRight(a);
      case "col" -> column((String) a[0]);
      case "count" -> delegate.count();
      case "filter" -> wrap(delegate.filter(expression(a[0])));
      case "df" -> delegate;
      case "withColumnRenamed" -> renameColumn((String) a[0], (String) a[1]);
      case "dropDuplicates" -> dropDuplicates(method, a);
      case "drop" -> drop(method, a);
      case "except" -> wrap(delegate.except(unwrap(zframe(a[0]))));
      case "intersect" -> wrap(delegate.intersect(unwrap(zframe(a[0]))));
      case "aggSum" -> aggregateNumber("SUM", (String) a[0]).doubleValue();
      case "groupByMinMaxScore" -> groupByMinMax(expression(a[0]));
      case "groupByCount" -> groupByCount(a);
      case "union" -> wrap(delegate.union(unwrap(zframe(a[0])), false, false));
      case "unionAll" -> wrap(delegate.union(unwrap(zframe(a[0])), false, false));
      case "unionByName" -> wrap(delegate.union(unwrap(zframe(a[0])), true, (Boolean) a[1]));
      case "withColumn" -> withColumn((String) a[0], a[1]);
      case "withColumns" -> withColumns((String[]) a[0], (DuckZColumn[]) a[1]);
      case "repartition" -> repartition(method, a);
      case "coalesce" -> wrap(delegate.coalesce((Integer) a[0]));
      case "sample" -> {
        yield wrap(delegate.sample((Boolean) a[0], ((Number) a[1]).doubleValue()));
      }
      case "gt" -> greaterThan(method, a);
      case "equalTo" -> equalTo(method, a);
      case "notEqual" -> notEqual(method, a);
      case "concat" -> DuckZColumn.expression("CASE WHEN " + sql(a[0]) + " IS NULL OR " + sql(a[1])
          + " IS NULL THEN NULL ELSE concat(" + sql(a[0]) + "," + sql(a[1]) + ") END");
      case "not" -> DuckZColumn.expression("NOT (" + sql(a[0]) + ")");
      case "isNotNull" -> DuckZColumn.expression(sql(a[0]) + " IS NOT NULL");
      case "and" -> DuckZColumn.expression("(" + sql(a[0]) + ") AND (" + sql(a[1]) + ")");
      case "or" -> DuckZColumn.expression("(" + sql(a[0]) + ") OR (" + sql(a[1]) + ")");
      case "show" -> show(method, a);
      case "showSchema" -> showSchema();
      case "orderBy" -> wrap(delegate.sort(quote((String) a[0]) + " ASC NULLS FIRST"));
      case "sortAscending" -> wrap(delegate.sort(quote((String) a[0]) + " ASC NULLS FIRST"));
      case "sortDescending" -> wrap(delegate.sort(quote((String) a[0]) + " DESC NULLS LAST"));
      case "limit" -> wrap(delegate.limit((Integer) a[0]));
      case "getAsString" -> (String) row(a[0]).get((String) a[1]);
      case "getAsDouble" -> (Double) row(a[0]).get((String) a[1]);
      case "getAsInt" -> (Integer) row(a[0]).get((String) a[1]);
      case "getAsLong" -> (Long) row(a[0]).get((String) a[1]);
      case "head" -> head();
      case "getOnlyObjectFromRow" -> onlyObject(row(a[0]));
      case "isEmpty" -> delegate.count() == 0;
      case "split" -> split((String) a[0], (String) a[1], (String) a[2]);
      case "explode" -> explode((String) a[0], (String) a[1]);
      case "fieldIndex" -> fieldIndex((String) a[0]);
      case "fields" -> fields();
      case "getMaxVal" -> aggregate("MAX", (String) a[0]);
      case "filterInCond" -> filterInCond((String) a[0], zframe(a[1]), (String) a[2]);
      case "filterNotNullCond" -> wrap(delegate.filter(new DuckExpr(quote((String) a[0]) + " IS NOT NULL")));
      case "filterNullCond" -> wrap(delegate.filter(new DuckExpr(quote((String) a[0]) + " IS NULL")));
      case "countDistinct" -> countDistinct((String) a[0], (String) a[1], (String) a[2]);
      case "substr" -> {
        int position = (Integer) a[1];
        int length = (Integer) a[2];
        // Spark treats position zero as the first character; DuckDB's
        // substring is zero-offset here and returns one fewer character.
        yield DuckZColumn.expression("substring(" + sql(a[0]) + "," + (position == 0 ? 1 : position)
            + "," + length + ")");
      }
      case "get" -> row(a[0]).get((String) a[1]);
      case "getCols" -> getColumns();
      default -> throw unsupported(method);
    };
  }

  private Object select(Method method, Object[] args) {
    if (method.getParameterTypes()[0] == String[].class) {
      String[] columns = (String[]) args[0];
      if (columns == null || columns.length == 0) {
        throw new IllegalArgumentException("select requires at least one column");
      }
      String[] physical=Arrays.stream(columns).map(this::physicalName).toArray(String[]::new);
      Frame selected=delegate.select(physical);
      // DuckDB uniquifies repeated projection names with a physical suffix;
      // Spark preserves the requested names, including duplicates. Keep the
      // requested positional names at the adapter boundary for both cases.
      return withLogicalNames(selected,Arrays.asList(columns.clone()));
    }
    if (args[0] instanceof List<?> expressions) {
      if (expressions.isEmpty()) return wrap(delegate.selectZeroColumns());
      return wrap(delegate.selectExpr(expressions.stream().map(ZFrameDuckAdapter::projectionSql).toArray(String[]::new)));
    }
    DuckZColumn[] expressions = (DuckZColumn[]) args[0];
    if (expressions == null) {
      throw new IllegalArgumentException("select requires at least one expression");
    }
    if (expressions.length == 0) {
      return wrap(delegate.selectZeroColumns());
    }
    return wrap(delegate.selectExpr(Arrays.stream(expressions).map(DuckZColumn::projectionSql).toArray(String[]::new)));
  }

  private Object renameColumns(Method method, Object[] args) {
    String[] names = method.getParameterCount() == 1
        ? (String[]) args[0] : new String[] {(String) args[0], (String) args[1]};
    List<String> old = delegate.columns();
    if (names.length != old.size()) throw new IllegalArgumentException("toDF requires one name per column");
    if (Arrays.stream(names).anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("toDF column names cannot be null");
    }
    return withLogicalNames(Arrays.asList(names.clone()));
  }

  private ZFrame<Frame,Row,DuckZColumn> withLogicalNames(List<String> names) {
    return withLogicalNames(delegate,names);
  }

  private ZFrame<Frame,Row,DuckZColumn> withLogicalNames(Frame frame,List<String> names) {
    ZFrame<Frame,Row,DuckZColumn> renamed=wrap(frame);
    ((ZFrameDuckAdapter)Proxy.getInvocationHandler(renamed)).logicalNames=List.copyOf(names);
    return renamed;
  }

  private Object alias(String name) {
    if (name == null) throw new IllegalArgumentException("ZFrame alias is required");
    // Wrapping creates a fresh relation token. That token, rather than the
    // human-readable SQL alias, distinguishes both sides of adapter joins.
    return logicalNames == null ? wrap(delegate) : withLogicalNames(delegate, logicalNames);
  }

  private Object renameColumn(String from, String to) {
    List<String> columns = columns();
    if (from == null || to == null || from.equals(to) || !columns.contains(from))
      return logicalNames==null?wrap(delegate):withLogicalNames(delegate,columns);
    return withLogicalNames(columns.stream().map(column -> column.equals(from) ? to : column).toList());
  }

  private Object join(Method method, Object[] args) {
    ZFrameDuckAdapter rightAdapter = (ZFrameDuckAdapter) Proxy.getInvocationHandler(zframe(args[0]));
    Frame right = rightAdapter.delegate;
    if (args.length == 2) {
      String key = (String) args[1];
      return joined(delegate.join(right, qualified("l", key) + " = " + qualified("r", ID_PREFIX + key)),
          rightAdapter);
    }
    if (args.length == 3 && args[1] instanceof Expression condition) {
      return joined(delegate.join(right, condition.sql(), (String) args[2]), rightAdapter);
    }
    if (args.length == 3) {
      String first = (String) args[1], second = (String) args[2];
      return joined(delegate.join(right, equality(first, first) + " AND " + equality(second, second)),
          rightAdapter);
    }
    if (args.length == 4 && args[2] instanceof Boolean addPrefix) {
      String left = (String) args[1];
      String rightColumn = addPrefix ? ID_PREFIX + left : left;
      return joined(delegate.join(right, qualified("l", left) + " = " + qualified("r", rightColumn),
          (String) args[3]), rightAdapter);
    }
    String first = (String) args[1], second = (String) args[2], type = (String) args[3];
    return joined(delegate.join(right, equality(first, first) + " AND " + equality(second, second), type),
        rightAdapter);
  }

  private ZFrame<Frame,Row,DuckZColumn> joined(Frame frame, ZFrameDuckAdapter right) {
    var names = new ArrayList<String>(columns());
    names.addAll(right.columns());
    // DuckDB uniquifies duplicate SELECT * names; Spark retains both names for
    // condition joins. Restore the Spark-visible positional schema logically.
    return frame.columns().size() == names.size() ? withLogicalNames(frame, names) : wrap(frame);
  }

  private Object joinOnColumn(Object[] args) {
    Frame right = unwrap(zframe(args[0]));
    if (args[1] instanceof String key) return wrap(delegate.joinUsing(right, "inner", key));
    ZFrameDuckAdapter rightAdapter = (ZFrameDuckAdapter) Proxy.getInvocationHandler(zframe(args[0]));
    return joined(delegate.join(right, sql(args[1])), rightAdapter);
  }

  private Object joinRight(Object[] args) {
    String key = (String) args[1];
    ZFrameDuckAdapter right = (ZFrameDuckAdapter) Proxy.getInvocationHandler(zframe(args[0]));
    return joined(delegate.join(right.delegate, equality(key, key), "right"), right);
  }

  private Object dropDuplicates(Method method, Object[] args) {
    String[] columns;
    if (method.getParameterTypes()[0] == String[].class) columns = (String[]) args[0];
    else {
      String[] rest = (String[]) args[1];
      columns = new String[rest.length + 1];
      columns[0] = (String) args[0];
      System.arraycopy(rest, 0, columns, 1, rest.length);
    }
    // Spark's dropDuplicates(emptySubset) aggregates on a constant key and emits at
    // most one representative row; DuckFrame's empty-key implementation otherwise
    // means full-row distinct, which is a different operation.
    if (columns.length == 0) return wrap(delegate.limit(1));
    return wrap(delegate.dropDuplicates(columns));
  }

  private Object drop(Method method, Object[] args) {
    if (method.getParameterTypes()[0] == String[].class) return wrap(delegate.drop((String[]) args[0]));
    if (args[0] instanceof String column) return wrap(delegate.drop(column));
    return wrap(delegate.drop(args[0] instanceof DuckZColumn col ? col.directColumnName() : directColumn(sql(args[0]))));
  }

  private Object withColumn(String name, Object value) {
    return wrap(delegate.withColumn(name, value instanceof Expression ? expression(value) : value));
  }

  private Object withColumns(String[] names, DuckZColumn[] values) {
    if (names == null || values == null || names.length != values.length) {
      throw new IllegalArgumentException("withColumns names and expressions must have equal length");
    }
    java.util.LinkedHashMap<String, DuckZColumn> replacements = new java.util.LinkedHashMap<>();
    for (int i = 0; i < names.length; i++) {
      if (names[i] == null || names[i].isBlank() || values[i] == null || replacements.putIfAbsent(names[i], values[i]) != null) {
        throw new IllegalArgumentException("withColumns requires unique names and non-null expressions");
      }
    }
    List<String> projections = new ArrayList<>();
    for (String column : delegate.columns()) {
      DuckZColumn replacement = replacements.remove(column);
      projections.add(replacement == null ? quote(column) + " AS " + quote(column)
          : replacement.sql() + " AS " + quote(column));
    }
    replacements.forEach((column, expression) -> projections.add(expression.sql() + " AS " + quote(column)));
    return wrap(delegate.selectExpr(projections.toArray(String[]::new)));
  }

  private Object repartition(Method method, Object[] args) {
    if (method.getParameterTypes()[0] == int.class) return wrap(delegate.repartition((Integer) args[0]));
    return wrap(delegate);
  }

  private Object greaterThan(Method method, Object[] args) {
    if (args.length == 1) {
      String column = (String) args[0];
      return DuckZColumn.expression(quote(column) + " > " + quote(ID_PREFIX + column));
    }
    if (args[0] instanceof ZFrame<?, ?, ?> other) {
      String column = (String) args[1];
      return DuckZColumn.expression(qualified("l", column) + " > " + qualified("r", ID_PREFIX + column));
    }
    if (args[0] instanceof String column) return DuckZColumn.expression(quote(column) + " > " + args[1]);
    return DuckZColumn.expression(binarySql(args[0], args[1], " > "));
  }

  private Object equalTo(Method method, Object[] args) {
    if (args[0] instanceof Expression left) return DuckZColumn.expression(binarySql(left, args[1], " = "));
    return DuckZColumn.expression(quote((String) args[0]) + " = " + literal(args[1]));
  }

  private Object notEqual(Method method, Object[] args) {
    if (args.length == 1) {
      String column = (String) args[0];
      return DuckZColumn.expression(quote(column) + " <> " + quote(ID_PREFIX + column));
    }
    return DuckZColumn.expression(quote((String) args[0]) + " <> " + literal(args[1]));
  }

  private Object groupByMinMax(Expression group) {
    return wrap(delegate.aggregate(new String[] {group.sql()},
        "MIN(" + quote(SCORE) + ") AS " + quote(MIN_SCORE),
        "MAX(" + quote(SCORE) + ") AS " + quote(MAX_SCORE)));
  }

  private Object groupByCount(Object[] args) {
    String first = (String) args[0];
    if (args.length == 2) return wrap(delegate.aggregate(new String[] {first},
        "COUNT(*) AS " + quote((String) args[1])));
    String second = (String) args[1];
    return wrap(delegate.aggregate(new String[] {first, second},
        "COUNT(" + quote(first) + ") AS " + quote((String) args[2])));
  }

  private Object filterInCond(String leftColumn, ZFrame<?, ?, ?> inner, String innerColumn) {
    return wrap(delegate.filterInCond(unwrap(inner), leftColumn, innerColumn));
  }

  private Object countDistinct(String group, String distinct, String output) {
    return wrap(delegate.aggregate(new String[] {group},
        "COUNT(DISTINCT " + quote(distinct) + ") AS " + quote(output)));
  }

  private Object split(String column, String pattern, String output) {
    return wrap(delegate.selectExpr("regexp_split_to_array(" + quote(column) + ", "
        + DuckExpr.literal(pattern).sql() + ") AS " + quote(output)));
  }

  private Object explode(String column, String output) {
    return wrap(delegate.selectExpr("UNNEST(" + quote(column) + ") AS " + quote(output)));
  }

  private Object aggregate(String function, String column) {
    List<Row> rows = delegate.aggregate(null, function + "(" + quote(column) + ") AS " + quote("value")).collect();
    return rows.isEmpty() ? null : rows.get(0).get("value");
  }

  private Number aggregateNumber(String function, String column) {
    Object value = aggregate(function, column);
    if (value == null) throw new DuckException(function + " result is NULL and cannot be returned as a primitive number");
    return (Number) value;
  }

  private Object[] getColumns() {
    return columns().stream().map(this::column).toArray(DuckZColumn[]::new);
  }

  private FieldData[] fields() {
    List<Column> schema=delegate.schema();
    return java.util.stream.IntStream.range(0,schema.size()).mapToObj(i->{var field=schema.get(i);return new FieldData(
        columns().get(i), sparkTypeName(field), field.nullable());}).toArray(FieldData[]::new);
  }

  private String showSchema() {
    List<Column> schema = delegate.schema();
    StringBuilder result = new StringBuilder("StructType(");
    for (int index = 0; index < schema.size(); index++) {
      if (index > 0) result.append(',');
      Column field = schema.get(index);
      result.append("StructField(").append(columns().get(index)).append(',')
          .append(sparkTypeName(field)).append(',').append(field.nullable()).append(')');
    }
    return result.append(')').toString();
  }

  private static String sparkTypeName(Column field) {
    String type = field.typeName().toUpperCase(Locale.ROOT).replace(" ", "");
    return switch (type) {
      case "BOOL", "BOOLEAN" -> "BooleanType";
      case "TINYINT", "INT1" -> "ByteType";
      case "SMALLINT", "INT2" -> "ShortType";
      case "INTEGER", "INT", "INT4" -> "IntegerType";
      case "BIGINT", "INT8" -> "LongType";
      case "UTINYINT" -> "ShortType";
      case "USMALLINT" -> "IntegerType";
      case "UINTEGER" -> "LongType";
      case "UBIGINT" -> "DecimalType(20,0)";
      case "FLOAT", "REAL", "FLOAT4" -> "FloatType";
      case "DOUBLE", "FLOAT8" -> "DoubleType";
      case "VARCHAR", "CHAR", "BPCHAR", "TEXT", "STRING" -> "StringType";
      case "BLOB", "BYTEA", "BINARY", "VARBINARY" -> "BinaryType";
      case "DATE" -> "DateType";
      case "TIMESTAMP", "TIMESTAMP_S", "TIMESTAMP_MS", "TIMESTAMP_NS", "TIMESTAMPTZ" -> "TimestampType";
      default -> {
        if (type.startsWith("DECIMAL(") || type.startsWith("NUMERIC(")) {
          yield "DecimalType" + type.substring(type.indexOf('('));
        }
        yield field.typeName();
      }
    };
  }

  private int fieldIndex(String name) {
    int index = ColumnResolver.lastIndexOf(columns(), name);
    if (index < 0) throw new IllegalArgumentException("field does not exist: " + name);
    return index;
  }

  private List<String> firstColumn() {
    List<Row> rows = collect();
    List<String> values = new ArrayList<>(rows.size());
    if (columns().isEmpty()) return values;
    String column = columns().get(0);
    for (Row row : rows) values.add((String) row.values().get(0));
    return values;
  }

  private Row head() {
    List<Row> rows = delegate.limit(1).collect();
    if (rows.isEmpty()) throw new java.util.NoSuchElementException("head of empty ZFrame");
    Row row=rows.get(0);
    return logicalNames==null?row:new Row(logicalNames,row.values());
  }

  private static Object onlyObject(Row row) {
    if (row.values().isEmpty()) throw new IllegalArgumentException("row has no fields");
    return row.values().get(0);
  }

  private Object show(Method method, Object[] args) {
    int requestedLimit = args.length == 0 || args[0] instanceof Boolean ? 20 : (Integer) args[0];
    int limit = Math.max(0, requestedLimit);
    boolean truncate = method.getParameterCount() == 2 ? (Boolean) args[1]
        : method.getParameterCount() == 1 && args[0] instanceof Boolean b ? b : true;
    List<String> columns = columns();
    int maxWidth = truncate ? 20 : 0;
    List<Row> fetched = delegate.limit((long) limit + 1).collect();
    boolean more = fetched.size() > limit;
    List<Row> rows = fetched.subList(0, Math.min(limit, fetched.size()));
    List<List<String>> values = new ArrayList<>(rows.size());
    for (Row row : rows) {
      List<String> displayed = new ArrayList<>(columns.size());
      for (int i = 0; i < columns.size(); i++) {
        Object value = row.values().get(i);
        String text = value == null ? "NULL" : escapeShowValue(value.toString());
        displayed.add(truncate(text, maxWidth));
      }
      values.add(displayed);
    }
    int[] widths = new int[columns.size()];
    for (int i = 0; i < widths.length; i++) {
      widths[i] = Math.max(3, truncate(escapeShowValue(columns.get(i)), maxWidth).length());
      for (List<String> row : values) widths[i] = Math.max(widths[i], row.get(i).length());
    }
    String border = tableBorder(widths);
    System.out.println(border);
    printTableRow(columns.stream().map(name -> truncate(escapeShowValue(name), maxWidth)).toList(),
        widths, truncate);
    System.out.println(border);
    for (List<String> row : values) printTableRow(row, widths, truncate);
    System.out.println(border);
    if (more) System.out.println("only showing top " + limit + (limit == 1 ? " row" : " rows"));
    return null;
  }

  private static String truncate(String value, int maxWidth) {
    if (maxWidth == 0 || value.length() <= maxWidth) return value;
    if (maxWidth < 4) return value.substring(0, maxWidth);
    return value.substring(0, maxWidth - 3) + "...";
  }

  private static String escapeShowValue(String value) {
    return value.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n")
        .replace("\r", "\\r").replace("\b", "\\b").replace("\f", "\\f");
  }

  private static String tableBorder(int[] widths) {
    StringBuilder border = new StringBuilder("+");
    if (widths.length == 0) return border.append('+').toString();
    for (int width : widths) border.append("-".repeat(width)).append('+');
    return border.toString();
  }

  private static void printTableRow(List<String> values, int[] widths, boolean rightAlign) {
    StringBuilder line = new StringBuilder("|");
    for (int i = 0; i < widths.length; i++) {
      String value = values.get(i);
      int padding = widths[i] - value.length();
      if (rightAlign) line.append(" ".repeat(padding));
      line.append(value);
      if (!rightAlign) line.append(" ".repeat(padding));
      line.append('|');
    }
    if (widths.length == 0) line.append('|');
    System.out.println(line);
  }

  private static Frame frame(ZFrame<?, ?, ?> value) { return unwrap(value); }
  @SuppressWarnings("unchecked")
  private static ZFrame<?, ?, ?> zframe(Object value) { return (ZFrame<?, ?, ?>) value; }
  private static Row row(Object value) { return (Row) value; }
  private static Expression expression(Object value) {
    if (value instanceof Expression expression) return expression;
    throw new UnsupportedOperationException("adapter expression is not a DuckDB expression");
  }
  private static String sql(Object value) {
    if (value instanceof Expression expression) return expression.sql();
    throw new UnsupportedOperationException("adapter expression is not a DuckDB expression");
  }
  private static String projectionSql(Object value) {
    return value instanceof DuckZColumn column ? column.projectionSql() : sql(value);
  }
  private static String binarySql(Object left, Object right, String operator) {
    if (left instanceof DuckZColumn l && right instanceof DuckZColumn r
        && l.relation() != null && r.relation() != null && l.relation() != r.relation()
        && l.alias() == null) {
      return l.qualified("l") + operator + r.qualified("r");
    }
    return sql(left) + operator + sql(right);
  }
  private static String literal(Object value) { return DuckExpr.literal(value).sql(); }
  private static String equality(String left, String right) { return qualified("l", left) + " = " + qualified("r", right); }
  private static String qualified(String alias, String column) { return alias + "." + quote(column); }
  private static String quote(String value) { return "\"" + value.replace("\"", "\"\"") + "\""; }
  private static String directColumn(String sql) {
    if (sql.matches("\"(?:\"\"|[^\"])+\"")) return sql.substring(1, sql.length() - 1).replace("\"\"", "\"");
    if (sql.matches("[A-Za-z_][A-Za-z0-9_]*")) return sql;
    throw new UnsupportedOperationException("dropping a computed expression is unsupported: " + sql);
  }
  private static UnsupportedOperationException unsupported(Method method) {
    return new UnsupportedOperationException("unsupported Zingg v0.7 ZFrame operation: " + method.toGenericString());
  }
}
