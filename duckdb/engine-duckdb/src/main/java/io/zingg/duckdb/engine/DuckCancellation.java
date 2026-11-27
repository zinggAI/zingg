package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.CancellationToken;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Cancellation token that can cancel every currently executing JDBC statement. */
public final class DuckCancellation implements CancellationToken {
  private static final ThreadLocal<DuckCancellation> CURRENT = new ThreadLocal<>();
  private static final ScheduledExecutorService TIMER = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "zingg-duckdb-cancellation");
    thread.setDaemon(true);
    return thread;
  });

  private final AtomicBoolean cancelled = new AtomicBoolean();
  private final Set<Statement> activeStatements = ConcurrentHashMap.newKeySet();

  /** Returns the cancellation scope installed on this worker request thread, if any. */
  public static DuckCancellation current() { return CURRENT.get(); }

  /** Installs a request cancellation scope for the duration of a synchronous operation. */
  public static Scope install(DuckCancellation cancellation) {
    DuckCancellation previous = CURRENT.get();
    CURRENT.set(cancellation);
    return () -> {
      if (previous == null) CURRENT.remove();
      else CURRENT.set(previous);
    };
  }

  @FunctionalInterface public interface Scope extends AutoCloseable {
    @Override void close();
  }

  public void attach(Statement statement) {
    statement = unwrap(statement);
    if (cancelled.get()) cancel(statement);
    activeStatements.add(statement);
    if (cancelled.get()) cancel(statement);
  }

  public void detach(Statement statement) { activeStatements.remove(unwrap(statement)); }

  public void cancel() {
    if (!cancelled.compareAndSet(false, true)) return;
    for (Statement statement : activeStatements) cancel(statement);
  }

  @Override public boolean isCancelled() { return cancelled.get(); }

  public ScheduledFuture<?> cancelAfter(Duration timeout) {
    if (timeout == null || timeout.isNegative() || timeout.isZero())
      throw new IllegalArgumentException("timeout must be positive");
    return TIMER.schedule((Runnable) this::cancel, timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /** Wraps a JDBC statement so active native/JDBC calls are visible to cancel(). */
  public Statement track(Statement statement) {
    Class<?> contract = statement instanceof PreparedStatement ? PreparedStatement.class : Statement.class;
    return (Statement) Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[] {contract},
        new TrackedStatement(statement));
  }

  private final class TrackedStatement implements java.lang.reflect.InvocationHandler {
    private final Statement target;
    private TrackedStatement(Statement target) { this.target = target; }
    @Override public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments)
        throws Throwable {
      String name = method.getName();
      if (name.equals("close")) {
        try { return DuckCancellation.invoke(target, method, arguments); }
        finally { detach(target); }
      }
      if (isExecutionMethod(name)) {
        if (cancelled.get()) throw new SQLException("operation was cancelled");
        attach(target);
      }
      return DuckCancellation.invoke(target, method, arguments);
    }
  }

  private static boolean isExecutionMethod(String name) {
    return name.equals("execute") || name.equals("executeQuery") || name.equals("executeUpdate")
        || name.equals("executeLargeUpdate") || name.equals("executeBatch")
        || name.equals("executeLargeBatch");
  }

  private static Object invoke(Statement statement, java.lang.reflect.Method method, Object[] args)
      throws Throwable {
    try { return method.invoke(statement, args); }
    catch (InvocationTargetException failure) { throw failure.getCause(); }
  }

  private static Statement unwrap(Statement statement) {
    if (Proxy.isProxyClass(statement.getClass())) {
      var handler = Proxy.getInvocationHandler(statement);
      if (handler instanceof TrackedStatement tracked) return tracked.target;
    }
    return statement;
  }

  private static void cancel(Statement statement) {
    try { statement.cancel(); } catch (Exception ignored) { }
  }
}
