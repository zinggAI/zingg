package io.zingg.duckdb.compat;
import java.util.*;
public final class LinkOutput {
 public record Link(long left,long right,double score){public Link{if(left==right||!Double.isFinite(score))throw new IllegalArgumentException("invalid link");}}
 private LinkOutput(){}
 /** Released compatibility behavior: left projection is distinct; right rows remain multiplicity-preserving. */
 public static List<Link> asymmetric(Collection<Link> links){if(links==null)throw new IllegalArgumentException("links are required");LinkedHashMap<Long,Link> left=new LinkedHashMap<>();for(Link link:links){if(link==null)throw new IllegalArgumentException("link is null");left.putIfAbsent(link.left(),link);}return List.copyOf(left.values());}
 public static List<Link> preserveRight(Collection<Link> links){if(links==null)throw new IllegalArgumentException("links are required");var out=new ArrayList<Link>(links.size());for(Link link:links){if(link==null)throw new IllegalArgumentException("link is null");out.add(link);}return List.copyOf(out);}
}
