package io.zingg.duckdb.legacy;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.*;
import java.io.IOException; import java.nio.file.*;

/** Boundary object for an out-of-process Spark 3.5.5 importer. It emits neutral model artifacts only. */
public final class BlockingImporter {
  private final long maxBytes;
  public BlockingImporter(long maxBytes){if(maxBytes<1)throw new IllegalArgumentException("maxBytes must be positive");this.maxBytes=maxBytes;}
  public ModelReader.LoadedModel importNative(Path source) throws IOException {
    if(source==null||!Files.isDirectory(source))throw new DuckException("blocking model directory is required");
    var model=ModelReader.load(source,maxBytes);
    if(!ModelType.BLOCKING_TREE.name().equals(model.manifest().modelType()))throw new DuckException("not a blocking-tree artifact");
    return model;
  }
}
