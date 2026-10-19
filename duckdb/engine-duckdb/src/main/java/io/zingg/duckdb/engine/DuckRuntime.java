package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.JobHandle;
import io.zingg.duckdb.api.JobId;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.api.RuntimeDiagnostics;
import io.zingg.duckdb.api.RuntimeHandle;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.duckdb.DuckDBConnection;

public final class DuckRuntime implements RuntimeHandle {
  private static final Set<String> ACTIVE_RUN_SCHEMAS = ConcurrentHashMap.newKeySet();

  private final DuckDBConnection root;
  private final AtomicBoolean closed = new AtomicBoolean();
  private final Semaphore jobSlots;

  public DuckRuntime(String url) {
    this(new RuntimeConfig(url, Runtime.getRuntime().availableProcessors(), 0, null, 1));
  }

  public DuckRuntime(RuntimeConfig config) {
    jobSlots = new Semaphore(config.maxConcurrentJobs());
    DuckDBConnection connection = null;
    try {
      // Apply security-sensitive settings while the DuckDB instance is being
      // created, before any query can trigger extension auto-install/load.
      var properties = new Properties();
      properties.setProperty("threads", Integer.toString(config.threads()));
      if (config.memoryLimitBytes() > 0)
        properties.setProperty("memory_limit", config.memoryLimitBytes() + "B");
      if (config.tempDirectory() != null)
        properties.setProperty("temp_directory", config.tempDirectory().toAbsolutePath().normalize().toString());
      if (config.maxTempDirectoryBytes() > 0)
        properties.setProperty("max_temp_directory_size", config.maxTempDirectoryBytes() + "B");
      properties.setProperty("autoinstall_known_extensions", "false");
      properties.setProperty("autoload_known_extensions", "false");
      properties.setProperty("allow_community_extensions", "false");
      properties.setProperty("enable_external_access", Boolean.toString(!config.offlineMode()));
      if (config.offlineMode()) {
        // Offline operation still supports local data files. DuckDB requires
        // explicit allowed_directories when network/external access is off.
        String filesystemRoot = java.nio.file.Path.of("").toAbsolutePath().getRoot().toString()
            .replace('\\', '/').replace("'", "''");
        properties.setProperty("allowed_directories", "['" + filesystemRoot + "']");
      }
      connection = (DuckDBConnection) DriverManager.getConnection(config.databaseUrl(), properties);
      try (var statement = connection.createStatement()) {
        try (var settings = statement.executeQuery("SELECT current_setting('autoinstall_known_extensions'), "
            + "current_setting('autoload_known_extensions'), current_setting('allow_community_extensions'), "
            + "current_setting('enable_external_access')")) {
          if (!settings.next()) throw new SQLException("DuckDB did not return extension security settings");
          if (settings.getBoolean(1) || settings.getBoolean(2) || settings.getBoolean(3)
              || settings.getBoolean(4) != !config.offlineMode())
            throw new DuckException("DuckDB connection URL overrides the configured connector security policy");
        }
        if (config.offlineMode() && !config.allowedExtensions().isEmpty())
          throw new DuckException("offline mode cannot enable extensions");
        if (!config.offlineMode()) {
          for (String extension : config.connectorPolicy().allowedExtensions().stream().sorted().toList()) {
            config.connectorPolicy().requireAllowed(extension);
            statement.execute("INSTALL " + extension);
            statement.execute("LOAD " + extension);
          }
        }
        try (var result = statement.executeQuery("SELECT current_setting('lock_configuration')")) {
          if (!result.next()) throw new SQLException("DuckDB did not return lock_configuration");
          if (!result.getBoolean(1)) statement.execute("SET lock_configuration=true");
        }
      }
      cleanupOrphanRunSchemas(connection);
      root = connection;
    } catch (SQLException | RuntimeException e) {
      if (connection != null) {
        try {
          connection.close();
        } catch (SQLException closeFailure) {
          e.addSuppressed(closeFailure);
        }
      }
      if (e instanceof RuntimeException runtimeFailure) throw runtimeFailure;
      throw new DuckException("cannot open DuckDB: " + config.databaseUrl(), e);
    }
  }

  private static void cleanupOrphanRunSchemas(DuckDBConnection connection) throws SQLException {
    var stale = new ArrayList<String>();
    try (var statement = connection.createStatement();
         var schemas = statement.executeQuery("SELECT schema_name FROM information_schema.schemata "
             + "WHERE substr(schema_name, 1, 11) = '_zingg_run_'")) {
      while (schemas.next()) {
        String schema = schemas.getString(1);
        if (!ACTIVE_RUN_SCHEMAS.contains(schema)) stale.add(schema);
      }
    }
    for (String schema : stale) {
      try (var statement = connection.createStatement()) {
        statement.execute("DROP SCHEMA IF EXISTS " + DuckExpr.quote(schema) + " CASCADE");
      }
    }
  }

  public JobHandle openJob() {
    return openJob(ResourceBudget.unlimited());
  }

  public JobHandle openJob(ResourceBudget budget) {
    if (closed.get()) throw new DuckException("runtime is closed");
    try {
      jobSlots.acquire();
      DuckDBConnection duplicate = null;
      String schema = null;
      try {
        JobId id = JobId.create();
        schema = "_zingg_run_" + id.value().replace("-", "");
        duplicate = root.duplicate();
        ACTIVE_RUN_SCHEMAS.add(schema);
        DuckDBConnection ownedConnection = duplicate;
        String ownedSchema = schema;
        DuckJob job = new DuckJob(ownedConnection, id, () -> {
          ACTIVE_RUN_SCHEMAS.remove(ownedSchema);
          jobSlots.release();
        }, budget, DuckCancellation.current());
        // The job now owns both the duplicated JDBC connection and slot.
        duplicate = null;
        return job;
      } catch (SQLException | RuntimeException | Error e) {
        if (schema != null) ACTIVE_RUN_SCHEMAS.remove(schema);
        if (duplicate != null) {
          try { duplicate.close(); }
          catch (SQLException closeFailure) { e.addSuppressed(closeFailure); }
        }
        jobSlots.release();
        if (e instanceof SQLException sql) throw new DuckException("cannot duplicate DuckDB connection", sql);
        if (e instanceof Error error) throw error;
        throw (RuntimeException)e;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new DuckException("interrupted waiting for job slot", e);
    }
  }

  public RuntimeDiagnostics diagnostics() {
    if (closed.get()) throw new DuckException("runtime is closed");
    try (var statement = root.createStatement();
         var result = statement.executeQuery("SELECT current_setting('memory_limit'), "
             + "current_setting('max_temp_directory_size'), current_setting('temp_directory'), "
             + "current_setting('threads')")) {
      if (!result.next()) throw new DuckException("DuckDB did not return runtime settings");
      var memory = Runtime.getRuntime();
      String tempDirectory = result.getString(3);
      return new RuntimeDiagnostics(result.getString(1), result.getString(2), tempDirectory,
          result.getString(4), memory.totalMemory() - memory.freeMemory(), memory.maxMemory(),
          ProcessResourceSnapshot.residentBytes(), ProcessResourceSnapshot.directoryBytes(tempDirectory));
    } catch (SQLException e) {
      throw new DuckException("cannot read DuckDB runtime diagnostics", e);
    }
  }

  public void close() {
    if (closed.compareAndSet(false, true)) {
      try {
        root.close();
      } catch (SQLException e) {
        throw new DuckException("cannot close DuckDB", e);
      }
    }
  }
}
