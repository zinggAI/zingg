package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * Enforces the application spill budget while a JDBC statement is executing.
 * The shared registry makes the limit apply to the aggregate temp directory
 * across jobs in one worker rather than giving every job a fresh allowance.
 */
final class SpillByteGuard implements AutoCloseable {
  private static final long POLL_NANOS = 2_000_000L;
  private static final Object REGISTRY_MONITOR = new Object();
  private static final Map<Path, SharedBudget> REGISTRY = new HashMap<>();

  private final Statement statement;
  private final Path directory;
  private final long limit;
  private final SharedBudget shared;
  private final AtomicBoolean closed = new AtomicBoolean();
  private volatile boolean started;
  private volatile boolean exceeded;
  private volatile boolean inspectionFailed;
  private Thread watcher;

  private SpillByteGuard(Statement statement, Path directory, long limit) {
    this.statement = statement;
    this.directory = directory.toAbsolutePath().normalize();
    this.limit = limit;
    synchronized (REGISTRY_MONITOR) {
      this.shared = REGISTRY.computeIfAbsent(this.directory, ignored -> new SharedBudget(this.directory));
      this.shared.limit = this.shared.limit == 0 ? limit : Math.min(this.shared.limit, limit);
    }
  }

  static Statement track(Statement statement, Path directory, long limit) {
    if (limit <= 0) return statement;
    var guard = new SpillByteGuard(statement, directory, limit);
    Class<?> contract = statement instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
    return (Statement) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[] {contract},
        (proxy, method, arguments) -> {
          String name = method.getName();
          if (name.equals("close")) {
            try { return invoke(statement, method, arguments); }
            finally { guard.close(); }
          }
          if (isExecutionMethod(name)) guard.begin();
          try {
            Object result = invoke(statement, method, arguments);
            guard.throwIfExceeded();
            return result;
          } catch (Throwable failure) {
            guard.throwIfExceeded();
            throw failure;
          }
        });
  }

  void begin() {
    if (started || closed.get()) return;
    synchronized (shared) {
      if (started || closed.get()) return;
      shared.active.add(this);
      if (shared.active.size() == 1) shared.baseline = ProcessResourceSnapshot.directoryBytes(directory.toString());
      started = true;
    }
    watcher = new Thread(this::watch, "zingg-spill-budget");
    watcher.setDaemon(true);
    watcher.start();
  }

  private void watch() {
    while (!closed.get() && !exceeded && !inspectionFailed) {
      long bytes = ProcessResourceSnapshot.directoryBytes(directory.toString());
      if (bytes < 0) {
        inspectionFailed = true;
        shared.cancelAll();
        return;
      }
      long baseline;
      long sharedLimit;
      synchronized (shared) {
        baseline = shared.baseline;
        sharedLimit = shared.limit;
      }
      if (bytes - baseline > sharedLimit) {
        exceeded = true;
        shared.cancelAll();
        return;
      }
      LockSupport.parkNanos(POLL_NANOS);
    }
  }

  private void throwIfExceeded() {
    if (exceeded) throw new DuckException("spill byte budget exceeded: directory=" + directory
        + ", limit=" + limit + " bytes");
    if (inspectionFailed)
      throw new DuckException("cannot inspect spill directory while enforcing byte budget: " + directory);
  }

  @Override public void close() {
    if (!closed.compareAndSet(false, true)) return;
    synchronized (shared) {
      shared.active.remove(this);
      if (shared.active.isEmpty()) {
        shared.baseline = 0;
        shared.limit = 0;
      }
    }
    if (watcher != null) {
      watcher.interrupt();
      if (watcher != Thread.currentThread()) {
        try { watcher.join(1000L); }
        catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
      }
    }
    synchronized (REGISTRY_MONITOR) {
      if (shared.active.isEmpty()) REGISTRY.remove(directory, shared);
    }
  }

  private static boolean isExecutionMethod(String name) {
    return name.equals("execute") || name.equals("executeQuery") || name.equals("executeUpdate")
        || name.equals("executeLargeUpdate") || name.equals("executeBatch")
        || name.equals("executeLargeBatch");
  }

  private static Object invoke(Statement statement, java.lang.reflect.Method method, Object[] arguments)
      throws Throwable {
    try { return method.invoke(statement, arguments); }
    catch (InvocationTargetException failure) { throw failure.getCause(); }
  }

  private static final class SharedBudget {
    private final Path directory;
    private final ArrayList<SpillByteGuard> active = new ArrayList<>();
    private long baseline;
    private long limit;

    private SharedBudget(Path directory) { this.directory = directory; }

    private void cancelAll() {
      SpillByteGuard[] guards;
      synchronized (this) { guards = active.toArray(SpillByteGuard[]::new); }
      for (SpillByteGuard guard : guards) {
        guard.exceeded = true;
        try { guard.statement.cancel(); }
        catch (Exception ignored) { }
      }
    }
  }
}
