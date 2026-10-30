package io.zingg.duckdb.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Atomically publishes a completed output, failing closed when unsupported. */
@FunctionalInterface
interface OutputPublisher {
  void publish(Path staged, Path destination) throws IOException;

  static OutputPublisher atomic() {
    return (staged, destination) -> Files.move(staged, destination,
        StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
  }
}
