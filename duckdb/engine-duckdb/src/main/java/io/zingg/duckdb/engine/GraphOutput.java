package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.CompatibilityClock;
import io.zingg.duckdb.api.CancellationToken;
import java.util.*;

/** In-memory output shaping for the released graph contract; SQL integration is supplied by the orchestrator. */
public final class GraphOutput {
  /** Default upper bound for the number of materialized undirected graph pairs. */
  public static final long DEFAULT_MAX_TRANSITIVE_PAIRS = 1_000_000L;
  public record Edge(long left,long right,double score) { public Edge { if(left==right||!Double.isFinite(score)) throw new IllegalArgumentException("invalid graph edge"); } }
  public record EntityScore(long id,double min,double max,String cluster) {}
  private GraphOutput() {}
  /**
   * Returns the undirected component closure, adding zero-score implied pairs.
   * The default budget limits the materialized result to one million pairs.
   */
  public static List<Edge> transitiveEdges(Collection<Edge> edges) {
    return transitiveEdges(edges, DEFAULT_MAX_TRANSITIVE_PAIRS);
  }

  /**
   * Returns the undirected component closure, adding zero-score implied pairs,
   * or rejects before result materialization when the pair budget is exceeded.
   * Input graph storage is still proportional to the supplied direct edges.
   */
  public static List<Edge> transitiveEdges(Collection<Edge> edges, long maxOutputPairs) {
    return transitiveEdges(edges, maxOutputPairs, CancellationToken.none());
  }

  /** Cancellation-aware bounded pair expansion. No partial result is returned. */
  public static List<Edge> transitiveEdges(
      Collection<Edge> edges, long maxOutputPairs, CancellationToken cancellation) {
    if (edges == null) throw new IllegalArgumentException("edges are required");
    if (maxOutputPairs < 0) throw new IllegalArgumentException("maxOutputPairs must be non-negative");
    if (cancellation == null) throw new IllegalArgumentException("cancellation token is required");
    cancellation.throwIfCancelled();
    Map<Long,Set<Long>> graph = new HashMap<>();
    Map<String,Double> direct = new HashMap<>();
    for (Edge e : edges) {
      cancellation.throwIfCancelled();
      if (e == null || e.left() == e.right() || !Double.isFinite(e.score())) throw new IllegalArgumentException("invalid graph edge");
      graph.computeIfAbsent(e.left(), k -> new TreeSet<>()).add(e.right());
      graph.computeIfAbsent(e.right(), k -> new TreeSet<>()).add(e.left());
      direct.merge(pairKey(e.left(), e.right()), e.score(), Math::max);
    }
    // Establish the required output size before allocating the quadratic result.
    long requiredPairs = 0;
    Set<Long> counted = new HashSet<>();
    for (long start : new TreeSet<>(graph.keySet())) if (counted.add(start)) {
      cancellation.throwIfCancelled();
      long componentSize = 0;
      Deque<Long> queue = new ArrayDeque<>(); queue.add(start);
      while (!queue.isEmpty()) {
        cancellation.throwIfCancelled();
        long id = queue.remove(); componentSize++;
        for (long n : graph.getOrDefault(id, Set.of())) if (counted.add(n)) queue.add(n);
      }
      long componentPairs = componentSize < 2 ? 0 : (componentSize % 2 == 0
          ? (componentSize / 2) * (componentSize - 1)
          : componentSize * ((componentSize - 1) / 2));
      if (componentPairs > maxOutputPairs - requiredPairs) {
        throw new IllegalArgumentException("transitive graph output pair budget exceeded: required more than " + maxOutputPairs);
      }
      requiredPairs += componentPairs;
    }
    if (requiredPairs > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("transitive graph output cannot fit in a Java List: " + requiredPairs + " pairs");
    }
    List<Edge> result = new ArrayList<>((int) requiredPairs);
    Set<Long> seen = new HashSet<>();
    long generatedPairs = 0;
    for (long start : new TreeSet<>(graph.keySet())) if (seen.add(start)) {
      cancellation.throwIfCancelled();
      TreeSet<Long> component = new TreeSet<>();
      Deque<Long> queue = new ArrayDeque<>(); queue.add(start);
      while (!queue.isEmpty()) { long id = queue.remove(); component.add(id); for (long n : graph.getOrDefault(id, Set.of())) if (seen.add(n)) queue.add(n); }
      List<Long> ids = new ArrayList<>(component);
      for (int i = 0; i < ids.size(); i++) {
        cancellation.throwIfCancelled();
        for (int j = i + 1; j < ids.size(); j++) {
          if ((++generatedPairs & 1023L) == 0) cancellation.throwIfCancelled();
          long left = ids.get(i), right = ids.get(j);
          result.add(new Edge(left, right, direct.getOrDefault(pairKey(left, right), 0d)));
        }
      }
    }
    return List.copyOf(result);
  }
  public static List<EntityScore> entityScores(Collection<Edge> edges, CompatibilityClock clock) {
    if (edges == null || clock == null) throw new IllegalArgumentException("edges and clock are required");
    // Keep only observed evidence edges. transitiveEdges() is intentionally a
    // pair-expansion API and can be quadratic; entity summaries need not build
    // that clique merely to account for absent pairs as score zero.
    Map<Long,Map<Long,Double>> graph=new HashMap<>();
    for (Edge e:edges) {
      if (e == null || e.left() == e.right() || !Double.isFinite(e.score())) throw new IllegalArgumentException("invalid graph edge");
      graph.computeIfAbsent(e.left(),k->new HashMap<>()).merge(e.right(),e.score(),Math::max);
      graph.computeIfAbsent(e.right(),k->new HashMap<>()).merge(e.left(),e.score(),Math::max);
    }
    List<EntityScore> out=new ArrayList<>(); Set<Long> seen=new HashSet<>(); long stamp=clock.epochMillis();
    for(long start:new TreeSet<>(graph.keySet())) if(seen.add(start)){
      Set<Long> component=new TreeSet<>(); Deque<Long> q=new ArrayDeque<>(); q.add(start);
      while(!q.isEmpty()){
        long x=q.remove(); component.add(x);
        for(long n:new TreeSet<>(graph.getOrDefault(x,Map.of()).keySet())) if(seen.add(n)) q.add(n);
      }
      long componentId=component.iterator().next();
      for(long id:component){
        Map<Long,Double> direct=graph.get(id);
        double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;
        for(double score:direct.values()){min=Math.min(min,score);max=Math.max(max,score);}
        if(direct.size()<component.size()-1){min=Math.min(min,0d);max=Math.max(max,0d);}
        if(min==Double.POSITIVE_INFINITY)min=max=0d;
        out.add(new EntityScore(id,min,max,stamp+":"+componentId));
      }
    }
    return out;
  }
  private static String pairKey(long a,long b){return Math.min(a,b)+":"+Math.max(a,b);}
}
