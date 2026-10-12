package io.zingg.duckdb.worker;

import io.zingg.duckdb.api.PathPolicy;
import io.zingg.duckdb.compat.*;
import io.zingg.duckdb.engine.SqlSafety;
import io.zingg.duckdb.engine.DuckCancellation;
import io.zingg.duckdb.protocol.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/** Configured line-protocol server; diagnostics always retain the request id. */
final class ConfiguredWorkerServer implements AutoCloseable {
  private static final long SHUTDOWN_GRACE_MILLIS = 5_000L;
  private static final long SHUTDOWN_FORCE_MILLIS = 1_000L;
  private final CompatibilityRuntime runtime;
  private final PathPolicy paths;
  private final long maxModelBytes; private final long maxOutputBytes; private final long maxRows; private final long maxCollectBytes; private final long maxSpillBytes;
  private final boolean unsafeDebugSql;
  ConfiguredWorkerServer(CompatibilityRuntime runtime, PathPolicy paths, long maxModelBytes, long maxOutputBytes, long maxRows, long maxCollectBytes, long maxSpillBytes, boolean unsafeDebugSql) { this.runtime=runtime; this.paths=paths; this.maxModelBytes=maxModelBytes; this.maxOutputBytes=maxOutputBytes; this.maxRows=maxRows; this.maxCollectBytes=maxCollectBytes; this.maxSpillBytes=maxSpillBytes; this.unsafeDebugSql=unsafeDebugSql; }

