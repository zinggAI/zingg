package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Files;
import java.nio.file.Path;

/** Child-JVM probe held inside Arrow ingestion until the parent forcibly terminates it. */
public final class ArrowIngressKillProbe {
  private ArrowIngressKillProbe() {}

  public static void main(String[] args) throws Exception {
    Path database = Path.of(args[0]);
    Path input = Path.of(args[1]);
    Path ready = Path.of(args[2]);
    try (var runtime = new DuckRuntime(new RuntimeConfig("jdbc:duckdb:" + database, 1, 0, null, 1));
         var job = (DuckJob) runtime.openJob()) {
      ArrowFileSupport.read(job, input, false, importedRows -> {
        if (importedRows == 1) {
          Files.writeString(ready, job.schemaName());
          while (true) Thread.sleep(10_000);
        }
      });
    }
  }
}
