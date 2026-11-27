package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.model.ModelReader;
import io.zingg.duckdb.engine.DuckExpr;
import io.zingg.duckdb.engine.DuckFrame;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Explicit lifecycle for a persisted native workflow.  The state machine is
 * intentionally independent of a worker process so a caller can close and
 * recreate the job between training and matching.
 */
public final class PersistedMatchWorkflow implements AutoCloseable {
  public enum State { CREATED, TRAINING_DATA_READY, LABELS_APPLIED, MODEL_PERSISTED, RESTARTED, MATCHED, CLOSED }
  private final CompatibilityRuntime runtime;
  private ZinggJob job;
  private State state = State.CREATED;
  private Path model;
  private LinearClassifier classifier;
  private Frame trainingData;
  private int appliedLabels;
  private final Map<PairKey, LabelDecisionProvider.LabelDecision> decisions = new LinkedHashMap<>();
  private record PairKey(String leftId, String rightId) {}

  public PersistedMatchWorkflow(CompatibilityRuntime runtime) {
    this.runtime = Objects.requireNonNull(runtime, "runtime");
    this.job = new ZinggJob(runtime);
  }

  public Frame read(List<Path> inputs, ZinggJob.Phase phase, io.zingg.duckdb.api.PathPolicy policy) {
    requireOpen();
    return job.read(inputs, phase, policy);
  }

  public Frame findTrainingData(Frame input, TrainingPlan.Config config) {
    requireOpen();
    trainingData = job.trainingData(Objects.requireNonNull(input, "input"), config);
    state = State.TRAINING_DATA_READY;
    return trainingData;
  }

  public LabelDecisionProvider.ApplyLabelsResult applyLabels(LabelDecisionProvider.ApplyLabelsRequest request) {
    requireState(State.TRAINING_DATA_READY, State.LABELS_APPLIED);
    var result = job.applyLabels(request);
    if (!result.replay()) {
      appliedLabels += result.applied();
      for (var decision : request.decisions()) {
        boolean rejected = result.rejections().stream().anyMatch(r ->
            r.leftId().equals(decision.leftId()) && r.rightId().equals(decision.rightId()));
        if (!rejected && decision.decision() != LabelDecisionProvider.Decision.UNKNOWN)
          decisions.putIfAbsent(new PairKey(decision.leftId(), decision.rightId()), decision);
      }
    }
    state = State.LABELS_APPLIED;
    return result;
  }

  /** Resume training from definitive decisions recovered by the configured provider. */
  public void restoreAppliedLabels() {
    requireState(State.TRAINING_DATA_READY);
    for (var decision : runtime.labels().acceptedDecisions()) {
      if (decision.decision() == LabelDecisionProvider.Decision.UNKNOWN) continue;
      decisions.put(new PairKey(decision.leftId(), decision.rightId()), decision);
    }
    appliedLabels = decisions.size();
    if (decisions.isEmpty()) throw new DuckException("no applied labels are available for recovery");
    state = State.LABELS_APPLIED;
  }

  public NativeTrainingService.Result trainMatch(Frame input, NativeTrainingConfig config) {
    requireState(State.LABELS_APPLIED, State.TRAINING_DATA_READY);
    var result = job.trainNative(Objects.requireNonNull(input, "input"), config);
    model = result.artifactDirectory();
    classifier = null; // A blocking histogram is not a scoring model.
    state = State.MODEL_PERSISTED;
    return result;
  }