  void serve(Reader input, Writer output) throws IOException {
    var in = new BufferedReader(input);
    var out = new PrintWriter(output, true);
    var writes = new Object();
    var executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<>(64), r -> {
      Thread thread = new Thread(r, "zingg-worker-request");
      thread.setDaemon(false);
      return thread;
    });
    Map<String, DuckCancellation> active = new ConcurrentHashMap<>();
    try {
      while (true) {
        String line;
        try { line = readBoundedLine(in); }
        catch (LineTooLongException e) {
          try { executor.execute(() -> respond(out, writes, "", "error", "worker message exceeds size limit")); }
          catch (RejectedExecutionException full) {
            respond(out, writes, "", "error", "worker request queue is full");
          }
          continue;
        }
        if (line == null) break;
        WorkerMessage message;
        try { message = ProtocolCodec.decode(line); }
        catch (RuntimeException invalid) {
          try { executor.execute(() -> respond(out, writes, "", "error", diagnostic(invalid))); }
          catch (RejectedExecutionException full) {
            respond(out, writes, "", "error", "worker request queue is full");
          }
          continue;
        }
        if (message.id().isBlank()) {
          try { executor.execute(() -> respond(out, writes, "", "error", "request id is required")); }
          catch (RejectedExecutionException full) {
            respond(out, writes, "", "error", "worker request queue is full");
          }
          continue;
        }
        if (message.operation().equals("cancel")) {
          DuckCancellation cancellation = active.get(message.payload());
          if (cancellation == null) respond(out, writes, message.id(), "ok", "not_active");
          else {
            cancellation.cancel();
            respond(out, writes, message.id(), "ok", "cancel_requested");
          }
          continue;
        }
        if (message.operation().equals("shutdown")) {
          stopRequests(executor, active);
          respond(out, writes, message.id(), "ok", "stopping");
          return;
        }
        DuckCancellation cancellation = new DuckCancellation();
        if (active.putIfAbsent(message.id(), cancellation) != null) {
          respond(out, writes, message.id(), "error", "request id is already active");
          continue;
        }
        try {
          executor.execute(() -> process(message, cancellation, active, out, writes));
        } catch (RejectedExecutionException rejected) {
          active.remove(message.id(), cancellation);
          respond(out, writes, message.id(), "error", "worker request queue is full");
        }
      }
    } finally {
      stopRequests(executor, active);
    }
  }

  private void process(WorkerMessage message, DuckCancellation cancellation,
      Map<String, DuckCancellation> active, PrintWriter out, Object writes) {
    String status = "ok";
    String result;
    try (var ignored = DuckCancellation.install(cancellation)) {
      cancellation.throwIfCancelled();
      result = execute(message);
    } catch (Exception failure) {
      status = "error";
      result = cancellation.isCancelled() ? "operation cancelled" : diagnostic(failure);
    } finally {
      active.remove(message.id(), cancellation);
    }
    respond(out, writes, message.id(), status, result);
  }

  private String execute(WorkerMessage message) {
    String requestId = message.id();
    return switch (message.operation()) {
      case "ping" -> "pong";
      case "status" -> status();
      case "count" -> {
        requireUnsafeDebugSql();
        try (var job = runtime.openJob()) {
          yield Long.toString(job.sql(SqlSafety.readOnlyQuery(message.payload())).count());
        }
      }
      case "explain" -> {
        requireUnsafeDebugSql();
        try (var job = runtime.openJob()) {
          yield job.sql(SqlSafety.readOnlyQuery(message.payload())).explain();
        }
      }
      case "run" -> run(message.payload());
      case "run_v2" -> runV2(message.payload());
      case "train_blocking" -> trainBlocking(message.payload());
      case "train_classifier" -> trainClassifier(message.payload());
      case "train_from_labels" -> trainFromLabels(message.payload());
      case "apply_labels" -> applyLabels(requestId, message.payload());
      case "enqueue_pending_label" -> enqueuePendingLabel(message.payload());
      case "get_pending_labels" -> getPendingLabels(requestId, message.payload());
      default -> throw new IllegalArgumentException("operation unavailable in configured server: "
          + message.operation());
    };
  }

  private static String diagnostic(Throwable failure) {
    return failure.getMessage() == null ? failure.getClass().getSimpleName() : failure.getMessage();
  }

  private static void respond(PrintWriter output, Object lock, String id, String status, String payload) {
    synchronized (lock) {
      output.println(ProtocolCodec.encode(new WorkerMessage(id, status, payload)));
    }
  }

  private static void stopRequests(ExecutorService executor,
      Map<String, DuckCancellation> active) {
    executor.shutdown();
    boolean interrupted = false;
    try {
      try {
        if (executor.awaitTermination(SHUTDOWN_GRACE_MILLIS, TimeUnit.MILLISECONDS)) return;
      } catch (InterruptedException e) {
        interrupted = true;
      }
      for (DuckCancellation cancellation : active.values()) cancellation.cancel();
      executor.shutdownNow();
      try {
        executor.awaitTermination(SHUTDOWN_FORCE_MILLIS, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        interrupted = true;
      }
    } finally {
      if (interrupted) Thread.currentThread().interrupt();
    }
  }

  private static String readBoundedLine(BufferedReader input) throws IOException, LineTooLongException {
    var line=new StringBuilder();
    int value;
    boolean sawCharacter=false;
    while((value=input.read())!=-1) {
      sawCharacter=true;
      if(value=='\n') {
        if(line.length()>0 && line.charAt(line.length()-1)=='\r') line.setLength(line.length()-1);
        if(line.length()>ProtocolCodec.MAX_LINE_CHARS) throw new LineTooLongException();
        return line.toString();
      }
      line.append((char)value);
      boolean possibleCrLf=line.length()==ProtocolCodec.MAX_LINE_CHARS+1 && value=='\r';
      if(line.length()>ProtocolCodec.MAX_LINE_CHARS && !possibleCrLf) {
        while(value!=-1 && value!='\n') value=input.read();
        throw new LineTooLongException();
      }
    }
    if(!sawCharacter) return null;
    if(line.length()>0 && line.charAt(line.length()-1)=='\r') line.setLength(line.length()-1);
    if(line.length()>ProtocolCodec.MAX_LINE_CHARS) throw new LineTooLongException();
    return line.toString();
  }

  private static final class LineTooLongException extends Exception {}

  private String status() {
    var d=runtime.diagnostics();
    var policy=runtime.connectorPolicy();
    return "memory_limit="+d.memoryLimit()+";max_temp_directory_size="+d.maxTempDirectorySize()+";temp_directory="+d.tempDirectory()+";threads="+d.threads()+";max_rows="+maxRows+";max_input_bytes="+runtime.budget().maxInputBytes()+";max_collect_bytes="+maxCollectBytes+";max_output_bytes="+maxOutputBytes+";max_spill_bytes="+maxSpillBytes+";heap_used_bytes="+d.heapUsedBytes()+";heap_max_bytes="+d.heapMaxBytes()+";process_resident_bytes="+d.processResidentBytes()+";temp_directory_used_bytes="+d.tempDirectoryUsedBytes()+";offline_mode="+policy.offlineMode()+";unsafe_debug_sql="+unsafeDebugSql+";allowed_extensions="+String.join(",",policy.allowedExtensions());
  }

  private void requireUnsafeDebugSql(){if(!unsafeDebugSql)throw new IllegalArgumentException("count/explain SQL is disabled; restart with --unsafe-debug-sql for diagnostics only");}

  private String run(String payload) {
    String[] p=PayloadCodec.decode(payload, -1);
    if(p.length<5) throw new IllegalArgumentException("run payload requires phase, output, predicate, model, and input fields");
    var inputs=Arrays.stream(p,4,p.length).filter(x->!x.isBlank()).map(Path::of).toList();
    Path model=!p[3].isBlank()?Path.of(p[3]):null;
    var config=new PipelineConfig(ZinggJob.Phase.valueOf(p[0]),inputs,Path.of(p[1]),p[2],paths,null,model,maxModelBytes,maxOutputBytes);
    return Long.toString(new ZinggPipeline(runtime).execute(config).outputRows());
  }

  private String runV2(String payload) {
    String[] p=PayloadCodec.decode(payload,-1);
    if(p.length<9) throw new IllegalArgumentException("run_v2 requires eight control fields and at least one input");
    var inputs=Arrays.stream(p,8,p.length).filter(x->!x.isBlank()).map(Path::of).toList();
    Path model=p[3].isBlank()?null:Path.of(p[3]);
    Matcher.MatchConfig matcher=p[4].isBlank()&&p[5].isBlank()?null:
        new Matcher.MatchConfig(p[4],p[5],p[6],Double.parseDouble(p[7]));
    var config=new PipelineConfig(ZinggJob.Phase.valueOf(p[0]),inputs,Path.of(p[1]),p[2],paths,
        matcher,model,maxModelBytes,maxOutputBytes);
    return Long.toString(new ZinggPipeline(runtime).execute(config).outputRows());
  }

  private String trainBlocking(String payload) {
    String[] p=PayloadCodec.decode(payload, -1);
    if(p.length<6) throw new IllegalArgumentException("train_blocking payload requires 5 control fields plus input fields");
    var inputs=Arrays.stream(p,5,p.length).filter(x->!x.isBlank()).map(Path::of).toList();
    try(var job=new ZinggJob(runtime)) {
      var frame=job.read(inputs,ZinggJob.Phase.TRAIN,paths);
      var config=new NativeTrainingConfig("zingg-0.7.0",p[2],p[3],List.of(),paths.output(Path.of(p[1])),Long.parseLong(p[4]),maxOutputBytes);
      return job.trainNative(frame,config).artifactDirectory().toString();
    }
  }
  private String trainClassifier(String payload) {
    String[] p=PayloadCodec.decode(payload, -1);
    if(p.length<10) throw new IllegalArgumentException("train_classifier payload requires 9 control fields plus input fields");
    var inputs=Arrays.stream(p,9,p.length).filter(x->!x.isBlank()).map(Path::of).toList();
    try(var job=new ZinggJob(runtime)) {
      var frame=job.read(inputs,ZinggJob.Phase.TRAIN,paths);
      var features=Arrays.stream(p[3].split(",",-1)).filter(x->!x.isBlank()).toList();
      var config=new NativeClassifierTrainer.Config(p[8],features,p[2],paths.output(Path.of(p[1])),Integer.parseInt(p[5]),Double.parseDouble(p[6]),Double.parseDouble(p[7]),Long.parseLong(p[4]),maxOutputBytes);
      return new NativeClassifierTrainer().train(frame,config).artifactDirectory().toString();
    }
  }
  private String applyLabels(String requestId,String payload) {
    String[] fields=PayloadCodec.decode(payload,-1);
    if(fields.length<2 || (fields.length-2)%4!=0 || (fields.length-2)/4>100_000)
      throw new IllegalArgumentException("apply_labels requires schema, idempotency key, and groups of four decision fields");
    var decisions=new java.util.ArrayList<LabelDecisionProvider.LabelDecision>();
    for(int i=2;i<fields.length;i+=4)
      decisions.add(new LabelDecisionProvider.LabelDecision(fields[i],fields[i+1],
          LabelDecisionProvider.Decision.valueOf(fields[i+2]),fields[i+3]));
    var applied=runtime.labels().applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
        requestId,fields[0],fields[1],decisions));
    var response=new java.util.ArrayList<String>();
    response.add(Integer.toString(applied.applied()));
    response.add(Boolean.toString(applied.replay()));
    response.add(Integer.toString(applied.rejections().size()));
    for(var rejection:applied.rejections()) {
      response.add(rejection.leftId()); response.add(rejection.rightId());
      response.add(rejection.code()); response.add(rejection.message());
    }
    return PayloadCodec.encode(response.toArray(String[]::new));
  }
  private PersistentLabelDecisionProvider persistentLabels() {
    if (runtime.labels() instanceof PersistentLabelDecisionProvider provider) return provider;
    throw new IllegalArgumentException("pending-label worker operations require --labels-root");
  }
  private String enqueuePendingLabel(String payload) {
    String[] fields=PayloadCodec.decode(payload,8);
    if(fields[5].length()>10_000 || fields[7].length()>10_000)
      throw new IllegalArgumentException("pending-label payload exceeds size limit");
    if(!fields[4].equals("true") && !fields[4].equals("false")
        || !fields[6].equals("true") && !fields[6].equals("false"))
      throw new IllegalArgumentException("pending-label presence flag is invalid");
    var label=new LabelDecisionProvider.PendingLabel(fields[2],fields[3],
        fields[4].equals("true")?fields[5]:null, fields[6].equals("true")?fields[7]:null);
    boolean replay=persistentLabels().enqueueIdempotent(fields[0],fields[1],label);
    return PayloadCodec.encode(Boolean.toString(replay));
  }
  private String getPendingLabels(String requestId,String payload) {
    String[] fields=PayloadCodec.decode(payload,3);
    int limit=Integer.parseInt(fields[2]);
    if(limit<1 || limit>10) throw new IllegalArgumentException("worker pending-label limit must be 1..10");
    var batch=persistentLabels().getPendingLabels(new LabelDecisionProvider.LabelRequest(
        requestId,fields[0],fields[1],limit));
    var response=new java.util.ArrayList<String>();
    response.add(Boolean.toString(batch.hasMore()));
    response.add(Integer.toString(batch.labels().size()));
    for(var label:batch.labels()) {
      response.add(label.leftId()); response.add(label.rightId());
      response.add(Boolean.toString(label.leftPayload()!=null));
      response.add(label.leftPayload()==null?"":label.leftPayload());
      response.add(Boolean.toString(label.rightPayload()!=null));
      response.add(label.rightPayload()==null?"":label.rightPayload());
    }
    String encoded=PayloadCodec.encode(response.toArray(String[]::new));
    if(encoded.length()>ProtocolCodec.MAX_LINE_CHARS-1024)
      throw new IllegalArgumentException("pending-label response exceeds size limit");
    return encoded;
  }
  private String trainFromLabels(String payload) {
    String[] p=PayloadCodec.decode(payload,-1);
    if(p.length<11) throw new IllegalArgumentException("train_from_labels requires ten control fields and at least one input");
    var inputs=Arrays.stream(p,10,p.length).filter(x->!x.isBlank()).map(Path::of).toList();
    long rowLimit=Long.parseLong(p[5]);
    var features=Arrays.stream(p[3].split(",",-1)).filter(x->!x.isBlank()).toList();
    var config=new NativeClassifierTrainer.Config(p[9],features,p[4],paths.output(Path.of(p[0])),
        Integer.parseInt(p[6]),Double.parseDouble(p[7]),Double.parseDouble(p[8]),rowLimit,maxOutputBytes);
    try(var workflow=new PersistedMatchWorkflow(runtime)) {
      var input=workflow.read(inputs,ZinggJob.Phase.FIND_TRAINING_DATA,paths);
      String quoted="\""+p[2].replace("\"","\"\"")+"\"";
      var training=workflow.findTrainingData(input,new TrainingPlan.Config(quoted,"z_training_block",rowLimit));
      workflow.restoreAppliedLabels();
      var candidates=new Matcher().candidates(training,
          new Matcher.MatchConfig(p[1],"z_training_block","1.0",0.5));
      return workflow.trainClassifier(candidates,p[1],"z_"+p[1],config).artifactDirectory().toString();
    }
  }
  public void close() {}
}
