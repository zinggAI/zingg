package io.zingg.duckdb.engine;

import java.sql.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read-only diagnostic executable for the pinned DuckDB JDBC runtime.
 *
 * This is deliberately a main class, not a test: it can be run in a clean
 * installation and emits a stable JSON report for CI and support bundles.
 */
public final class JdbcLifecycleProbe {
  private JdbcLifecycleProbe() {}

  public static void main(String[] args) throws Exception {
    String url = args.length == 0 ? "jdbc:duckdb:" : args[0];
    Map<String, Object> report = new LinkedHashMap<>();
    report.put("probe", "duckdb-jdbc-lifecycle");
    report.put("startedAt", Instant.now().toString());
    report.put("jdbcUrl", url);
    report.put("driver", probeDriver());
    try (Connection owner = DriverManager.getConnection(url)) {
      report.put("ownerConnection", probeOwnerConnection(owner));
      report.put("tempVisibility", probeTempVisibility(owner, url));
      report.put("runSchema", probeRunSchema(owner));
      report.put("duplicateConnection", probeDuplicateConnection(owner, url));
      report.put("udfCatalog", probeUdfCatalog(owner, url));
      report.put("arrowRegistration", probeArrowRegistration(owner));
      report.put("cancellation", probeCancellation(owner));
      report.put("timeout", probeTimeout(owner));
      report.put("cleanup", probeCleanup(owner));
    }
    report.put("finishedAt", Instant.now().toString());
    System.out.println(toJson(report));
  }

  private static Map<String, Object> probeDriver() throws SQLException {
    Driver driver = DriverManager.getDriver("jdbc:duckdb:");
    return map("name", driver.getClass().getName(), "version", driver.getMajorVersion() + "." + driver.getMinorVersion());
  }

  private static Map<String, Object> probeOwnerConnection(Connection c) throws SQLException {
    try (Statement s = c.createStatement(); ResultSet r = s.executeQuery("select current_schema(), current_database()")) {
      r.next();
      return map("ok", true, "schema", r.getString(1), "database", r.getString(2));
    }
  }

  private static Map<String, Object> probeTempVisibility(Connection owner, String url) throws SQLException {
    try (Statement s = owner.createStatement()) {
      s.execute("create temporary table probe_temp(value integer)");
      s.execute("insert into probe_temp values (7)");
    }
    boolean ownerVisible = count(owner, "select count(*) from probe_temp") == 1;
    boolean duplicateVisible;
    try (Connection duplicate = DriverManager.getConnection(url)) {
      duplicateVisible = canQuery(duplicate, "select count(*) from probe_temp");
    }
    return map("ownerVisible", ownerVisible, "duplicateVisible", duplicateVisible);
  }

  private static Map<String, Object> probeRunSchema(Connection c) throws SQLException {
    String schema = "probe_run_" + Long.toUnsignedString(System.nanoTime());
    try (Statement s = c.createStatement()) {
      s.execute("create schema \"" + schema + "\"");
      s.execute("create table \"" + schema + "\".payload(value integer)");
      s.execute("insert into \"" + schema + "\".payload values (11)");
      boolean visible = count(c, "select count(*) from \"" + schema + "\".payload") == 1;
      s.execute("drop schema \"" + schema + "\" cascade");
      return map("schema", schema, "visible", visible, "dropped", !canQuery(c, "select * from \"" + schema + "\".payload"));
    }
  }

  private static Map<String, Object> probeDuplicateConnection(Connection owner, String url) throws SQLException {
    try (Connection duplicate = DriverManager.getConnection(url)) {
      return map("distinctObjects", owner != duplicate, "ownerClosedAfterDuplicate", owner.isClosed(), "duplicateClosed", duplicate.isClosed());
    }
  }

  private static Map<String, Object> probeUdfCatalog(Connection c, String url) throws SQLException {
    String name = "probe_udf_" + Long.toUnsignedString(System.nanoTime());
    try (Statement s = c.createStatement()) {
      s.execute("create or replace macro \"" + name + "\"(x) as x + 1");
      boolean ownerCallable = count(c, "select \"" + name + "\"(41)") == 42;
      boolean duplicateCallable;
      try (Connection duplicate = DriverManager.getConnection(url)) {
        duplicateCallable = canQuery(duplicate, "select \"" + name + "\"(41)");
      }
      s.execute("drop macro \"" + name + "\"");
      return map("catalogAccessible", canQuery(c, "select function_name from duckdb_functions() limit 1"),
          "ownerCallable", ownerCallable, "duplicateCallable", duplicateCallable,
          "dropped", !canQuery(c, "select \"" + name + "\"(41)"));
    }
  }

  private static Map<String, Object> probeArrowRegistration(Connection c) throws SQLException {
    return map("jdbcArrowApiPresent", hasClass("org.duckdb.DuckDBResultSet"), "connectionArrowApiPresent", hasMethod(c, "registerArrow"));
  }

  private static Map<String, Object> probeCancellation(Connection c) {
    try (Statement s = c.createStatement()) {
      Thread worker = new Thread(() -> { try { s.execute("select sum(i) from range(1000000000) t(i)"); } catch (SQLException ignored) {} });
      worker.start(); Thread.sleep(25); s.cancel(); worker.join(2_000);
      return map("cancelInvoked", true, "workerStopped", !worker.isAlive());
    } catch (Exception e) { return map("cancelInvoked", false, "error", e.getClass().getName()); }
  }

  private static Map<String, Object> probeTimeout(Connection c) throws SQLException {
    try (Statement s = c.createStatement()) { s.setQueryTimeout(1); return map("accepted", true, "seconds", s.getQueryTimeout()); }
  }

  private static Map<String, Object> probeCleanup(Connection c) throws SQLException {
    try (Statement s = c.createStatement()) { s.execute("drop table if exists probe_temp"); }
    return map("connectionOpenBeforeClose", !c.isClosed());
  }

  private static boolean canQuery(Connection c, String sql) {
    try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
      return true;
    } catch (SQLException e) {
      return false;
    }
  }
  private static long count(Connection c, String sql) throws SQLException { try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) { r.next(); return r.getLong(1); } }
  private static boolean hasClass(String name) { try { Class.forName(name); return true; } catch (ClassNotFoundException e) { return false; } }
  private static boolean hasMethod(Object object, String name) { for (var m : object.getClass().getMethods()) if (m.getName().equals(name)) return true; return false; }
  private static Map<String, Object> map(Object... values) { Map<String, Object> out = new LinkedHashMap<>(); for (int i = 0; i < values.length; i += 2) out.put(String.valueOf(values[i]), values[i + 1]); return out; }
  private static String toJson(Object value) { if (value instanceof Map<?, ?> m) { StringBuilder b = new StringBuilder("{"); boolean first = true; for (var e : m.entrySet()) { if (!first) b.append(','); first = false; b.append(quote(String.valueOf(e.getKey()))).append(':').append(toJson(e.getValue())); } return b.append('}').toString(); } if (value instanceof Boolean || value instanceof Number) return value.toString(); return quote(String.valueOf(value)); }
  private static String quote(String value) { return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""; }
}
