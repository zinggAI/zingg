package io.zingg.duckdb.legacy;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

/** Isolated-process entry point. It intentionally depends on legacy classes only through reflection. */
public final class LegacyBlockingTreeImporterMain {
  private LegacyBlockingTreeImporterMain() {}
  public static void main(String[] args) throws Exception {
    Path input=requiredEnv("ZINGG_IMPORT_INPUT"), output=requiredEnv("ZINGG_IMPORT_OUTPUT");
    long maxBytes=Long.parseLong(requiredEnv("ZINGG_IMPORT_MAX_BYTES").toString()); int maxDepth=Integer.parseInt(requiredEnv("ZINGG_IMPORT_MAX_DEPTH").toString()); long maxRefs=Long.parseLong(requiredEnv("ZINGG_IMPORT_MAX_REFERENCES").toString());
    ImportLimits limits=new ImportLimits(maxBytes,maxDepth,maxRefs,Math.max(1,Long.parseLong(requiredEnv("ZINGG_IMPORT_TIMEOUT_MILLIS").toString())));
    ImportPreflight.validate(input,limits); Path binary=findBinary(input); importSerialized(binary,output,limits);
  }
  public static void importSerialized(Path binary,Path output,ImportLimits limits) throws Exception {
    if(binary==null||output==null||limits==null)throw new IllegalArgumentException("binary, output and limits are required");
    Object tree=read(binary,limits); Map<String,Object> payload=new LinkedHashMap<>(); payload.put("kind","native-blocking-tree"); payload.put("sourceClass",tree.getClass().getName());
    List<Map<String,Object>> nodes=new ArrayList<>(); walk(tree,nodes,new IdentityHashMap<>(),0,limits.maxDepth(),limits.maxReferences()); payload.put("nodes",nodes); payload.put("nodeCount",nodes.size());
    String json=toJson(payload); byte[] bytes=json.getBytes(StandardCharsets.UTF_8);
    ModelManifest manifest=new ModelManifest("zingg-0.7.0","0.7.0",ModelFormat.CURRENT,ModelType.BLOCKING_TREE.name(),List.of(),"");
    new ModelArtifactWriter(output).write(manifest,bytes,new ImportProvenance(binary.toString(),"0.7.0","zingg-duckdb-legacy-reflective-0.1",Instant.now(),""));
  }
  private static Object read(Path file,ImportLimits limits)throws Exception {
    try(InputStream raw=Files.newInputStream(file); CountingInputStream counted=new CountingInputStream(raw,limits.maxBytes()); ObjectInputStream in=new ObjectInputStream(counted)) {
      in.setObjectInputFilter(filter(limits)); return in.readObject();
    } catch(InvalidClassException|ClassNotFoundException e){throw new DuckException("legacy blocking tree class is not allowed or unavailable",e);}
    catch(IOException e){
      if(causedByByteLimit(e)) throw new DuckException("legacy serialized input byte limit exceeded", e);
      if(e instanceof EOFException || e instanceof StreamCorruptedException || e instanceof OptionalDataException)
        throw new DuckException("legacy serialized input is truncated or corrupt", e);
      throw e;
    }
  }
  private static boolean causedByByteLimit(Throwable failure){for(Throwable cause=failure;cause!=null;cause=cause.getCause())if("serialized input exceeds byte limit".equals(cause.getMessage()))return true;return false;}
  private static ObjectInputFilter filter(ImportLimits l){StringBuilder s=new StringBuilder("maxdepth=").append(l.maxDepth()).append(";maxrefs=").append(l.maxReferences()).append(";maxbytes=").append(l.maxBytes()).append(";java.base/*;java.util.*;");String allowed=System.getenv("ZINGG_IMPORT_ALLOWED_CLASSES");if(allowed!=null)for(String c:allowed.split("\\R"))if(c.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*"))s.append(c).append(";");s.append("zingg.common.core.block.*;zingg.common.client.*;zingg.common.core.hash.*;!*");return ObjectInputFilter.Config.createFilter(s.toString());}
  private static void walk(Object node,List<Map<String,Object>> out,IdentityHashMap<Object,Boolean> seen,int depth,int maxDepth,long maxRefs)throws Exception{if(node==null||seen.put(node,Boolean.TRUE)!=null)return;if(depth>maxDepth||out.size()>=maxRefs)throw new DuckException("legacy tree limits exceeded");Map<String,Object> n=new LinkedHashMap<>();n.put("class",node.getClass().getName());for(String field:List.of("function","context","hash","elimCount")){Object v=field(node,field);if(v!=null)n.put(field,String.valueOf(v));}Object head=field(node,"head");if(head!=null)n.put("headClass",head.getClass().getName());List<Object> children=list(field(node,"leafs"));n.put("children",children.size());out.add(n);for(Object child:children)walk(child,out,seen,depth+1,maxDepth,maxRefs);}
  private static Object field(Object o,String name)throws Exception{Class<?> c=o.getClass();while(c!=null)try{var f=c.getDeclaredField(name);f.setAccessible(true);return f.get(o);}catch(NoSuchFieldException e){c=c.getSuperclass();}return null;}
  @SuppressWarnings("unchecked") private static List<Object> list(Object value){return value instanceof List<?> l?(List<Object>)l:List.of();}
  private static Path findBinary(Path root)throws IOException{try(var stream=Files.walk(root)){return stream.filter(Files::isRegularFile).filter(p->p.getFileName().toString().matches("(?i)(model|tree).*\\.(bin|bytes|ser|data)$")).findFirst().orElseThrow(()->new DuckException("serialized blocking tree binary not found"));}}
  private static Path requiredEnv(String n){String v=System.getenv(n);if(v==null||v.isBlank())throw new DuckException("missing importer environment: "+n);return Path.of(v);}
  static String toJson(Object v){if(v==null)return "null";if(v instanceof String s)return ModelArtifactJson.quote(s);if(v instanceof Number||v instanceof Boolean)return v.toString();if(v instanceof Map<?,?> m)return m.entrySet().stream().map(e->toJson(String.valueOf(e.getKey()))+":"+toJson(e.getValue())).collect(java.util.stream.Collectors.joining(",","{","}"));if(v instanceof Collection<?> c)return c.stream().map(LegacyBlockingTreeImporterMain::toJson).collect(java.util.stream.Collectors.joining(",","[","]"));return toJson(String.valueOf(v));}
  private static final class CountingInputStream extends FilterInputStream {private final long max;private long count;CountingInputStream(InputStream in,long max){super(in);this.max=max;}public int read()throws IOException{int r=super.read();if(r>=0&&++count>max)throw new IOException("serialized input exceeds byte limit");return r;}public int read(byte[] b,int o,int l)throws IOException{int n=super.read(b,o,l);if(n>0&&(count+=n)>max)throw new IOException("serialized input exceeds byte limit");return n;}}
}
