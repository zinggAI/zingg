package io.zingg.duckdb.worker;
import io.zingg.duckdb.api.RuntimeConfig;
import io.zingg.duckdb.compat.CompatibilityRuntime;
import io.zingg.duckdb.compat.InMemoryLabelDecisionProvider;
import io.zingg.duckdb.compat.PersistentLabelDecisionProvider;
import io.zingg.duckdb.compat.ProfileRegistry;
import io.zingg.duckdb.engine.ResourceBudget;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
public final class Main {
  public static void main(String[] a) throws Exception {
    try {
      run(a);
    } catch (Exception failure) {
      reportFailure(failure);
      throw failure;
    } catch (Error failure) {
      reportFailure(failure);
      throw failure;
    }
  }

  private static void reportFailure(Throwable failure) {
    System.err.println("Worker failed: " + failure.getClass().getName() + ": " + failure.getMessage());
  }

  private static void run(String[] a) throws Exception {
    String url="jdbc:duckdb:"; int threads=Runtime.getRuntime().availableProcessors(), jobs=1;
    long memory=0, maxTemp=0, maxRows=0, maxInput=0, maxSpill=0, maxCollect=0, maxModel=256L*1024*1024, maxOutput=0; Path inputRoot=null, outputRoot=null, labelsRoot=null, tempDirectory=null; boolean offline=true, unsafeDebugSql=false; Set<String> extensions=new LinkedHashSet<>();
    for(int i=0;i<a.length;i++){switch(a[i]){
      case "--url"->url=a[++i]; case "--threads"->threads=Integer.parseInt(a[++i]); case "--memory-bytes"->memory=Long.parseLong(a[++i]);
      case "--max-temp-bytes"->maxTemp=Long.parseLong(a[++i]); case "--max-jobs"->jobs=Integer.parseInt(a[++i]); case "--max-rows"->maxRows=Long.parseLong(a[++i]);
      case "--max-input-bytes"->maxInput=Long.parseLong(a[++i]);
      case "--max-spill-bytes"->maxSpill=Long.parseLong(a[++i]); case "--max-model-bytes"->maxModel=Long.parseLong(a[++i]);
      case "--max-output-bytes"->maxOutput=Long.parseLong(a[++i]);
      case "--max-collect-bytes"->maxCollect=Long.parseLong(a[++i]);
      case "--temp-directory"->tempDirectory=Path.of(a[++i]);
      case "--online"->offline=false; case "--offline"->offline=true; case "--allow-extension"->extensions.add(a[++i].toLowerCase(java.util.Locale.ROOT));
      case "--unsafe-debug-sql"->unsafeDebugSql=true;
      case "--input-root"->inputRoot=Path.of(a[++i]); case "--output-root"->outputRoot=Path.of(a[++i]); case "--labels-root"->labelsRoot=Path.of(a[++i]); default->url=a[i];
    }}
    if(maxModel<0||maxTemp<0||maxInput<0||maxOutput<0||maxCollect<0||maxSpill<0)throw new IllegalArgumentException("resource limits cannot be negative");
    long effectiveTemp=maxTemp>0?maxTemp:maxSpill;
    try(var runtime=new CompatibilityRuntime(new RuntimeConfig(url,threads,memory,tempDirectory,jobs,effectiveTemp,offline,extensions),null,ProfileRegistry.DEFAULT_PROFILE_ID,new ResourceBudget(memory,maxRows,maxInput,maxOutput,maxCollect,maxSpill),
            labelsRoot==null?new InMemoryLabelDecisionProvider():new PersistentLabelDecisionProvider(labelsRoot));
        var server=new ConfiguredWorkerServer(runtime,new io.zingg.duckdb.api.PathPolicy(inputRoot,outputRoot),maxModel,maxOutput,maxRows,maxCollect,maxSpill,unsafeDebugSql))
      {server.serve(new java.io.InputStreamReader(System.in),new java.io.OutputStreamWriter(System.out));}
  }
}
