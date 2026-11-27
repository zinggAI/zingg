package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.*; import java.io.IOException; import java.nio.file.*;
import io.zingg.duckdb.engine.DuckCancellation;
import io.zingg.duckdb.engine.ResourceBudget;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/** Executable phase pipeline for the first native DuckDB milestone. */
public final class ZinggPipeline {
  private static final Object OUTPUT_LOCKS_MONITOR = new Object();
  private static final Map<String, LocalOutputLock> OUTPUT_LOCKS = new HashMap<>();
  private static final class LocalOutputLock {
    private final ReentrantLock lock = new ReentrantLock();
    private int users;
  }
  @FunctionalInterface
  interface AtomicMover {
    void move(Path source, Path target) throws IOException;
  }
  @FunctionalInterface
  interface OutputLockObserver { void waitingForLock() throws IOException; }

  private final CompatibilityRuntime runtime;
  private final AtomicMover atomicMover;
  private final OutputLockObserver outputLockObserver;
  public ZinggPipeline(CompatibilityRuntime runtime){
    this(runtime, (source, target) -> java.nio.file.Files.move(source, target,
        java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE), () -> {});
  }
  ZinggPipeline(CompatibilityRuntime runtime, AtomicMover atomicMover){
    this(runtime,atomicMover,()->{});
  }
  ZinggPipeline(CompatibilityRuntime runtime, AtomicMover atomicMover, OutputLockObserver outputLockObserver){
    this.runtime=java.util.Objects.requireNonNull(runtime, "runtime is required");
    this.atomicMover=java.util.Objects.requireNonNull(atomicMover, "atomic mover is required");
    this.outputLockObserver=java.util.Objects.requireNonNull(outputLockObserver,"output lock observer is required");
  }
  public PipelineResult execute(PipelineConfig config){
    checkCancellation();
    if(config.matcher()!=null && config.phase()!=ZinggJob.Phase.MATCH)
      throw new DuckException("explicit matcher is only supported for MATCH, not "+config.phase());
    if(config.classifierModel()!=null && config.matcher()==null)
      throw new DuckException("classifier model requires an explicit MATCH matcher");
    if(config.matcher()==null && !runtime.phases().phases().contains(config.phase()))
      throw new DuckException("no executor for phase: "+config.phase());
    // Apply all path-policy checks before creating a job or performing input I/O.
    Path outputPath=config.pathPolicy()==null?config.output():config.pathPolicy().output(config.output());
    // Validate and load all model inputs before opening jobs or reading data.
    // A corrupt/missing classifier must not cause expensive or observable input I/O.
    LinearClassifier classifier = null;
    if (config.classifierModel() != null) {
      try {
        Path modelPath=config.pathPolicy()==null?config.classifierModel():config.pathPolicy().input(config.classifierModel());
        var loaded=io.zingg.duckdb.model.ModelReader.load(modelPath,config.maxModelBytes());
        classifier=(LinearClassifier)new ModelScorerRegistry().create(loaded);
      } catch(IOException e) {
        throw new DuckException("classifier model load failed",e);
      }
    }
    try(var job=new ZinggJob(runtime,withOutputLimit(runtime.budget(),config.maxOutputBytes()))){
      Frame input=job.read(config.inputs(),config.phase(),config.pathPolicy()); long in=input.count();
      // The explicit MATCH scorer and a registered phase executor are
      // alternatives. A registered MATCH executor already owns scoring.
      Frame output;
      if(config.matcher()!=null){
      var matcher=new Matcher(); var candidates=matcher.candidates(input,config.matcher());
          if(classifier==null) output=matcher.score(candidates,config.matcher());
          else output=matcher.scoreLinear(candidates,config.matcher(),classifier);
      } else output=runtime.phases().execute(config.phase(),job,input);
      checkCancellation();
      long outputRows = output.count();
      checkCancellation();
      write(output,outputPath,config.maxOutputBytes(),atomicMover,outputLockObserver);
      return new PipelineResult(in,outputRows,config.phase().name(),outputPath.toString());
    }
  }
  private static ResourceBudget withOutputLimit(ResourceBudget configured,long pipelineLimit){
    if(pipelineLimit<=0)return configured;
    long runtimeLimit=configured.maxOutputBytes();
    long effective=runtimeLimit<=0?pipelineLimit:Math.min(runtimeLimit,pipelineLimit);
    return new ResourceBudget(configured.memoryLimitBytes(),configured.maxRows(),configured.maxInputBytes(),
        effective,configured.maxCollectBytes(),configured.maxSpillBytes());
  }
  private static void write(Frame frame,Path output,long maxBytes,AtomicMover atomicMover,
      OutputLockObserver outputLockObserver){
    try {
      Path requested=output.toAbsolutePath().normalize(); Path requestedParent=requested.getParent();
      if(requestedParent==null)throw new DuckException("output must have a parent directory: "+requested);
      java.nio.file.Files.createDirectories(requestedParent);
      // Resolve directory aliases before taking the in-JVM and OS locks;
      // otherwise two lexical paths could overlap a FileLock in the same JVM.
      Path parent=requestedParent.toRealPath();
      Path target=parent.resolve(requested.getFileName());
      String name=target.getFileName().toString(); String suffix="";
      int dot=name.lastIndexOf('.'); if(dot>=0) suffix=name.substring(dot);
      Path lockPath=parent.resolve("."+name+".lock");
      if(java.nio.file.Files.isSymbolicLink(lockPath))throw new IOException("output lock cannot be a symbolic link: "+lockPath);
      String identity=target.toString();
      if(java.io.File.separatorChar=='\\')identity=identity.toLowerCase(java.util.Locale.ROOT);
      outputLockObserver.waitingForLock();
      LocalOutputLock local=acquireOutputLock(identity);
      try {
      try (FileChannel channel=FileChannel.open(lockPath, StandardOpenOption.CREATE,
          StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);
           FileLock ignored=acquireFileLock(channel)) {
        // Only cooperative writers use this lock. It prevents recovery from
        // deleting another live writer's staging file and serializes same-target commits.
        cleanupOrphanOutputs(parent,"."+name+".partial-");
        Path temporary=java.nio.file.Files.createTempFile(parent,"."+name+".partial-",suffix);
        try {
          checkCancellation();
          var format=io.zingg.duckdb.api.OutputFormat.fromPath(target);
          if(format==io.zingg.duckdb.api.OutputFormat.PARQUET)frame.writeParquet(temporary);
          else if(format==io.zingg.duckdb.api.OutputFormat.JSON)frame.writeJson(temporary);
          else if(format==io.zingg.duckdb.api.OutputFormat.ARROW)frame.writeArrow(temporary);
          else frame.writeCsv(temporary,true);
          if(maxBytes>0&&java.nio.file.Files.size(temporary)>maxBytes)throw new DuckException("output exceeds configured byte limit: "+maxBytes);
          // This is the last cancellable point. Once the atomic mover starts,
          // publication is a commit race: a completed rename wins cancellation.
          checkCancellation();
          moveAtomically(temporary, target, atomicMover);
        } finally { java.nio.file.Files.deleteIfExists(temporary); }
      }
      } finally { releaseOutputLock(identity,local); }
    } catch(java.io.IOException e) { throw new DuckException("atomic output write failed",e); }
  }
  private static LocalOutputLock acquireOutputLock(String identity) {
    LocalOutputLock local;
    synchronized(OUTPUT_LOCKS_MONITOR) {
      local=OUTPUT_LOCKS.computeIfAbsent(identity,ignored->new LocalOutputLock());
      local.users++;
    }
    try {
      while (true) {
        checkCancellation();
        try {
          if (local.lock.tryLock(25, java.util.concurrent.TimeUnit.MILLISECONDS)) return local;
        } catch (InterruptedException interrupted) {
          Thread.currentThread().interrupt();
          throw new DuckException("interrupted waiting for output lock", interrupted);
        }
      }
    } catch (RuntimeException | Error failure) {
      synchronized(OUTPUT_LOCKS_MONITOR) {
        if (--local.users == 0) OUTPUT_LOCKS.remove(identity, local);
      }
      throw failure;
    }
  }
  private static void releaseOutputLock(String identity,LocalOutputLock local) {
    local.lock.unlock();
    synchronized(OUTPUT_LOCKS_MONITOR) {
      if(--local.users==0)OUTPUT_LOCKS.remove(identity,local);
    }
  }
  private static void cleanupOrphanOutputs(Path parent,String prefix)throws IOException {
    try(var entries=java.nio.file.Files.newDirectoryStream(parent,prefix+"*")){
      for(Path candidate:entries){
        if(java.nio.file.Files.isRegularFile(candidate,LinkOption.NOFOLLOW_LINKS))
          java.nio.file.Files.deleteIfExists(candidate);
      }
    }
  }
  private static void checkCancellation() {
    DuckCancellation cancellation = DuckCancellation.current();
    if (cancellation != null) cancellation.throwIfCancelled();
  }
  private static FileLock acquireFileLock(FileChannel channel) throws IOException {
    while (true) {
      checkCancellation();
      try {
        FileLock lock=channel.tryLock();
        if (lock != null) return lock;
      } catch (java.nio.channels.OverlappingFileLockException busyInJvm) {
        // A cooperating same-JVM writer is serialized by OUTPUT_LOCKS. A
        // provider can still report overlap while another lexical path is
        // resolving; poll so cancellation remains bounded.
      }
      try {
        Thread.sleep(25L);
      } catch (InterruptedException interrupted) {
        Thread.currentThread().interrupt();
        throw new DuckException("interrupted waiting for output lock", interrupted);
      }
    }
  }
  static void moveAtomically(Path source, Path target, AtomicMover mover) throws IOException {
    java.util.Objects.requireNonNull(source, "source is required");
    java.util.Objects.requireNonNull(target, "target is required");
    java.util.Objects.requireNonNull(mover, "atomic mover is required");
    // Deliberately do not downgrade to a non-atomic replacement: preserving an
    // existing complete output is more important than publishing on a filesystem
    // which cannot provide the documented atomicity guarantee.
    mover.move(source, target);
  }
}
