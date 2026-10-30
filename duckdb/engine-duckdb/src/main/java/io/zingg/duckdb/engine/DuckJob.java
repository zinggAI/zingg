package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import org.duckdb.DuckDBConnection;

public final class DuckJob implements JobHandle {
  private final DuckDBConnection connection;
  private final JobId id;
  private final String connectionId;
  private final String schemaName;
  private final Runnable release;
  private final ResourceBudget budget;
  private final DuckCancellation cancellation;
  private volatile java.nio.file.Path cachedTempDirectory;
  private final Set<String> materializedTables = Collections.synchronizedSet(new LinkedHashSet<>());
  private final Object materializationLifecycle = new Object();
  private final AtomicBoolean closed = new AtomicBoolean();

  public DuckJob(DuckDBConnection connection, JobId id) {
    this(connection, id, () -> {}, ResourceBudget.unlimited(), DuckCancellation.current());
  }

  public DuckJob(DuckDBConnection connection, JobId id, Runnable release) {
    this(connection, id, release, ResourceBudget.unlimited(), DuckCancellation.current());
  }

  public DuckJob(DuckDBConnection connection, JobId id, Runnable release, ResourceBudget budget) {
    this(connection, id, release, budget, DuckCancellation.current());
  }

  public DuckJob(DuckDBConnection connection, JobId id, Runnable release, ResourceBudget budget,
      DuckCancellation cancellation) {
    this.connection = connection;
    this.id = id;
    this.connectionId = "conn_" + UUID.randomUUID();
    this.schemaName = "_zingg_run_" + id.value().replace("-", "");
    this.release = release;
    this.budget = budget == null ? ResourceBudget.unlimited() : budget;
    this.cancellation = cancellation;
    try (var statement = createStatement()) {
      statement.execute("CREATE SCHEMA " + DuckExpr.quote(schemaName));
    } catch (SQLException e) {
      throw new DuckException("cannot create job schema", e);
    }
  }

  public JobId id() { return id; }
  public String connectionId() { return connectionId; }
  public String schemaName() { return schemaName; }
  public DuckDBConnection connection() { return connection; }

  public Statement createStatement() throws SQLException {
    Statement statement = connection.createStatement();
    Statement tracked = cancellation == null ? statement : cancellation.track(statement);
    return budget.maxSpillBytes() <= 0 ? tracked
        : SpillByteGuard.track(tracked, tempDirectory(), budget.maxSpillBytes());
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(sql);
    PreparedStatement tracked = cancellation == null ? statement : (PreparedStatement) cancellation.track(statement);
    return budget.maxSpillBytes() <= 0 ? tracked
        : (PreparedStatement) SpillByteGuard.track(tracked, tempDirectory(), budget.maxSpillBytes());
  }

  public void checkCancelled() {
    if (cancellation != null) cancellation.throwIfCancelled();
  }

  ResourceBudget budget() { return budget; }

  java.nio.file.Path tempDirectory() {
    java.nio.file.Path existing = cachedTempDirectory;
    if (existing != null) return existing;
    try (var statement = connection.createStatement();
         var result = statement.executeQuery("SELECT current_setting('temp_directory')")) {
      if (!result.next()) throw new SQLException("DuckDB did not return temp_directory");
      String configured = result.getString(1);
      String path = configured == null || configured.isBlank()
          ? System.getProperty("java.io.tmpdir") : configured;
      java.nio.file.Path resolved = java.nio.file.Path.of(path).toAbsolutePath().normalize();
      cachedTempDirectory = resolved;
      return resolved;
    } catch (SQLException | java.nio.file.InvalidPathException e) {
      throw new DuckException("cannot resolve DuckDB temp_directory for input snapshots", e);
    }
  }

  void registerMaterialized(String name) {
    synchronized (materializationLifecycle) {
      if (closed.get()) throw new DuckException("job is already closed");
      materializedTables.add(name);
    }
  }

  void dropMaterialized(String name) throws SQLException {
    // Cleanup must remain available after cancellation has disabled normal SQL.
    try (var statement = connection.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS " + DuckExpr.quote(schemaName) + "." + DuckExpr.quote(name));
    }
    synchronized (materializedTables) { materializedTables.remove(name); }
  }

