package io.zingg.duckdb.legacy;

import io.zingg.duckdb.api.DuckException; import io.zingg.duckdb.model.*; import java.io.IOException; import java.nio.file.*;

public final class ClassifierImporter {
  private final long maxBytes; public ClassifierImporter(long maxBytes){if(maxBytes<1)throw new IllegalArgumentException("maxBytes must be positive");this.maxBytes=maxBytes;}
  public ModelReader.LoadedModel importNative(Path source)throws IOException{if(source==null||!Files.isDirectory(source))throw new DuckException("classifier model directory is required");var model=ModelReader.load(source,maxBytes);if(!ModelType.CLASSIFIER.name().equals(model.manifest().modelType()))throw new DuckException("not a classifier artifact");return model;}
}
