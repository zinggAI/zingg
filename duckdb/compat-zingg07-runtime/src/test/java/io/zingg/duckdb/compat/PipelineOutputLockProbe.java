package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.RuntimeConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Child JVM whose mover can be held to deterministically test inter-process target locking. */
public final class PipelineOutputLockProbe {
  private PipelineOutputLockProbe() {}

  public static void main(String[] args) throws Exception {
    if(args.length!=6)throw new IllegalArgumentException("input, output, database, waiting, entered, release required");
    Path input=Path.of(args[0]); Path output=Path.of(args[1]); Path database=Path.of(args[2]);
    Path waiting=Path.of(args[3]); Path entered=Path.of(args[4]); Path release=Path.of(args[5]);
    var config=new PipelineConfig(ZinggJob.Phase.MATCH,List.of(input),output,"TRUE",null,
        new Matcher.MatchConfig("id","block","1.0",0.5));
    try(var runtime=new CompatibilityRuntime(new RuntimeConfig(
        "jdbc:duckdb:"+database,1,0,null,1),null)){
      new ZinggPipeline(runtime,(source,target)->{
        Files.writeString(entered,"mover entered");
        long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
        while(!Files.exists(release)&&System.nanoTime()<deadline){
          try{Thread.sleep(10);}catch(InterruptedException interrupted){
            Thread.currentThread().interrupt();
            throw new java.io.IOException("interrupted while waiting for release marker",interrupted);
          }
        }
        if(!Files.exists(release))throw new IllegalStateException("timed out waiting for release marker");
        Files.move(source,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
      },()->Files.writeString(waiting,"attempting lock acquisition")).execute(config);
    }
  }
}
