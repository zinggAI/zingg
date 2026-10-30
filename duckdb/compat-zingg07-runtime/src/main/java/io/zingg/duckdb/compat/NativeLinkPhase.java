package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.Row;
import io.zingg.duckdb.api.CompatibilityClock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.TreeMap;

/**
 * Native graph closure for already-scored entity pairs. This is an explicit
 * backend profile; it is not a claim of Zingg v0.7 LINK output parity.
 */
public final class NativeLinkPhase {
  public static final int MAX_NATIVE_ENTITIES = 1_000_000;
  public static final int MAX_NATIVE_INPUT_EDGES = 1_000_000;
  public static final String LEFT_ID = "z_zid";
  public static final String RIGHT_ID = "z_z_zid";
  public static final String SCORE = "z_score";
  public static final String ENTITY_ID = "z_zid";
  public static final String MIN_SCORE = "z_minScore";
  public static final String MAX_SCORE = "z_maxScore";
  public static final String CLUSTER_ID = "z_cluster";

  public Frame execute(ZinggJob job, Frame scoredPairs, CompatibilityClock clock) {
    Objects.requireNonNull(job, "job is required");
    Objects.requireNonNull(scoredPairs, "scored pairs are required");
    Objects.requireNonNull(clock, "clock is required");
    List<String> columns = scoredPairs.columns();
    for (String required : List.of(LEFT_ID, RIGHT_ID, SCORE)) {
      if (!columns.contains(required)) throw new DuckException("LINK input column is required: " + required);
    }

    long inputRows = scoredPairs.count(); // Enforces the configured input row budget before collecting the graph.
    if (inputRows > MAX_NATIVE_INPUT_EDGES)
      throw new DuckException("LINK input exceeds the native edge safety limit: " + MAX_NATIVE_INPUT_EDGES);
    Map<PairKey, PairScore> uniqueEdges = new TreeMap<>();
    try {
      for (Row row : scoredPairs.collect()) {
        Object left = row.get(LEFT_ID);
        Object right = row.get(RIGHT_ID);
        Object score = row.get(SCORE);
        if (left == null || right == null || !(score instanceof Number numericScore)) {
          throw new DuckException("LINK pair IDs must be non-null and score must be numeric");
        }
        String leftId = left.toString();
        String rightId = right.toString();
        if (leftId.equals(rightId)) continue;
        String low = leftId.compareTo(rightId) < 0 ? leftId : rightId;
        String high = leftId.compareTo(rightId) < 0 ? rightId : leftId;
        PairKey key = pairKey(low, high);
        double scoreValue = numericScore.doubleValue();
        if (!Double.isFinite(scoreValue)) throw new DuckException("LINK score must be finite");
        uniqueEdges.merge(key, new PairScore(low, high, scoreValue),
            (previous, current) -> previous.score() >= current.score() ? previous : current);
      }
    } catch (DuckException e) {
      throw e;
    } catch (RuntimeException e) {
      throw new DuckException("invalid LINK input rows", e);
    }

    List<EntityScore> entities = entityScores(uniqueEdges.values(), clock.epochMillis());
    if (entities.size() > MAX_NATIVE_ENTITIES)
      throw new DuckException("LINK entity output exceeds the native safety limit: " + MAX_NATIVE_ENTITIES);
    List<List<Object>> rows = new ArrayList<>(entities.size());
    for (EntityScore entity : entities) {
      rows.add(List.of(entity.id(), entity.min(), entity.max(), entity.cluster()));
    }
    return job.createFrame("native_link_" + java.util.UUID.randomUUID().toString().replace("-", ""),
        List.of(ENTITY_ID, MIN_SCORE, MAX_SCORE, CLUSTER_ID),
        List.of("VARCHAR", "DOUBLE", "DOUBLE", "VARCHAR"), rows);
  }

  private static List<EntityScore> entityScores(java.util.Collection<PairScore> pairs, long stamp) {
    Map<String, java.util.Set<String>> graph = new TreeMap<>();
    Map<PairKey, Double> scores = new TreeMap<>();
    for (PairScore pair : pairs) {
      graph.computeIfAbsent(pair.left(), ignored -> new java.util.TreeSet<>()).add(pair.right());
      graph.computeIfAbsent(pair.right(), ignored -> new java.util.TreeSet<>()).add(pair.left());
      scores.merge(pairKey(pair.left(), pair.right()), pair.score(), Math::max);
    }
    List<EntityScore> result = new ArrayList<>();
    java.util.Set<String> visited = new java.util.HashSet<>();
    for (String start : graph.keySet()) {
      if (!visited.add(start)) continue;
      java.util.Set<String> component = new java.util.TreeSet<>();
      java.util.ArrayDeque<String> queue = new java.util.ArrayDeque<>();
      queue.add(start);
      while (!queue.isEmpty()) {
        String id = queue.remove();
        component.add(id);
        for (String neighbor : graph.getOrDefault(id, java.util.Set.of())) {
          if (visited.add(neighbor)) queue.add(neighbor);
        }
      }
      String cluster = stamp + ":" + component.iterator().next();
      boolean complete = component.size() <= 1 || component.stream()
          .allMatch(id -> graph.getOrDefault(id, java.util.Set.of()).size() == component.size() - 1);
      for (String id : component) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (String other : graph.getOrDefault(id, java.util.Set.of())) {
          double score = scores.get(pairKey(id, other));
          min = Math.min(min, score);
          max = Math.max(max, score);
        }
        if (!complete) {
          min = Math.min(min, 0d);
          max = Math.max(max, 0d);
        }
        result.add(new EntityScore(id, min == Double.POSITIVE_INFINITY ? 0d : min,
            max == Double.NEGATIVE_INFINITY ? 0d : max, cluster));
      }
    }
    return List.copyOf(result);
  }

  private static PairKey pairKey(String left, String right) {
    return left.compareTo(right) < 0 ? new PairKey(left, right) : new PairKey(right, left);
  }

  private record PairKey(String left, String right) implements Comparable<PairKey> {
    @Override
    public int compareTo(PairKey other) {
      int first = left.compareTo(other.left);
      return first != 0 ? first : right.compareTo(other.right);
    }
  }

  private record PairScore(String left, String right, double score) {}
  private record EntityScore(String id, double min, double max, String cluster) {}

}
