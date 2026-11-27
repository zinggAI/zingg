package io.zingg.duckdb.worker;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.engine.DuckJob;
import io.zingg.duckdb.engine.DuckRuntime;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Path;
import java.sql.SQLException;

/** End-to-end network-denied probe intended to run inside the packaged offline Docker smoke. */
public final class OfflineNetworkProbe {
  private OfflineNetworkProbe() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 1) throw new IllegalArgumentException("usage: OfflineNetworkProbe <scratch-directory>");
    Path scratch = Path.of(args[0]).toAbsolutePath().normalize();
    java.nio.file.Files.createDirectories(scratch);
    try (var socket = new Socket()) {
      try {
        socket.connect(new InetSocketAddress(InetAddress.getByAddress(new byte[] {1, 1, 1, 1}), 443), 1500);
        throw new AssertionError("outbound TCP unexpectedly succeeded in the network-denied container");
      } catch (java.io.IOException expected) {
        // Docker --network none must make the operating-system connection fail.
      }
    }

    var config = new RuntimeConfig("jdbc:duckdb:" + scratch.resolve("offline-network.duckdb"), 1, 0, null, 1);
    try (var runtime = new DuckRuntime(config); var job = (DuckJob) runtime.openJob()) {
      try (var statement = job.connection().createStatement();
           var settings = statement.executeQuery("SELECT current_setting('autoinstall_known_extensions'), "
               + "current_setting('autoload_known_extensions'), current_setting('enable_external_access')")) {
        if (!settings.next() || !"false".equals(settings.getString(1))
            || !"false".equals(settings.getString(2)) || !"false".equals(settings.getString(3)))
          throw new AssertionError("packaged offline DuckDB security settings are not locked down");
      }
      try {
        job.sql("SELECT * FROM read_csv('https://203.0.113.1/zingg-offline-probe.csv')").count();
        throw new AssertionError("DuckDB unexpectedly read a remote URL in offline mode");
      } catch (DuckException rejected) {
        if (!(rejected.getCause() instanceof SQLException sql)
            || sql.getMessage() == null || sql.getMessage().isBlank())
          throw new AssertionError("remote URL denial did not preserve structured DuckDB SQL diagnostics", rejected);
      }
    }
    System.out.println("OFFLINE_NETWORK_AND_DUCKDB_URL_REJECTED");
  }
}
