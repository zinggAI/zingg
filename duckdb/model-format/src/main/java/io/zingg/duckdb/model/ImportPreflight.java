package io.zingg.duckdb.model;

import io.zingg.duckdb.api.DuckException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Validates legacy input trees before handing them to an isolated importer. */
public final class ImportPreflight {
  private ImportPreflight() {}
  public static void validate(Path input, ImportLimits limits) throws IOException {
    if (input == null || limits == null) throw new IllegalArgumentException("input and limits are required");
    Path root = input.toAbsolutePath().normalize();
    if (!Files.isDirectory(root)) throw new DuckException("legacy input directory missing: " + root);
    long references = 0, bytes = 0;
    try (var paths = Files.walk(root)) {
      for (var it = paths.iterator(); it.hasNext();) {
        Path path = it.next();
        references++;
        if (references > limits.maxReferences()) throw new DuckException("legacy input reference limit exceeded");
        if (Files.isSymbolicLink(path)) throw new DuckException("legacy input contains a symbolic link: " + path);
        int depth = root.relativize(path).getNameCount();
        if (depth > limits.maxDepth()) throw new DuckException("legacy input depth limit exceeded");
        if (Files.isRegularFile(path)) {
          long size = Files.size(path);
          if (size > limits.maxBytes() - bytes) throw new DuckException("legacy input byte limit exceeded");
          bytes += size;
        }
      }
    }
  }
}
