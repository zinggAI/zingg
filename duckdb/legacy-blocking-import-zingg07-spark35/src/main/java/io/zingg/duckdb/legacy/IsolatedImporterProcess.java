package io.zingg.duckdb.legacy;

import io.zingg.duckdb.api.DuckException; import io.zingg.duckdb.model.*; import java.io.IOException; import java.nio.file.*; import java.util.*; import java.util.concurrent.TimeUnit;

/** Process boundary for the optional Spark importer; never deserializes legacy bytes in the worker JVM. */
public final class IsolatedImporterProcess {
  private final ImportLimits limits;
  public IsolatedImporterProcess(ImportLimits limits){this.limits=Objects.requireNonNull(limits);}
  public int run(List<String> command,Path input,Path output)throws IOException,InterruptedException{return run(command,input,output,List.of());}
  public int run(List<String> command,Path input,Path output,List<String> allowedClasses)throws IOException,InterruptedException{
    if(command==null||command.isEmpty())throw new IllegalArgumentException("importer command required");
    if(allowedClasses!=null&&allowedClasses.stream().anyMatch(c->c==null||c.isBlank()||!c.matches("[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*")))throw new IllegalArgumentException("invalid legacy class allowlist entry");
    Path in=input.toAbsolutePath().normalize(),out=output.toAbsolutePath().normalize();
    ImportPreflight.validate(in,limits);
    Path realInput=in.toRealPath();
    if(out.equals(in)||out.startsWith(in)||out.equals(realInput)||out.startsWith(realInput))
      throw new DuckException("legacy output must be outside input directory");
    rejectSymbolicLinkComponents(out);
    Files.createDirectories(out);
    if(Files.isSymbolicLink(out))throw new DuckException("legacy output directory cannot be a symbolic link");
    Path work=Files.createTempDirectory("zingg-import-");
    try { ProcessBuilder pb=new ProcessBuilder(command).directory(work.toFile()).redirectError(ProcessBuilder.Redirect.INHERIT).redirectOutput(ProcessBuilder.Redirect.INHERIT);var env=pb.environment();env.put("ZINGG_IMPORT_INPUT",in.toString());env.put("ZINGG_IMPORT_OUTPUT",out.toString());env.put("ZINGG_IMPORT_MAX_BYTES",Long.toString(limits.maxBytes()));env.put("ZINGG_IMPORT_MAX_DEPTH",Integer.toString(limits.maxDepth()));env.put("ZINGG_IMPORT_MAX_REFERENCES",Long.toString(limits.maxReferences()));env.put("ZINGG_IMPORT_TIMEOUT_MILLIS",Long.toString(limits.timeoutMillis()));env.put("ZINGG_IMPORT_ALLOWED_CLASSES",String.join("\n",allowedClasses==null?List.of():allowedClasses));env.put("ZINGG_IMPORT_NETWORK","disabled");
      Process process=pb.start();
      if(!process.waitFor(limits.timeoutMillis(),TimeUnit.MILLISECONDS)){destroyTree(process);throw new DuckException("legacy importer timed out");}
      int exit=process.exitValue();if(exit!=0)throw new DuckException("legacy importer failed with exit code "+exit);ImportPreflight.validate(out,limits);return exit;
    } finally {try(var paths=Files.walk(work)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException ignored){}});}}
  }
  private static void rejectSymbolicLinkComponents(Path path)throws IOException{
    Path current=path.getRoot();
    for(Path component:path){
      current=current==null?component:current.resolve(component);
      if(Files.isSymbolicLink(current))throw new DuckException("legacy output path contains a symbolic link: "+current);
    }
  }
  private static void destroyTree(Process process) {
    process.descendants().forEach(child -> { try { child.destroyForcibly(); } catch (RuntimeException ignored) {} });
    try { process.destroyForcibly(); } catch (RuntimeException ignored) {}
    try { process.waitFor(5, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
  }
}
