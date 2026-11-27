package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.engine.DuckCancellation;
import io.zingg.duckdb.engine.DuckExpr;
import io.zingg.duckdb.model.ModelReader;
import io.zingg.duckdb.model.ModelType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ZinggPipelineTest {
  @Test
  void pipelineOutputLimitCancelsStagingWriteAndPreservesDestination(@TempDir Path temp)
      throws Exception {
    Path input=temp.resolve("large-records.csv");
    Path output=temp.resolve("limited-matches.csv");
    StringBuilder csv=new StringBuilder("id,block\n");
    for(int i=0;i<100;i++)csv.append(i).append(",same\n");
    Files.writeString(input,csv);
    Files.writeString(output,"previous complete result\n");
    try(var runtime=runtime(temp)){
      var config=new PipelineConfig(ZinggJob.Phase.MATCH,java.util.List.of(input),output,"TRUE",null,
          new Matcher.MatchConfig("id","block","1.0",0.5),null,1024*1024,128);
      DuckException failure=assertThrows(DuckException.class,()->new ZinggPipeline(runtime).execute(config));
      assertTrue(failure.getMessage().contains("output byte budget exceeded"),failure.toString());
    }
    assertEquals("previous complete result\n",Files.readString(output));
    try(var files=Files.list(temp)){
      assertTrue(files.noneMatch(path->path.getFileName().toString().startsWith(".limited-matches.csv.partial-")),
          "quota rejection must remove pipeline staging output");
    }
  }

  @Test
  void cancellationBeforeOutputPipelineLeavesExistingDestinationUntouched(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("records.csv");
    Path output = temp.resolve("matches.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n");
    Files.writeString(output, "previous complete result\n");
    try (var runtime = runtime(temp)) {
      var cancellation = new DuckCancellation();
      cancellation.cancel();
      try (var ignored = DuckCancellation.install(cancellation)) {
        DuckException failure = assertThrows(DuckException.class, () ->
            new ZinggPipeline(runtime).execute(new PipelineConfig(
                ZinggJob.Phase.MATCH, java.util.List.of(input), output, "TRUE", null,
                new Matcher.MatchConfig("id", "block", "1.0", 0.5))));
        assertTrue(failure.getMessage().contains("operation cancelled"));
      }
      assertEquals("previous complete result\n", Files.readString(output));
      try (var files = Files.list(temp)) {
        assertTrue(files.noneMatch(path -> path.getFileName().toString().startsWith(".matches.csv.partial-")),
            "cancellation before pipeline execution must not create output staging files");
      }
    }
  }

  @Test
  void outputAtomicRenameWinsCancellationOnceCommitMoverStarts(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("records.csv");
    Path output = temp.resolve("matches.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n");
    Files.writeString(output, "previous complete result\n");
    try (var runtime = runtime(temp)) {
      var cancellation = new DuckCancellation();
      var pipeline = new ZinggPipeline(runtime, (source, target) -> {
        cancellation.cancel();
        Files.move(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      });
      var config = new PipelineConfig(ZinggJob.Phase.MATCH, java.util.List.of(input), output,
          "TRUE", null, new Matcher.MatchConfig("id", "block", "1.0", 0.5));
      PipelineResult result;
      try (var ignored = DuckCancellation.install(cancellation)) {
        result = pipeline.execute(config);
      }
      assertEquals("MATCH", result.phase());
      assertEquals(1, result.outputRows());
      assertTrue(Files.readString(output).contains("z_score"),
          "an atomic output rename that has begun is a completed commit even if cancel races it");
      try (var files = Files.list(temp)) {
        assertTrue(files.noneMatch(path -> path.getFileName().toString().startsWith(".matches.csv.partial-")),
            "successful commit must not leave the temporary output");
      }
    }
  }

  @Test
  void hardWorkerStopBeforeOutputRenamePreservesDestinationAndRetryCleansOrphan(@TempDir Path temp)
      throws Exception {
    Path input=temp.resolve("crash-records.csv");
    Path output=temp.resolve("crash-matches.csv");
    Path database=temp.resolve("crash-worker.duckdb");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Files.writeString(output,"previous complete result\n");

    String executable=System.getProperty("os.name").startsWith("Windows")?"java.exe":"java";
    String javaExecutablePath=Path.of(System.getProperty("java.home"),"bin",executable).toString();
    var child=new ProcessBuilder(javaExecutablePath,"-cp",System.getProperty("java.class.path"),
        PipelineOutputCrashProbe.class.getName(),input.toString(),output.toString(),database.toString())
        .redirectErrorStream(true).start();
    assertTrue(child.waitFor(60,java.util.concurrent.TimeUnit.SECONDS),"output crash probe timed out");
    assertEquals(73,child.exitValue(),new String(child.getInputStream().readAllBytes()));
    assertEquals("previous complete result\n",Files.readString(output),
        "the old destination must survive process death before the atomic rename");
    try(var files=Files.list(temp)){
      assertEquals(1,files.filter(path->path.getFileName().toString().startsWith(".crash-matches.csv.partial-")).count(),
          "hard stop should leave precisely the staged output for startup recovery");
    }

    try(var runtime=runtime(temp)){
      var result=new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.MATCH,java.util.List.of(input),output,"TRUE",null,
          new Matcher.MatchConfig("id","block","1.0",0.5)));
      assertEquals(1,result.outputRows());
      assertTrue(Files.readString(output).contains("z_score"));
      try(var files=Files.list(temp)){
        assertTrue(files.noneMatch(path->path.getFileName().toString().startsWith(".crash-matches.csv.partial-")),
            "retry must clean the orphan before committing a new complete output");
      }
    }
  }

  @Test
  void separateProcessesSerializePublicationToTheSameDestination(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("contended-records.csv");
    Path output=temp.resolve("contended-matches.csv");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Files.writeString(output,"previous complete result\n");
    String executable=System.getProperty("os.name").startsWith("Windows")?"java.exe":"java";
    String javaExecutablePath=Path.of(System.getProperty("java.home"),"bin",executable).toString();
    Process first=null; Process second=null;
    try {
      first=startOutputLockProbe(javaExecutablePath,temp,input,output,"first");
      awaitFile(temp.resolve("first-entered"));
      second=startOutputLockProbe(javaExecutablePath,temp,input,output,"second");
      awaitFile(temp.resolve("second-waiting"));
      assertTrue(Files.notExists(temp.resolve("second-entered")),
          "second process must block before entering its atomic mover while first owns the destination lock");

      Files.writeString(temp.resolve("first-release"),"release first publisher");
      assertTrue(first.waitFor(60,java.util.concurrent.TimeUnit.SECONDS),"first publisher timed out");
      assertEquals(0,first.exitValue(),"first publisher child failed: "+new String(first.getInputStream().readAllBytes()));
      awaitFile(temp.resolve("second-entered"));
      Files.writeString(temp.resolve("second-release"),"release second publisher");
      assertTrue(second.waitFor(60,java.util.concurrent.TimeUnit.SECONDS),"second publisher timed out");
      assertEquals(0,second.exitValue(),"second publisher child failed: "+new String(second.getInputStream().readAllBytes()));
      assertTrue(Files.readString(output).contains("z_score"),"destination must be a complete published result");
      try(var files=Files.list(temp)){
        assertTrue(files.noneMatch(path->path.getFileName().toString().startsWith(".contended-matches.csv.partial-")),
            "both publishers must leave no staging output");
      }
    } finally {
      if(first!=null&&first.isAlive())first.destroyForcibly();
      if(second!=null&&second.isAlive())second.destroyForcibly();
    }
  }

  @Test
  void outputRecoveryIgnoresSymlinkStagingEntries(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("symlink-records.csv");
    Path output=temp.resolve("symlink-matches.csv");
    Path external=temp.resolve("external-sentinel.txt");
    Path stagingLink=temp.resolve(".symlink-matches.csv.partial-attacker");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Files.writeString(external,"must not be followed or deleted");
    try { Files.createSymbolicLink(stagingLink,external); }
    catch(IOException|UnsupportedOperationException|SecurityException unavailable){
      Assumptions.assumeTrue(false,"symbolic links unavailable on this runner: "+unavailable.getMessage());
    }

    try(var runtime=runtime(temp)){
      new ZinggPipeline(runtime).execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),output,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5)));
    }

    assertTrue(Files.isSymbolicLink(stagingLink),"recovery must ignore symlink staging entries");
    assertEquals("must not be followed or deleted",Files.readString(external));
    assertTrue(Files.readString(output).contains("z_score"));
  }

  @Test
  void outputLockSymlinkFailsClosedBeforeDestinationPublication(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("lock-records.csv");
    Path output=temp.resolve("lock-matches.csv");
    Path external=temp.resolve("external-lock-target.txt");
    Path lockLink=temp.resolve(".lock-matches.csv.lock");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Files.writeString(external,"lock target remains unchanged");
    try { Files.createSymbolicLink(lockLink,external); }
    catch(IOException|UnsupportedOperationException|SecurityException unavailable){
      Assumptions.assumeTrue(false,"symbolic links unavailable on this runner: "+unavailable.getMessage());
    }

    try(var runtime=runtime(temp)){
      DuckException failure=assertThrows(DuckException.class,()->new ZinggPipeline(runtime).execute(
          new PipelineConfig(ZinggJob.Phase.MATCH,java.util.List.of(input),output,"TRUE",null,
              new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(failure.getMessage().contains("atomic output write failed"));
    }

    assertTrue(Files.isSymbolicLink(lockLink));
    assertEquals("lock target remains unchanged",Files.readString(external));
    assertTrue(Files.notExists(output),"a symlinked lock target must never authorize output publication");
  }

  @Test
  void sameJvmSerializesSymlinkAliasesOfOneOutput(@TempDir Path temp) throws Exception {
    Path realDirectory=Files.createDirectory(temp.resolve("real-output"));
    Path aliasDirectory=temp.resolve("output-alias");
    try { Files.createSymbolicLink(aliasDirectory,realDirectory); }
    catch(IOException|UnsupportedOperationException|SecurityException unavailable){
      Assumptions.assumeTrue(false,"directory symlinks unavailable on this runner: "+unavailable.getMessage());
    }
    Path input=temp.resolve("alias-records.csv");
    Path realOutput=realDirectory.resolve("matches.csv");
    Path aliasedOutput=aliasDirectory.resolve("matches.csv");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Path firstDbDirectory=Files.createDirectory(temp.resolve("db-one"));
    Path secondDbDirectory=Files.createDirectory(temp.resolve("db-two"));
    var firstEnteredMover=new java.util.concurrent.CountDownLatch(1);
    var releaseFirstMover=new java.util.concurrent.CountDownLatch(1);
    var secondWaitingForLock=new java.util.concurrent.CountDownLatch(1);
    var secondEnteredMover=new java.util.concurrent.CountDownLatch(1);
    try(var firstRuntime=runtime(firstDbDirectory);
        var secondRuntime=runtime(secondDbDirectory);
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2)){
      var firstPipeline=new ZinggPipeline(firstRuntime,(source,target)->{
        firstEnteredMover.countDown();
        boolean released;
        try{released=releaseFirstMover.await(30,java.util.concurrent.TimeUnit.SECONDS);}
        catch(InterruptedException interrupted){
          Thread.currentThread().interrupt();
          throw new IOException("interrupted while waiting to release first same-JVM mover",interrupted);
        }
        if(!released)
          throw new IOException("timed out waiting to release first same-JVM mover");
        Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      });
      var secondPipeline=new ZinggPipeline(secondRuntime,(source,target)->{
        secondEnteredMover.countDown();
        Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      },secondWaitingForLock::countDown);
      var first=executor.submit(()->firstPipeline.execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),realOutput,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(firstEnteredMover.await(60,java.util.concurrent.TimeUnit.SECONDS),"first mover did not start");
      var second=executor.submit(()->secondPipeline.execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),aliasedOutput,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(secondWaitingForLock.await(60,java.util.concurrent.TimeUnit.SECONDS),"second writer did not request lock");
      assertFalse(second.isDone(),"same-JVM alias writer must wait, not fail with an overlapping FileLock");
      assertFalse(secondEnteredMover.await(250,java.util.concurrent.TimeUnit.MILLISECONDS),
          "second alias must not enter the mover until the first publishes");
      releaseFirstMover.countDown();
      assertEquals(1,first.get(60,java.util.concurrent.TimeUnit.SECONDS).outputRows());
      assertEquals(1,second.get(60,java.util.concurrent.TimeUnit.SECONDS).outputRows());
      assertTrue(secondEnteredMover.await(1,java.util.concurrent.TimeUnit.SECONDS));
      assertTrue(Files.readString(realOutput).contains("z_score"));
      try(var files=Files.list(realDirectory)){
        assertTrue(files.noneMatch(path->path.getFileName().toString().startsWith(".matches.csv.partial-")),
            "alias writers must leave no staging artifacts");
      }
    } finally { releaseFirstMover.countDown(); }
  }

  @Test
  void cancellationInterruptsAWaitForTheOutputLock(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("cancel-lock-records.csv");
    Path output=temp.resolve("cancel-lock-matches.csv");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Path firstDb=Files.createDirectory(temp.resolve("cancel-lock-db-one"));
    Path secondDb=Files.createDirectory(temp.resolve("cancel-lock-db-two"));
    var firstEntered=new java.util.concurrent.CountDownLatch(1);
    var releaseFirst=new java.util.concurrent.CountDownLatch(1);
    var secondWaiting=new java.util.concurrent.CountDownLatch(1);
    var cancellation=new DuckCancellation();
    try(var firstRuntime=runtime(firstDb); var secondRuntime=runtime(secondDb);
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
      var firstPipeline=new ZinggPipeline(firstRuntime,(source,target)->{
        firstEntered.countDown();
        try {
          if(!releaseFirst.await(30,java.util.concurrent.TimeUnit.SECONDS))
            throw new IOException("timed out waiting to release first output publisher");
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new IOException("interrupted waiting to release first output publisher", interrupted);
        }
        Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      });
      var secondPipeline=new ZinggPipeline(secondRuntime,
          (source,target)->Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
              java.nio.file.StandardCopyOption.ATOMIC_MOVE), secondWaiting::countDown);
      var first=executor.submit(()->firstPipeline.execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),output,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(firstEntered.await(60,java.util.concurrent.TimeUnit.SECONDS));
      var second=executor.submit(()->{
        try(var ignored=DuckCancellation.install(cancellation)) {
          return assertThrows(DuckException.class,()->secondPipeline.execute(new PipelineConfig(
              ZinggJob.Phase.MATCH,java.util.List.of(input),output,"TRUE",null,
              new Matcher.MatchConfig("id","block","1.0",0.5))));
        }
      });
      assertTrue(secondWaiting.await(60,java.util.concurrent.TimeUnit.SECONDS));
      cancellation.cancel();
      DuckException failure=second.get(10,java.util.concurrent.TimeUnit.SECONDS);
      assertTrue(failure.getMessage().contains("operation cancelled"),failure.toString());
      releaseFirst.countDown();
      assertEquals(1,first.get(60,java.util.concurrent.TimeUnit.SECONDS).outputRows());
    } finally { releaseFirst.countDown(); }
  }

  @Test
  void sameJvmAllowsParallelPublicationToDistinctOutputs(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("parallel-records.csv");
    Path firstOutputDirectory=Files.createDirectory(temp.resolve("parallel-one"));
    Path secondOutputDirectory=Files.createDirectory(temp.resolve("parallel-two"));
    Path firstOutput=firstOutputDirectory.resolve("matches.csv");
    Path secondOutput=secondOutputDirectory.resolve("matches.csv");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Path firstDb=Files.createDirectory(temp.resolve("parallel-db-one"));
    Path secondDb=Files.createDirectory(temp.resolve("parallel-db-two"));
    var firstEnteredMover=new java.util.concurrent.CountDownLatch(1);
    var releaseFirstMover=new java.util.concurrent.CountDownLatch(1);
    var secondEnteredMover=new java.util.concurrent.CountDownLatch(1);
    try(var firstRuntime=runtime(firstDb);
        var secondRuntime=runtime(secondDb);
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2)){
      var firstPipeline=new ZinggPipeline(firstRuntime,(source,target)->{
        firstEnteredMover.countDown();
        boolean released;
        try{released=releaseFirstMover.await(30,java.util.concurrent.TimeUnit.SECONDS);}
        catch(InterruptedException interrupted){
          Thread.currentThread().interrupt();
          throw new IOException("interrupted while waiting to release first distinct-output mover",interrupted);
        }
        if(!released)throw new IOException("timed out waiting to release first distinct-output mover");
        Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      });
      var secondPipeline=new ZinggPipeline(secondRuntime,(source,target)->{
        secondEnteredMover.countDown();
        Files.move(source,target,java.nio.file.StandardCopyOption.REPLACE_EXISTING,
            java.nio.file.StandardCopyOption.ATOMIC_MOVE);
      });
      var first=executor.submit(()->firstPipeline.execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),firstOutput,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(firstEnteredMover.await(60,java.util.concurrent.TimeUnit.SECONDS),"first mover did not start");
      var second=executor.submit(()->secondPipeline.execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),secondOutput,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(secondEnteredMover.await(60,java.util.concurrent.TimeUnit.SECONDS),
          "a different destination must not wait for the first destination's lock");
      assertEquals(1,second.get(60,java.util.concurrent.TimeUnit.SECONDS).outputRows());
      assertFalse(first.isDone(),"first mover remains intentionally held while second destination completes");
      releaseFirstMover.countDown();
      assertEquals(1,first.get(60,java.util.concurrent.TimeUnit.SECONDS).outputRows());
      assertTrue(Files.readString(firstOutput).contains("z_score"));
      assertTrue(Files.readString(secondOutput).contains("z_score"));
    } finally { releaseFirstMover.countDown(); }
  }

  @Test
  void outputLockOpenFailurePreservesPriorOutputAndAllowsRetry(@TempDir Path temp) throws Exception {
    Path input=temp.resolve("lock-failure-records.csv");
    Path output=temp.resolve("lock-failure-matches.csv");
    Path lockPath=temp.resolve(".lock-failure-matches.csv.lock");
    Files.writeString(input,"id,block\n1,a\n2,a\n");
    Files.writeString(output,"previous complete result\n");
    Files.createDirectory(lockPath); // A directory at the reserved lock-file path cannot be opened as a FileChannel.

    try(var runtime=runtime(temp)){
      DuckException failure=assertThrows(DuckException.class,()->new ZinggPipeline(runtime).execute(
          new PipelineConfig(ZinggJob.Phase.MATCH,java.util.List.of(input),output,"TRUE",null,
              new Matcher.MatchConfig("id","block","1.0",0.5))));
      assertTrue(failure.getMessage().contains("atomic output write failed"));
      assertTrue(failure.getCause() instanceof IOException);
      assertEquals("previous complete result\n",Files.readString(output));
      try(var files=Files.list(temp)){
        assertTrue(files.noneMatch(path->path.getFileName().toString().startsWith(".lock-failure-matches.csv.partial-")),
            "lock acquisition must precede staging creation and orphan cleanup");
      }

      Files.delete(lockPath);
      var retry=new ZinggPipeline(runtime).execute(new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(input),output,"TRUE",null,new Matcher.MatchConfig("id","block","1.0",0.5)));
      assertEquals(1,retry.outputRows(),"failed OS lock open must release the in-JVM reference-counted lock");
      assertTrue(Files.readString(output).contains("z_score"));
    }
  }

  private static Process startOutputLockProbe(String java,Path temp,Path input,Path output,String name)
      throws java.io.IOException {
    return new ProcessBuilder(java,"-cp",System.getProperty("java.class.path"),
        PipelineOutputLockProbe.class.getName(),input.toString(),output.toString(),
        temp.resolve(name+".duckdb").toString(),temp.resolve(name+"-waiting").toString(),
        temp.resolve(name+"-entered").toString(),temp.resolve(name+"-release").toString())
        .redirectErrorStream(true).start();
  }

  private static void awaitFile(Path marker) throws Exception {
    long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(60);
    while(!Files.exists(marker)&&System.nanoTime()<deadline)Thread.sleep(10);
    assertTrue(Files.exists(marker),"timed out waiting for child marker: "+marker.getFileName());
  }

  @Test
  void configuredMatcherPathDoesNotRequireARegistryExecutor(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("records.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n3,b\n");
    Path output = temp.resolve("matches.csv");
    var runtimeConfig = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("pipeline.duckdb"), 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null)) {
      var matcher = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
      var result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.MATCH, java.util.List.of(input), output, "TRUE", null, matcher));
      assertEquals(1, result.outputRows());
      assertEquals("MATCH", result.phase());
      assertTrue(Files.exists(output));
    }
  }

  @Test
  void registryMatchIsNotFilteredAgainByPipelinePredicate(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("records.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n3,b\n");
    Path output = temp.resolve("matches.csv");
    var matcher = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    try (var runtime = runtime(temp)) {
      runtime.registerPhaseExecutor(ZinggJob.Phase.MATCH,
          (job, frame) -> job.matchCandidates(frame, matcher));
      var result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.MATCH, java.util.List.of(input), output, "z_score < 0"));
      assertEquals(1, result.outputRows());
      assertTrue(Files.readString(output).contains("z_score"));
    }
  }

  @Test
  void everyPhaseWithoutExecutorFailsBeforeReadingInputOrPublishingOutput(@TempDir Path temp) {
    try (var runtime = runtime(temp)) {
      for (ZinggJob.Phase phase : ZinggJob.Phase.values()) {
        Path input = temp.resolve("missing-" + phase + ".csv");
        Path output = temp.resolve("unpublished-" + phase + ".csv");
        DuckException failure = assertThrows(DuckException.class, () ->
            new ZinggPipeline(runtime).execute(new PipelineConfig(
                phase, java.util.List.of(input), output, "TRUE")), phase.name());
        assertTrue(failure.getMessage().contains("no executor for phase: " + phase), phase.name());
        assertTrue(Files.notExists(input), phase.name());
        assertTrue(Files.notExists(output), phase.name());
      }
    }
  }

  @Test
  void linkNeverUsesExplicitMatchScorer(@TempDir Path temp) throws Exception {
    Path output = temp.resolve("links.csv");
    var matcher = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    try (var runtime = runtime(temp)) {
      DuckException failure = assertThrows(DuckException.class, () ->
          new ZinggPipeline(runtime).execute(new PipelineConfig(
              ZinggJob.Phase.LINK, java.util.List.of(temp.resolve("missing.csv")), output,
              "TRUE", null, matcher)));
      assertTrue(failure.getMessage().contains("only supported for MATCH"));
      assertTrue(Files.notExists(output));
    }
  }

  @Test
  void classifierArtifactRequiresExplicitMatcherBeforeInputIo(@TempDir Path temp) {
    Path missingInput = temp.resolve("must-not-be-read.csv");
    Path output = temp.resolve("matches.csv");
    try (var runtime = runtime(temp)) {
      PipelineConfig config = new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(missingInput), output, "TRUE", null, null,
          temp.resolve("classifier-model"), 1024);

      DuckException failure = assertThrows(DuckException.class,
          () -> new ZinggPipeline(runtime).execute(config));

      assertTrue(failure.getMessage().contains("classifier model requires an explicit MATCH matcher"));
      assertTrue(Files.notExists(missingInput));
      assertTrue(Files.notExists(output));
    }
  }

  @Test
  void classifierArtifactIsLoadedBeforeInputIo(@TempDir Path temp) {
    Path missingInput = temp.resolve("must-not-be-read-before-model-validation.csv");
    Path output = temp.resolve("must-not-be-published.csv");
    Path missingModel = temp.resolve("missing-classifier");
    var matcher = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    try (var runtime = runtime(temp)) {
      PipelineConfig config = new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(missingInput), output, "TRUE", null, matcher, missingModel, 1024);

      DuckException failure = assertThrows(DuckException.class,
          () -> new ZinggPipeline(runtime).execute(config));

      assertTrue(failure.getMessage().contains("model artifact directory is required"));
      assertTrue(Files.notExists(missingInput));
      assertTrue(Files.notExists(output));
    }
  }

  @Test
  void outputPathPolicyIsCheckedBeforeInputIo(@TempDir Path temp) {
    Path missingInput = temp.resolve("must-not-be-read-before-output-policy.csv");
    Path rejectedOutput = temp.resolve("outside" + java.io.File.separator + "matches.csv");
    var matcher = new Matcher.MatchConfig("id", "block", "1.0", 0.5);
    var policy = new io.zingg.duckdb.api.PathPolicy(null, temp.resolve("allowed-output"));
    try (var runtime = runtime(temp)) {
      PipelineConfig config = new PipelineConfig(ZinggJob.Phase.MATCH,
          java.util.List.of(missingInput), rejectedOutput, "TRUE", policy, matcher);

      DuckException failure = assertThrows(DuckException.class,
          () -> new ZinggPipeline(runtime).execute(config));

      assertTrue(failure.getMessage().contains("output path escapes configured root"));
      assertTrue(Files.notExists(missingInput));
      assertTrue(Files.notExists(rejectedOutput));
    }
  }

  @Test
  void registeredLinkExecutorRunsOnceWithoutMatchTransformation(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("records.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n3,b\n");
    Path output = temp.resolve("links.csv");
    var calls = new java.util.concurrent.atomic.AtomicInteger();
    try (var runtime = runtime(temp)) {
      runtime.registerPhaseExecutor(ZinggJob.Phase.LINK, (job, frame) -> {
        calls.incrementAndGet();
        return frame.filter(new DuckExpr("id = 3"));
      });
      var result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.LINK, java.util.List.of(input), output, "FALSE"));
      assertEquals(1, calls.get());
      assertEquals(1, result.outputRows());
      assertEquals("LINK", result.phase());
      assertTrue(Files.readString(output).contains("3,b"));
    }
  }

  @Test
  void nativeLinkBuildsDeterministicEntityScoresAndEmptySchema(@TempDir Path temp) throws Exception {
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("native-link.duckdb"), 1, 0, null, 1);
    String linkSchema;
    try (var runtime = new CompatibilityRuntime(config, () -> 1700000000000L)) {
      try (var job = new ZinggJob(runtime)) {
        linkSchema = job.schemaName();
        var pairs = job.sql("SELECT * FROM (VALUES ('1','2',0.9),('2','3',0.7),('2','1',0.8),('7','8',0.4)) AS p(z_zid,z_z_zid,z_score)");
        var result = new NativeLinkPhase().execute(job, pairs, runtime.clock());
        assertEquals(java.util.List.of("z_zid", "z_minScore", "z_maxScore", "z_cluster"), result.columns());
        assertEquals(5, result.count());
        var rows = result.collect();
        assertEquals("1", rows.get(0).get("z_zid"));
        assertEquals(0d, ((Number) rows.get(0).get("z_minScore")).doubleValue());
        assertEquals(0.9d, ((Number) rows.get(0).get("z_maxScore")).doubleValue());
        assertEquals("1700000000000:1", rows.get(0).get("z_cluster"));
        assertEquals(0d, ((Number) rows.get(1).get("z_minScore")).doubleValue());
        assertEquals(0.9d, ((Number) rows.get(1).get("z_maxScore")).doubleValue());
        assertEquals("3", rows.get(2).get("z_zid"));
        assertEquals("7", rows.get(3).get("z_zid"));

        var empty = new NativeLinkPhase().execute(job,
            job.sql("SELECT * FROM (VALUES ('x','y',0.1)) AS p(z_zid,z_z_zid,z_score) WHERE FALSE"),
            runtime.clock());
        assertEquals(0, empty.count());
        assertEquals(result.columns(), empty.columns());

        var textualIds = new NativeLinkPhase().execute(job,
            job.sql("SELECT * FROM (VALUES ('customer-A','customer_B',0.25)) AS p(z_zid,z_z_zid,z_score)"),
            runtime.clock());
        assertEquals("customer-A", textualIds.collect().get(0).get("z_zid"));

        var selfEdge = new NativeLinkPhase().execute(job,
            job.sql("SELECT * FROM (VALUES ('same','same',0.5)) AS p(z_zid,z_z_zid,z_score)"), runtime.clock());
        assertEquals(0, selfEdge.count());
        var delimiterCollision = new NativeLinkPhase().execute(job, job.sql(
            "SELECT 'a' || chr(0) || 'b' AS z_zid, 'c' AS z_z_zid, 0.9 AS z_score "
                + "UNION ALL SELECT 'a', 'b' || chr(0) || 'c', 0.7"), runtime.clock());
        assertEquals(4, delimiterCollision.count(), "distinct edges containing the key separator must not collide");
        assertEquals(2, delimiterCollision.collect().stream().map(row -> row.get("z_cluster")).distinct().count());
        DuckException nonFinite = assertThrows(DuckException.class, () ->
            new NativeLinkPhase().execute(job,
                job.sql("SELECT * FROM (VALUES ('a','b',CAST('NaN' AS DOUBLE))) AS p(z_zid,z_z_zid,z_score)"),
                runtime.clock()));
        assertTrue(nonFinite.getMessage().contains("score must be finite"));
      }
    }
    try (var runtime = new io.zingg.duckdb.engine.DuckRuntime(config); var job = runtime.openJob()) {
      assertEquals(0L, ((Number) job.sql("SELECT COUNT(*) AS n FROM information_schema.schemata WHERE schema_name = '" + linkSchema + "'")
          .collect().get(0).get("n")).longValue());
    }
  }

  @Test
  void nativeLinkRejectsMissingAndNullPairFieldsBeforeOutput(@TempDir Path temp) throws Exception {
    try (var runtime = runtime(temp)) {
      try (var job = new ZinggJob(runtime)) {
        NativeLinkPhase link = new NativeLinkPhase();
        DuckException missingError = assertThrows(DuckException.class, () ->
            link.execute(job, job.sql("SELECT 1 AS z_zid, 0.5 AS z_score"), runtime.clock()));
        assertTrue(missingError.getMessage().contains("LINK input column is required"));
        DuckException nullError = assertThrows(DuckException.class, () ->
            link.execute(job, job.sql("SELECT '1' AS z_zid, NULL::VARCHAR AS z_z_zid, 0.5 AS z_score"), runtime.clock()));
        assertTrue(nullError.getMessage().contains("pair IDs must be non-null"));
      }
    }
  }

  @Test
  void nativeLinkPipelinePublishesEntitySchemaAndTransitiveScores(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("scored-pairs.csv");
    Path reorderedInput = temp.resolve("scored-pairs-reordered.csv");
    Path output = temp.resolve("linked-entities.csv");
    Files.writeString(input, "z_zid,z_z_zid,z_score\n1,2,0.8\n");
    Files.writeString(reorderedInput, "z_score,z_z_zid,z_zid\n0.6,3,2\n");
    var config = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("native-link-pipeline.duckdb"),
        1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(config, () -> 1700000000000L)) {
      LocalPhaseExecutors.registerNativeLink(runtime);
      PipelineResult result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.LINK, java.util.List.of(input, reorderedInput), output, "FALSE"));

      assertEquals("LINK", result.phase());
      assertEquals(2, result.inputRows());
      java.util.List<String> lines = Files.readAllLines(output);
      assertEquals(3, result.outputRows(), "published output:\n" + Files.readString(output));
      assertEquals("z_zid,z_minScore,z_maxScore,z_cluster", lines.get(0));
      assertEquals(4, lines.size());
      assertTrue(lines.contains("1,0.0,0.8,1700000000000:1"));
      assertTrue(lines.contains("2,0.0,0.8,1700000000000:1"));
      assertTrue(lines.contains("3,0.0,0.6,1700000000000:1"));
    }
  }

  @Test
  void nativeLinkHonorsConfiguredRowBudgetBeforeGraphMaterialization(@TempDir Path temp) {
    var budget = new io.zingg.duckdb.engine.ResourceBudget(0, 1, 0, 0, 0, 0);
    var runtime = new CompatibilityRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("limited-link.duckdb"), 1, 0, null, 1),
        () -> 1L, "zingg-0.7.0-duckdb-1.5.5.1", budget);
    try (runtime; var job = new ZinggJob(runtime)) {
      var pairs = job.sql("SELECT * FROM (VALUES ('1','2',0.9),('2','3',0.7)) AS p(z_zid,z_z_zid,z_score)");
      DuckException failure = assertThrows(DuckException.class,
          () -> new NativeLinkPhase().execute(job, pairs, runtime.clock()));
      assertTrue(failure.getMessage().contains("row budget exceeded"));
    }
  }

  @Test
  void nativeLinkRejectsOversizedEdgeRelationBeforeCollecting(@TempDir Path temp) {
    var runtime = runtime(temp);
    try (runtime; var job = new ZinggJob(runtime)) {
      var oversized = job.sql("SELECT 'a' AS z_zid, 'b' AS z_z_zid, 0.5 AS z_score FROM range(1000001)");
      DuckException failure = assertThrows(DuckException.class,
          () -> new NativeLinkPhase().execute(job, oversized, runtime.clock()));
      assertTrue(failure.getMessage().contains("edge safety limit: 1000000"));
    }
  }

  @Test
  void registeredNativeTrainPublishesTypedHistogramArtifactAndOutput(@TempDir Path temp) throws Exception {
    Path input = temp.resolve("records.csv");
    Files.writeString(input, "id,grp\n1,a\n2,a\n3,b\n");
    Path output = temp.resolve("trained.csv");
    Path artifact = temp.resolve("training-artifact");
    var runtimeConfig = new RuntimeConfig("jdbc:duckdb:" + temp.resolve("train-pipeline.duckdb"), 1, 0, null, 1);
    try (var runtime = new CompatibilityRuntime(runtimeConfig, null)) {
      LocalPhaseExecutors.registerNativeTraining(runtime,
          NativeTrainingConfig.defaults(artifact, "'same-key'", "block_key"));
      var result = new ZinggPipeline(runtime).execute(new PipelineConfig(
          ZinggJob.Phase.TRAIN, java.util.List.of(input), output, "FALSE"));

      assertEquals("TRAIN", result.phase());
      assertEquals(3, result.outputRows());
      assertTrue(Files.exists(output));
      assertTrue(Files.exists(artifact.resolve("manifest.json")));
      ModelReader.LoadedModel loaded = ModelReader.load(artifact, 1024 * 1024);
      assertEquals(ModelType.BACKEND_BLOCKING_HISTOGRAM.name(), loaded.manifest().modelType());
      String payload = new String(loaded.payload(), java.nio.charset.StandardCharsets.UTF_8);
      assertTrue(payload.contains("\"kind\":\"duckdb-blocking-histogram\""));
      assertTrue(payload.contains("\"distinctBlockingKeys\":1"), payload);
      assertTrue(payload.contains("\"maxBucketSize\":3"), payload);
      assertTrue(payload.contains("\"key\":\"same-key\""), payload);
    }
  }

  @Test
  void nativeTrainingKeepsNullAndLiteralSentinelBlockingKeysDistinct(@TempDir Path temp)
      throws Exception {
    Path artifact = temp.resolve("null-key-training-artifact");
    try (var runtime = runtime(temp); var job = new ZinggJob(runtime)) {
      var input = job.sql("SELECT * FROM (VALUES "
          + "(1, 1, CAST(NULL AS VARCHAR)), (2, 2, '__NULL__')) AS t(id, z_zid, block_key)");
      var result = new NativeTrainingService().train(input,
          NativeTrainingConfig.defaults(artifact, "\"block_key\"", "block_key"));
      assertEquals(2, result.sampledRows());
      String payload = new String(ModelReader.load(artifact, 1024 * 1024).payload(),
          java.nio.charset.StandardCharsets.UTF_8);
      assertTrue(payload.contains("\"distinctBlockingKeys\":2"), payload);
      assertTrue(payload.contains("\"isNull\":true,\"key\":null"), payload);
      assertTrue(payload.contains("\"isNull\":false,\"key\":\"__NULL__\""), payload);
    }
  }

  @Test
  void unsupportedAtomicMovePreservesExistingOutputAndTemporaryFile(@TempDir Path temp) throws Exception {
    Path source = temp.resolve("result.partial");
    Path target = temp.resolve("result.csv");
    Files.writeString(source, "new complete result\n");
    Files.writeString(target, "previous complete result\n");

    var failure = assertThrows(java.nio.file.AtomicMoveNotSupportedException.class,
        () -> ZinggPipeline.moveAtomically(source, target, (from, to) -> {
          throw new java.nio.file.AtomicMoveNotSupportedException(from.toString(), to.toString(),
              "simulated filesystem without atomic replacement");
        }));

    assertTrue(failure.getReason().contains("simulated filesystem"));
    assertEquals("previous complete result\n", Files.readString(target));
    assertEquals("new complete result\n", Files.readString(source));
  }

  @Test
  void atomicPublicationFailurePreservesPriorPipelineOutputAndCleansTemporary(@TempDir Path temp)
      throws Exception {
    Path input = temp.resolve("records.csv");
    Path output = temp.resolve("matches.csv");
    Files.writeString(input, "id,block\n1,a\n2,a\n");
    Files.writeString(output, "previous complete result\n");
    var runtime = runtime(temp);
    try (runtime) {
      var pipeline = new ZinggPipeline(runtime, (source, target) -> {
        throw new java.nio.file.AtomicMoveNotSupportedException(source.toString(), target.toString(),
            "simulated filesystem without atomic replacement");
      });
      var config = new PipelineConfig(ZinggJob.Phase.MATCH, java.util.List.of(input), output,
          "TRUE", null, new Matcher.MatchConfig("id", "block", "1.0", 0.5));

      DuckException failure = assertThrows(DuckException.class, () -> pipeline.execute(config));

      assertTrue(failure.getMessage().contains("atomic output write failed"));
      assertTrue(failure.getCause() instanceof java.nio.file.AtomicMoveNotSupportedException);
      assertEquals("previous complete result\n", Files.readString(output));
      try (var files = Files.list(temp)) {
        assertTrue(files.noneMatch(path -> path.getFileName().toString().startsWith(".matches.csv.partial-")));
      }

      PipelineResult retry=new ZinggPipeline(runtime).execute(config);
      assertEquals(1,retry.outputRows(),"atomic publication failure must release the local and OS locks for retry");
      assertTrue(Files.readString(output).contains("z_score"));
    }
  }

  private static CompatibilityRuntime runtime(Path temp) {
    return new CompatibilityRuntime(new RuntimeConfig(
        "jdbc:duckdb:" + temp.resolve("pipeline.duckdb"), 1, 0, null, 1), null);
  }
}
