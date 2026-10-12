package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryLabelDecisionProviderTest {
  @Test
  void exactReplayIsIdempotentButChangedPayloadIsRejected() {
    var provider = new InMemoryLabelDecisionProvider();
    var match = decision(LabelDecisionProvider.Decision.MATCH);
    var first = provider.applyLabels(request("first", "key", match));
    assertFalse(first.replay());
    assertEquals(1, first.applied());
    var replay = provider.applyLabels(request("retry", "key", match));
    assertTrue(replay.replay());
    assertEquals("retry", replay.requestId());
    assertThrows(DuckException.class, () -> provider.applyLabels(
        request("changed", "key", decision(LabelDecisionProvider.Decision.NON_MATCH))));
    assertThrows(DuckException.class, () -> provider.applyLabels(
        new LabelDecisionProvider.ApplyLabelsRequest("version", "v2", "key", List.of(match))));
    var conflict = provider.applyLabels(request("conflict", "different-key",
        decision(LabelDecisionProvider.Decision.NON_MATCH)));
    assertEquals(0, conflict.applied());
    assertEquals("CONFLICT", conflict.rejections().get(0).code());
  }

  @Test
  void unknownDecisionDoesNotBlockLaterHumanResolution() {
    var provider = new InMemoryLabelDecisionProvider();
    assertEquals(1, provider.applyLabels(request("unknown", "one",
        decision(LabelDecisionProvider.Decision.UNKNOWN))).applied());
    var resolved = provider.applyLabels(request("resolved", "two",
        decision(LabelDecisionProvider.Decision.MATCH)));
    assertEquals(1, resolved.applied());
    assertTrue(resolved.rejections().isEmpty());
  }

  @Test
  void pairIdsContainingNulCannotCollideWhenIndexed() {
    var provider = new InMemoryLabelDecisionProvider();
    var first = new LabelDecisionProvider.LabelDecision("a\u0000b", "c",
        LabelDecisionProvider.Decision.MATCH, "human");
    var second = new LabelDecisionProvider.LabelDecision("a", "b\u0000c",
        LabelDecisionProvider.Decision.NON_MATCH, "human");

    var result = provider.applyLabels(new LabelDecisionProvider.ApplyLabelsRequest(
        "nul-pairs", "v1", "nul-pairs", List.of(first, second)));

    assertEquals(2, result.applied());
    assertTrue(result.rejections().isEmpty());
    assertEquals(List.of(first, second), provider.acceptedDecisions());
  }

  @Test
  void duplicatePairWithinOneRequestIsRejectedBeforeProviderMutation() {
    var provider = new InMemoryLabelDecisionProvider();
    var match = decision(LabelDecisionProvider.Decision.MATCH);
    var nonMatch = decision(LabelDecisionProvider.Decision.NON_MATCH);

    assertThrows(IllegalArgumentException.class, () -> new LabelDecisionProvider.ApplyLabelsRequest(
        "duplicate-pair", "v1", "duplicate-pair", List.of(match, nonMatch)));
    assertTrue(provider.acceptedDecisions().isEmpty());
    assertEquals(1, provider.applyLabels(request("valid", "valid", match)).applied());
  }

  private static LabelDecisionProvider.LabelDecision decision(LabelDecisionProvider.Decision value) {
    return new LabelDecisionProvider.LabelDecision("left", "right", value, "human");
  }

  private static LabelDecisionProvider.ApplyLabelsRequest request(String requestId, String key,
      LabelDecisionProvider.LabelDecision decision) {
    return new LabelDecisionProvider.ApplyLabelsRequest(requestId, "v1", key, List.of(decision));
  }
}
