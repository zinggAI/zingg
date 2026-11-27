package io.zingg.duckdb.compat;

import java.util.List;

/** Language-neutral boundary for human or external match decisions. */
public interface LabelDecisionProvider {
  LabelBatch getPendingLabels(LabelRequest request);
  ApplyLabelsResult applyLabels(ApplyLabelsRequest request);
  /** Definitive accepted pair decisions available for resumed training. */
  default List<LabelDecision> acceptedDecisions() {
    throw new UnsupportedOperationException("label provider does not support decision recovery");
  }

  record LabelRequest(String requestId, String schemaVersion, String idempotencyKey, int limit) {
    public LabelRequest {
      require(requestId, "requestId"); require(schemaVersion, "schemaVersion"); require(idempotencyKey, "idempotencyKey");
      if (limit < 1 || limit > 100_000) throw new IllegalArgumentException("limit must be between 1 and 100000");
    }
  }

  record ApplyLabelsRequest(String requestId, String schemaVersion, String idempotencyKey, List<LabelDecision> decisions) {
    public ApplyLabelsRequest {
      require(requestId, "requestId"); require(schemaVersion, "schemaVersion"); require(idempotencyKey, "idempotencyKey");
      decisions = List.copyOf(decisions == null ? List.of() : decisions);
      if (decisions.size() > 100_000) throw new IllegalArgumentException("too many decisions");
      var pairs = new java.util.HashSet<PairKey>();
      for (var decision : decisions) {
        if (!pairs.add(new PairKey(decision.leftId(), decision.rightId())))
          throw new IllegalArgumentException("duplicate pair decision in request");
      }
    }
  }

  record LabelDecision(String leftId, String rightId, Decision decision, String source) {
    public LabelDecision {
      require(leftId, "leftId"); require(rightId, "rightId"); require(source, "source");
      if (decision == null) throw new IllegalArgumentException("decision is required");
    }
  }

  record LabelBatch(String requestId, String schemaVersion, List<PendingLabel> labels, boolean hasMore) {
    public LabelBatch { require(requestId, "requestId"); require(schemaVersion, "schemaVersion"); labels = List.copyOf(labels == null ? List.of() : labels); }
  }

  record PendingLabel(String leftId, String rightId, String leftPayload, String rightPayload) {
    public PendingLabel { require(leftId, "leftId"); require(rightId, "rightId"); }
  }

  record ApplyLabelsResult(String requestId, String idempotencyKey, int applied, List<LabelRejection> rejections, boolean replay) {
    public ApplyLabelsResult { require(requestId, "requestId"); require(idempotencyKey, "idempotencyKey"); rejections = List.copyOf(rejections == null ? List.of() : rejections); }
  }

  record LabelRejection(String leftId, String rightId, String code, String message) {
    public LabelRejection { require(leftId, "leftId"); require(rightId, "rightId"); require(code, "code"); require(message, "message"); }
  }

  enum Decision { MATCH, NON_MATCH, UNKNOWN }

  record PairKey(String leftId, String rightId) {}

  private static void require(String value, String name) { if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required"); }
}
