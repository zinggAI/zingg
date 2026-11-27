package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Path;
import java.util.List;

/** Child JVM that halts while holding the output publication lock, before atomic rename. */
public final class PipelineOutputCrashProbe {
  private PipelineOutputCrashProbe() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 3) throw new IllegalArgumentException("input, output, and database paths are required");
    Path input=Path.of(args[0]);
    Path output=Path.of(args[1]);
    Path database=Path.of(args[2]);
    var config=new PipelineConfig(ZinggJob.Phase.MATCH,List.of(input),output,"TRUE",null,
        new Matcher.MatchConfig("id","block","1.0",0.5));
    try(var runtime=new CompatibilityRuntime(new RuntimeConfig(
        "jdbc:duckdb:"+database,1,0,null,1),null)){
      new ZinggPipeline(runtime,(source,target)->Runtime.getRuntime().halt(73)).execute(config);
    }
    throw new AssertionError("crash probe did not halt during publication");
  }
}
