package io.zingg.duckdb.legacy;

import io.zingg.duckdb.model.ImportLimits;
import java.nio.file.*;
import org.apache.spark.sql.*;

/** Spark 3.5.5 boundary: extracts the serialized blocking-tree column, then delegates to the neutral adapter. */
public final class SparkBlockingTreeImporterMain {
  private SparkBlockingTreeImporterMain() {}
  public static void main(String[] args) throws Exception {
    Path input=Path.of(required("ZINGG_IMPORT_INPUT")), output=Path.of(required("ZINGG_IMPORT_OUTPUT"));
    ImportLimits limits=new ImportLimits(Long.parseLong(required("ZINGG_IMPORT_MAX_BYTES")),Integer.parseInt(required("ZINGG_IMPORT_MAX_DEPTH")),Long.parseLong(required("ZINGG_IMPORT_MAX_REFERENCES")),Long.parseLong(required("ZINGG_IMPORT_TIMEOUT_MILLIS")));
    SparkSession spark=SparkSession.builder().appName("zingg-duckdb-legacy-blocking-import").master("local[1]").config("spark.ui.enabled","false").getOrCreate();
    try {
      Dataset<Row> data=spark.read().parquet(input.toString()); Row row=data.first(); if(row==null)throw new IllegalArgumentException("blocking artifact is empty");
      byte[] bytes=null; for(int i=0;i<row.size();i++) if(row.get(i) instanceof byte[] b){bytes=b;break;}
      if(bytes==null)throw new IllegalArgumentException("blocking artifact has no binary serialized-tree column");
      Path staging=Files.createTempFile("zingg-blocking-tree-", ".bin"); try { Files.write(staging,bytes); LegacyBlockingTreeImporterMain.importSerialized(staging,output,limits); } finally { Files.deleteIfExists(staging); }
    } finally { spark.stop(); }
  }
  private static String required(String name){String value=System.getenv(name);if(value==null||value.isBlank())throw new IllegalArgumentException("missing importer environment: "+name);return value;}
}
