package io.zingg.duckdb.compat;

import java.nio.file.Path;

/** Child JVM for deterministic hard-stop tests; never included in runtime artifacts. */
public final class PersistentLabelCrashProbe {
  private PersistentLabelCrashProbe() {}

  public static void main(String[] args) {
    if (args.length != 3) throw new IllegalArgumentException("root, stage, and operation are required");
    var stage = PersistentLabelDecisionProvider.SnapshotWriteStage.valueOf(args[1]);
    var provider = new PersistentLabelDecisionProvider(Path.of(args[0]), point -> {
      if (point == stage) Runtime.getRuntime().halt(73);
    });
    var label = new LabelDecisionProvider.PendingLabel("crash-left", "crash-right", null, null);
    if (args[2].equals("enqueue")) {
      provider.enqueueIdempotent("v1", "crash-producer", label);
    } else if (args[2].equals("delivery")) {
      provider.getPendingLabels(new LabelDecisionProvider.LabelRequest(
          "crash-request", "v1", "crash-delivery", 1));
    } else {
      throw new IllegalArgumentException("unknown operation");
    }
    throw new AssertionError("probe did not halt at requested write stage");
  }
}
