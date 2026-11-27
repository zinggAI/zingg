package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.zingg.duckdb.api.CompatibilityClock;
import io.zingg.duckdb.api.DuckException;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraphOutputTest {
  private static GraphOutput.Edge edge(long left, long right, double score) {
    return new GraphOutput.Edge(left, right, score);
  }

  @Test
  void chainAddsImpliedZeroScorePair() {
    var result = GraphOutput.transitiveEdges(List.of(edge(1, 2, .8), edge(2, 3, .7)));
    assertEquals(List.of(edge(1, 2, .8), edge(1, 3, 0), edge(2, 3, .7)), result);
  }

  @Test
  void disconnectedComponentsRemainSeparate() {
    var result = GraphOutput.transitiveEdges(List.of(edge(1, 2, .8), edge(7, 8, .4)));
    assertEquals(2, result.size());
    assertEquals(List.of(edge(1, 2, .8), edge(7, 8, .4)), result);
  }

  @Test
  void duplicateEdgesKeepMaximumScore() {
    var result = GraphOutput.transitiveEdges(List.of(edge(1, 2, .2), edge(2, 1, .9)));
    assertEquals(List.of(edge(1, 2, .9)), result);
  }

  @Test
  void starClosureAddsEveryMissingPairAtZeroScore() {
    var result = GraphOutput.transitiveEdges(List.of(edge(0, 1, .8), edge(0, 2, .6), edge(0, 3, .4)));
    assertEquals(List.of(edge(0, 1, .8), edge(0, 2, .6), edge(0, 3, .4),
        edge(1, 2, 0), edge(1, 3, 0), edge(2, 3, 0)), result);
  }

  @Test
  void denseGraphPreservesEveryDirectScoreAndZeroScore() {
    var result = GraphOutput.transitiveEdges(List.of(
        edge(1, 2, .8), edge(1, 3, 0), edge(2, 3, .4)));
    assertEquals(List.of(edge(1, 2, .8), edge(1, 3, 0), edge(2, 3, .4)), result);
  }

  @Test
  void selfEdgesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new GraphOutput.Edge(1, 1, .5));
  }

  @Test
  void transitivePairBudgetAllowsExactBoundaryAcrossComponents() {
    var edges = List.of(edge(1, 2, .8), edge(2, 3, .7), edge(10, 11, .6));
    var result = GraphOutput.transitiveEdges(edges, 4);
    assertEquals(4, result.size());
    assertEquals(4, GraphOutput.transitiveEdges(edges, 5).size());
  }

  @Test
  void transitivePairBudgetRejectsBeforeExpandingOversizedComponent() {
    var failure = assertThrows(IllegalArgumentException.class,
        () -> GraphOutput.transitiveEdges(List.of(edge(1, 2, .8), edge(2, 3, .7)), 2));
    assertEquals("transitive graph output pair budget exceeded: required more than 2", failure.getMessage());
  }

  @Test
  void transitivePairBudgetRejectsNegativeLimitsAndAllowsZeroForEmptyInput() {
    assertThrows(IllegalArgumentException.class, () -> GraphOutput.transitiveEdges(List.of(), -1));
    assertEquals(List.of(), GraphOutput.transitiveEdges(List.of(), 0));
  }

  @Test
  void transitivePairExpansionHonorsCancellationDuringTraversal() {
    var checks = new java.util.concurrent.atomic.AtomicInteger();
    var cancellation = (io.zingg.duckdb.api.CancellationToken) () -> checks.incrementAndGet() > 5;
    var failure = assertThrows(DuckException.class, () -> GraphOutput.transitiveEdges(
        List.of(edge(1, 2, .8), edge(2, 3, .7), edge(3, 4, .6)), 10, cancellation));
    assertEquals("operation cancelled", failure.getMessage());
  }

  @Test
  void defaultTransitivePairBudgetIsExplicitAndEnforced() {
    assertEquals(1_000_000L, GraphOutput.DEFAULT_MAX_TRANSITIVE_PAIRS);
    var edges = new java.util.ArrayList<GraphOutput.Edge>();
    for (long id = 1; id < 1_415; id++) edges.add(edge(id, id + 1, .5));
    assertThrows(IllegalArgumentException.class, () -> GraphOutput.transitiveEdges(edges));
  }

  @Test
  void entityScoresUseInjectedClusterTimestamp() {
    var clock = (CompatibilityClock) () -> 1700000000000L;
    var result = GraphOutput.entityScores(List.of(edge(1, 2, .8), edge(2, 3, .7)), clock);
    assertEquals(3, result.size());
    assertEquals("1700000000000:1", result.get(0).cluster());
    assertEquals(0d, result.get(0).min());
    assertEquals(.8d, result.get(0).max());
  }

  @Test
  void entityScoresHandlesAChainWithoutRepeatedEdgeScans() {
    var edges = new java.util.ArrayList<GraphOutput.Edge>();
    for (long id = 1; id < 301; id++) edges.add(edge(id, id + 1, .5));
    var result = GraphOutput.entityScores(edges, () -> 1700000000000L);
    assertEquals(301, result.size());
    assertEquals(0d, result.get(0).min());
    assertEquals(.5d, result.get(0).max());
  }

  @Test
  void entityScoringDoesNotExpandLargeSparseComponentToClique() {
    var edges = new java.util.ArrayList<GraphOutput.Edge>();
    for (long id = 1; id < 10_000; id++) edges.add(edge(id, id + 1, .5));
    var result = GraphOutput.entityScores(edges, () -> 1700000000000L);
    assertEquals(10_000, result.size());
    assertEquals(0d, result.get(0).min());
    assertEquals(.5d, result.get(0).max());
    assertEquals("1700000000000:1", result.get(result.size() - 1).cluster());
  }
}
