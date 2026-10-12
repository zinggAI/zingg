package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.*;
import io.zingg.duckdb.engine.*;
import java.nio.file.Path;
import java.util.*;

/** Phase-oriented compatibility orchestration without Spark in the worker. */
public final class ZinggJob implements AutoCloseable {
  public enum Phase { FIND_TRAINING_DATA, TRAIN, MATCH, LINK }
  private final CompatibilityRuntime runtime; private final JobHandle job;
  public ZinggJob(CompatibilityRuntime runtime){this(runtime,null);}
  ZinggJob(CompatibilityRuntime runtime,ResourceBudget budget){this.runtime=Objects.requireNonNull(runtime);this.job=budget==null?runtime.openJob():runtime.openJob(budget);}
  public Frame read(List<Path> inputs,Phase phase){return read(inputs,phase,null);}
  public Frame read(List<Path> inputs,Phase phase,io.zingg.duckdb.api.PathPolicy policy){
    var mode=phase==Phase.MATCH||phase==Phase.LINK
        ?DuckPipeReader.UnionMode.MATCH_BY_NAME:DuckPipeReader.UnionMode.TRAINING_POSITIONAL;
    Frame merged=new DuckPipeReader(job,policy,runtime.budget()).read(inputs,mode,phase!=Phase.LINK);
    return merged;
  }
  public Frame match(Frame input,String predicate){return input.filter(new DuckExpr(io.zingg.duckdb.engine.SqlSafety.predicate(predicate))).cache();}
  public Frame matchCandidates(Frame input,Matcher.MatchConfig config){return new Matcher().score(new Matcher().candidates(input,config),config).cache();}
  public Frame trainingData(Frame input,TrainingPlan.Config config){return new TrainingPlan().prepare(input,config);}
  public NativeTrainingService.Result trainNative(Frame input,NativeTrainingConfig config){return new NativeTrainingService().train(input,config);}
  public NativeClassifierTrainer.Result trainClassifier(Frame input,NativeClassifierTrainer.Config config){return new NativeClassifierTrainer().train(input,config);}
  public NativeClassifierTrainer.TrainingDiagnostics trainClassifierWithDiagnostics(
      Frame input,NativeClassifierTrainer.Config config){return new NativeClassifierTrainer().trainWithDiagnostics(input,config);}
  public List<GraphOutput.EntityScore> graphEntities(Collection<GraphOutput.Edge> edges){return GraphOutput.entityScores(edges,runtime.clock());}
  public List<GraphOutput.Edge> transitiveGraphEdges(Collection<GraphOutput.Edge> edges){return GraphOutput.transitiveEdges(edges);}
  public List<GraphOutput.Edge> transitiveGraphEdges(
      Collection<GraphOutput.Edge> edges,long maxOutputPairs,CancellationToken cancellation){
    return GraphOutput.transitiveEdges(edges,maxOutputPairs,cancellation);
  }
  public Object hash(String function,String value){return runtime.hashes().apply(function,value);}
  public double similarity(String function,String left,String right){return runtime.similarities().apply(function,left,right);}
  public LabelDecisionProvider.LabelBatch getPendingLabels(LabelDecisionProvider.LabelRequest request){return runtime.labels().getPendingLabels(request);}
  public LabelDecisionProvider.ApplyLabelsResult applyLabels(LabelDecisionProvider.ApplyLabelsRequest request){return runtime.labels().applyLabels(request);}
  public List<LinkOutput.Link> linkOutput(Collection<LinkOutput.Link> links,boolean asymmetric){return asymmetric?LinkOutput.asymmetric(links):LinkOutput.preserveRight(links);}
  public Frame executePhase(ZinggJob.Phase phase,Frame input){return runtime.phases().execute(phase,this,input).cache();}
  Frame sql(String sql){return job.sql(sql);}
  String schemaName(){return ((io.zingg.duckdb.engine.DuckJob) job).schemaName();}
  Frame createFrame(String table, List<String> columns, List<String> types, List<List<Object>> rows) {
    if (!(job instanceof io.zingg.duckdb.engine.DuckJob duckJob))
      throw new DuckException("native LINK requires the DuckDB job implementation");
    return duckJob.createFrame(table, columns, types, rows);
  }
  public long count(Frame frame){return frame.count();}
  public JobId id(){return job.id();}
  public void close(){job.close();}
}