  /** Derive binary training labels from accepted pair decisions, never from caller-supplied labels. */
  public NativeClassifierTrainer.Result trainClassifier(Frame pairFeatures, String leftIdColumn,
      String rightIdColumn, NativeClassifierTrainer.Config config) {
    requireState(State.LABELS_APPLIED);
    Objects.requireNonNull(pairFeatures, "pairFeatures");
    Objects.requireNonNull(config, "config");
    if (decisions.values().stream().noneMatch(d -> d.decision() == LabelDecisionProvider.Decision.MATCH)
        || decisions.values().stream().noneMatch(d -> d.decision() == LabelDecisionProvider.Decision.NON_MATCH))
      throw new DuckException("classifier training requires positive and negative applied labels");
    String leftPhysical = resolveColumn(pairFeatures.columns(), leftIdColumn);
    String rightPhysical = resolveColumn(pairFeatures.columns(), rightIdColumn);
    if (leftPhysical == null || rightPhysical == null)
      throw new DuckException("classifier pair ID columns are missing");
    if (resolveColumn(pairFeatures.columns(), config.labelColumn()) != null)
      throw new DuckException("classifier label column must come from applied decisions");
    var rows = new ArrayList<List<Object>>(decisions.size());
    for (var decision : decisions.values()) {
      rows.add(List.of(decision.leftId(), decision.rightId(),
          decision.decision() == LabelDecisionProvider.Decision.MATCH ? 1L : 0L));
    }
    // Keep the label mapping relational.  A generated CASE expression grows
    // with every decision, defeats the SQL planner, and can exceed parser or
    // protocol limits.  The job-owned mapping table is bounded by the same
    // decision count and is dropped with the job.
    if (!(pairFeatures instanceof DuckFrame source))
      throw new DuckException("classifier pair features require the DuckDB frame implementation");
    Frame labeled = source.withDecisionMapping(leftPhysical, rightPhysical, config.labelColumn(), rows);
    long labeledRows = labeled.count();
    if (labeledRows != decisions.size())
      throw new DuckException("classifier labeled pairs do not match applied decisions exactly");
    if (labeledRows > config.maxRows())
      throw new DuckException("classifier maxRows would discard applied decisions");
    var result = job.trainClassifier(labeled, config);
    model = result.artifactDirectory();
    classifier = loadClassifier(model);
    state = State.MODEL_PERSISTED;
    return result;
  }

  /** Closes the current job and reopens a fresh job against the same runtime. */
  public void restart() {
    requireOpen();
    if (model == null) throw new DuckException("cannot restart before a model is persisted");
    LinearClassifier reloaded = loadClassifier(model);
    job.close();
    job = new ZinggJob(runtime);
    classifier = reloaded;
    state = State.RESTARTED;
  }

  /** Load a classifier into a newly constructed workflow/runtime after a process restart. */
  public void restartFromModel(Path artifact) {
    requireState(State.CREATED);
    LinearClassifier reloaded = loadClassifier(Objects.requireNonNull(artifact, "artifact"));
    model = artifact.toAbsolutePath().normalize();
    classifier = reloaded;
    state = State.RESTARTED;
  }

  public Frame match(Frame input, Matcher.MatchConfig config) {
    requireState(State.RESTARTED, State.MODEL_PERSISTED, State.MATCHED);
    if (classifier == null) throw new DuckException("persisted model is not a classifier");
    var matcher = new Matcher();
    Frame output = matcher.scoreLinear(matcher.candidates(Objects.requireNonNull(input, "input"),
        Objects.requireNonNull(config, "config")), config, classifier).cache();
    state = State.MATCHED;
    return output;
  }

  private static LinearClassifier loadClassifier(Path artifact) {
    try { return (LinearClassifier) new ModelScorerRegistry().create(ModelReader.load(artifact, 0)); }
    catch (IOException e) { throw new DuckException("persisted classifier cannot be reloaded", e); }
  }
  private static String quote(String identifier) { return "\"" + identifier.replace("\"", "\"\"") + "\""; }
  private static String resolveColumn(List<String> columns, String requested) {
    if (requested == null || requested.isBlank()) return null;
    String match = null;
    for (String column : columns) {
      if (column.equalsIgnoreCase(requested)) {
        if (match != null && !match.equals(column))
          throw new DuckException("ambiguous classifier column: " + requested);
        match = column;
      }
    }
    return match;
  }

  public State state() { return state; }
  public Path model() { return model; }
  public int appliedLabels() { return appliedLabels; }

  @Override public void close() {
    if (state != State.CLOSED) { job.close(); state = State.CLOSED; }
  }

  private void requireOpen() { if (state == State.CLOSED) throw new DuckException("workflow is closed"); }
  private void requireState(State... allowed) {
    requireOpen();
    for (State candidate : allowed) if (candidate == state) return;
    throw new DuckException("invalid workflow state: " + state);
  }
}
