package io.zingg.duckdb.legacy;
import io.zingg.duckdb.api.DuckException; import io.zingg.duckdb.model.*; import java.nio.file.*; import java.util.*;
public final class BlockingImporterLauncher {
 private final IsolatedImporterProcess process; public BlockingImporterLauncher(ImportLimits limits){process=new IsolatedImporterProcess(limits);}
 public Path launch(LegacyImporterSpec spec,Path input,Path output)throws Exception{if(!"zingg-blocking-spark35".equals(spec.name()))throw new DuckException("unexpected blocking importer: "+spec.name());Path launcher=spec.launcher().toAbsolutePath().normalize();if(!Files.isRegularFile(launcher))throw new DuckException("blocking importer launcher is not a regular file: "+launcher);var limits=spec.limits();var isolated=new IsolatedImporterProcess(limits);int code=isolated.run(List.of(launcher.toString()),input,output,spec.allowedClasses());if(code!=0)throw new DuckException("blocking importer exited with code "+code);new BlockingImporter(limits.maxBytes()).importNative(output);return output;}
}
