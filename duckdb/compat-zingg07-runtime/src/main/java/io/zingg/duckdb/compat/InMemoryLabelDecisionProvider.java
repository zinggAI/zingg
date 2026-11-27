package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import java.util.*;

/** Bounded reference provider for local/offline orchestration and adapters. */
public final class InMemoryLabelDecisionProvider implements LabelDecisionProvider {
  private final Queue<PendingLabel> pending = new ArrayDeque<>();
  private final Map<String, AppliedRequest> applied = new HashMap<>();
  private final Map<PairKey, LabelDecision> decisions = new LinkedHashMap<>();
  private record PairKey(String leftId, String rightId) {}
  private record AppliedRequest(String schemaVersion, List<LabelDecision> decisions, ApplyLabelsResult result) {}

  public synchronized void enqueue(PendingLabel label) { pending.add(Objects.requireNonNull(label)); }

  @Override public synchronized LabelBatch getPendingLabels(LabelRequest request) {
    List<PendingLabel> result = new ArrayList<>(Math.min(request.limit(), pending.size()));
    while (result.size() < request.limit() && !pending.isEmpty()) result.add(pending.remove());
    return new LabelBatch(request.requestId(), request.schemaVersion(), result, !pending.isEmpty());
  }

  @Override public synchronized ApplyLabelsResult applyLabels(ApplyLabelsRequest request) {
    AppliedRequest previous = applied.get(request.idempotencyKey());
    if (previous != null) {
      if (!previous.schemaVersion().equals(request.schemaVersion()) || !previous.decisions().equals(request.decisions()))
        throw new DuckException("idempotency key reused with different label payload");
      ApplyLabelsResult result = previous.result();
      return new ApplyLabelsResult(request.requestId(), request.idempotencyKey(), result.applied(), result.rejections(), true);
    }
    int accepted = 0; List<LabelRejection> rejected = new ArrayList<>();
    for (LabelDecision decision : request.decisions()) {
      if (decision.decision() == Decision.UNKNOWN) { accepted++; continue; }
      PairKey key = new PairKey(decision.leftId(), decision.rightId());
      LabelDecision old = decisions.putIfAbsent(key, decision);
      if (old == null || old.decision() == decision.decision()) accepted++;
      else rejected.add(new LabelRejection(decision.leftId(), decision.rightId(), "CONFLICT", "pair already has a different decision"));
    }
    ApplyLabelsResult result = new ApplyLabelsResult(request.requestId(), request.idempotencyKey(), accepted, rejected, false);
    applied.put(request.idempotencyKey(), new AppliedRequest(request.schemaVersion(), request.decisions(), result));
    return result;
  }

  @Override public synchronized List<LabelDecision> acceptedDecisions() {
    return List.copyOf(decisions.values());
  }
}