  int materializedCount() { return materializedTables.size(); }

  public Frame createFrame(String table, List<String> columns, List<String> types,
      List<List<Object>> rows) {
    if (table == null || !table.matches("[A-Za-z_][A-Za-z0-9_]*") || columns == null
        || columns.isEmpty() || columns.stream().anyMatch(c -> c == null || c.isBlank())
        || types == null || types.size() != columns.size()
        || types.stream().anyMatch(t -> !Set.of("VARCHAR", "DOUBLE", "BIGINT", "BOOLEAN").contains(t))
        || rows == null) throw new DuckException("invalid job-local frame definition");
    String qualified = DuckExpr.quote(schemaName) + "." + DuckExpr.quote(table);
    String definitions = java.util.stream.IntStream.range(0, columns.size())
        .mapToObj(i -> DuckExpr.quote(columns.get(i)) + " " + types.get(i))
        .collect(java.util.stream.Collectors.joining(","));
    try (var statement = createStatement()) {
      statement.execute("CREATE TABLE " + qualified + " (" + definitions + ")");
      registerMaterialized(table);
    } catch (SQLException | RuntimeException e) {
      try (var cleanup = connection.createStatement()) {
        cleanup.execute("DROP TABLE IF EXISTS " + qualified);
      } catch (SQLException cleanupFailure) { e.addSuppressed(cleanupFailure); }
      if (e instanceof DuckException duck) throw duck;
      throw new DuckException("cannot create job-local frame", e);
    }
    String placeholders = String.join(",", Collections.nCopies(columns.size(), "?"));
    try (var statement = prepareStatement("INSERT INTO " + qualified + " VALUES (" + placeholders + ")")) {
      int pending = 0;
      for (List<Object> row : rows) {
        checkCancelled();
        if (row == null || row.size() != columns.size())
          throw new DuckException("job-local frame row shape mismatch");
        for (int i = 0; i < row.size(); i++) statement.setObject(i + 1, row.get(i));
        statement.addBatch();
        if (++pending == 1000) { statement.executeBatch(); pending = 0; }
      }
      if (pending > 0) statement.executeBatch();
      return table(table);
    } catch (SQLException | RuntimeException e) {
      try (var statement = connection.createStatement()) {
        statement.execute("DROP TABLE IF EXISTS " + qualified);
        materializedTables.remove(table);
      } catch (SQLException cleanup) { e.addSuppressed(cleanup); }
      if (e instanceof DuckException duck) throw duck;
      throw new DuckException("cannot populate job-local frame", e);
    }
  }

  public Frame table(String name) {
    return new DuckFrame(this, "SELECT * FROM " + DuckExpr.quote(schemaName) + "." + DuckExpr.quote(name),
        List.of(), RelationScope.WORKER_SHARED);
  }

  public Frame sql(String sql) {
    if (sql == null || sql.isBlank()) throw new DuckException("SQL is empty");
    return new DuckFrame(this, sql, List.of());
  }

  public void close() {
    if (!closed.compareAndSet(false, true)) return;
    DuckException failure = null;
    try {
      java.util.List<String> tables;
      synchronized (materializationLifecycle) {
        synchronized (materializedTables) { tables = new java.util.ArrayList<>(materializedTables); }
      }
      for (String name : tables)
        try (var statement = connection.createStatement()) {
          statement.execute("DROP TABLE IF EXISTS " + DuckExpr.quote(schemaName) + "." + DuckExpr.quote(name));
        }
      try (var statement = connection.createStatement()) {
        statement.execute("DROP SCHEMA IF EXISTS " + DuckExpr.quote(schemaName));
      }
    } catch (SQLException e) { failure = new DuckException("cannot close job", e); }
    finally {
      try { connection.close(); }
      catch (SQLException e) {
        if (failure == null) failure = new DuckException("cannot close job connection", e);
        else failure.addSuppressed(e);
      }
      try { release.run(); }
      finally { if (failure != null) throw failure; }
    }
  }
}
