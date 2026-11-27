package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/** Phase-aware input reader. Source tagging deliberately happens before union. */
public final class DuckPipeReader {
  public enum UnionMode { MATCH_BY_NAME, TRAINING_POSITIONAL }
  private final DuckJob job; private final io.zingg.duckdb.api.PathPolicy policy; private final ResourceBudget budget;
  private final Runnable afterPreflight;
  private record InputSnapshot(Path path,long size,FileTime modified,Object fileKey) {}
  public DuckPipeReader(JobHandle job) { this(job,null,ResourceBudget.unlimited()); }
  public DuckPipeReader(JobHandle job,io.zingg.duckdb.api.PathPolicy policy) { this(job,policy,ResourceBudget.unlimited()); }
  public DuckPipeReader(JobHandle job,io.zingg.duckdb.api.PathPolicy policy,ResourceBudget budget) {
    this(job,policy,budget,()->{});
  }
  DuckPipeReader(JobHandle job,io.zingg.duckdb.api.PathPolicy policy,ResourceBudget budget,Runnable afterPreflight) {
    if (!(job instanceof DuckJob j)) throw new IllegalArgumentException("DuckJob required");
    this.job=j; this.policy=policy; this.budget=budget==null?ResourceBudget.unlimited():budget;
    this.afterPreflight=Objects.requireNonNull(afterPreflight,"afterPreflight");
  }
  public Frame read(List<Path> files, UnionMode mode) {
    return read(files, mode, true);
  }
  /**
   * Reads and unions source files, optionally assigning the backend's row ID.
   * Phases whose input contract already carries entity IDs (for example LINK
   * scored-pair endpoints) must preserve those values rather than regenerate them.
   */
  public Frame read(List<Path> files, UnionMode mode, boolean assignRowIds) {
    if (files==null || files.isEmpty()) throw new DuckException("at least one input pipe is required");
    List<InputSnapshot> inputs=new java.util.ArrayList<>(files.size());
    long totalInputBytes=0;
    for(Path file:files){
      Path p=policy==null?file.toAbsolutePath().normalize():policy.input(file);
      try {
        BasicFileAttributes attributes=java.nio.file.Files.readAttributes(p,BasicFileAttributes.class,java.nio.file.LinkOption.NOFOLLOW_LINKS);
        if(attributes.isSymbolicLink()) throw new DuckException("symbolic-link inputs are not allowed: "+p);
        if(!attributes.isRegularFile()) throw new DuckException("input file does not exist: "+p);
        long size=attributes.size();
        if(size>Long.MAX_VALUE-totalInputBytes) throw new DuckException("input byte count overflow");
        totalInputBytes+=size;
        budget.enforceInputBytes(totalInputBytes);
        inputs.add(new InputSnapshot(p,size,attributes.lastModifiedTime(),attributes.fileKey()));
      } catch(java.io.IOException e) {
        throw new DuckException("cannot inspect input size: "+p,e);
      }
    }
    Path snapshotDirectory=null;
    Throwable failure=null;
    try {
      afterPreflight.run();
      verifySnapshots(inputs);
      List<Path> readInputs;
      if(budget.maxInputBytes()>0){
        snapshotDirectory=createSnapshotDirectory();
        readInputs=copyInputsToSnapshot(inputs,snapshotDirectory);
      } else {
        verifySnapshots(inputs);
        readInputs=inputs.stream().map(InputSnapshot::path).toList();
      }
      Frame result=null;
      for (int i=0;i<readInputs.size();i++) {
        if(budget.maxInputBytes()==0) verifySnapshot(inputs.get(i));
        Path p=readInputs.get(i);
        if (isArrow(p)) {
          Frame current=ArrowFileSupport.readWithAppender(job,p).withColumn("z_source", i);
          result=result==null?current:result.union(current, mode==UnionMode.MATCH_BY_NAME, mode==UnionMode.MATCH_BY_NAME);
          continue;
        }
        Frame current=job.sql("SELECT * FROM "+readerSql(p)).withColumn("z_source", i);
        result=result==null?current:result.union(current, mode==UnionMode.MATCH_BY_NAME, mode==UnionMode.MATCH_BY_NAME);
      }
      Frame merged=result.cache();
      if(assignRowIds) merged=RowIdAssigner.assign(merged,"z_zid").cache();
      budget.enforceRows(merged.count());
      if(budget.maxInputBytes()==0) verifySnapshots(inputs);
      return merged;
    } catch(java.io.IOException e) {
      DuckException wrapped=new DuckException("cannot create bounded input snapshot",e);
      failure=wrapped;
      throw wrapped;
    } catch(RuntimeException|Error e) {
      failure=e;
      throw e;
    } finally {
      if(snapshotDirectory!=null) try {
        deleteSnapshotDirectory(snapshotDirectory);
      } catch(java.io.IOException cleanup) {
        if(failure!=null) failure.addSuppressed(cleanup);
        else throw new DuckException("cannot clean bounded input snapshot",cleanup);
      }
    }
  }
  private Path createSnapshotDirectory() throws java.io.IOException {
    Path parent=job.tempDirectory();
    Files.createDirectories(parent);
    if(!Files.isDirectory(parent)) throw new java.io.IOException("DuckDB temp_directory is not a directory: "+parent);
    try {
      return Files.createTempDirectory(parent,".zingg-input-",
          java.nio.file.attribute.PosixFilePermissions.asFileAttribute(
              java.nio.file.attribute.PosixFilePermissions.fromString("rwx------")));
    } catch(UnsupportedOperationException unsupported) {
      return Files.createTempDirectory(parent,".zingg-input-");
    }
  }
  private List<Path> copyInputsToSnapshot(List<InputSnapshot> inputs,Path directory) throws java.io.IOException {
    List<Path> copies=new java.util.ArrayList<>(inputs.size());
    long copiedTotal=0;
    byte[] buffer=new byte[64*1024];
    for(InputSnapshot input:inputs){
      verifySnapshot(input);
      String name=input.path().getFileName().toString();
      int dot=name.lastIndexOf('.');
      String suffix=dot>=0?name.substring(dot):".data";
      Path copy=Files.createTempFile(directory,"input-",suffix);
      try(var source=Files.newInputStream(input.path(),StandardOpenOption.READ,LinkOption.NOFOLLOW_LINKS);
          var target=Files.newOutputStream(copy,StandardOpenOption.WRITE,StandardOpenOption.TRUNCATE_EXISTING)){
        int read;
        while((read=source.read(buffer))>=0){
          if(read==0) continue;
          if(read>Long.MAX_VALUE-copiedTotal) throw new DuckException("input byte count overflow");
          copiedTotal+=read;
          budget.enforceInputBytes(copiedTotal);
          target.write(buffer,0,read);
        }
      }
      verifySnapshot(input);
      copies.add(copy);
    }
    return copies;
  }
  private static void deleteSnapshotDirectory(Path directory) throws java.io.IOException {
    try(var entries=Files.list(directory)){
      for(Path entry:entries.toList()) Files.deleteIfExists(entry);
    }
    Files.deleteIfExists(directory);
  }
  private static void verifySnapshots(List<InputSnapshot> inputs) {
    for(InputSnapshot expected:inputs) verifySnapshot(expected);
  }
  private static void verifySnapshot(InputSnapshot expected) {
    try {
      BasicFileAttributes actual=java.nio.file.Files.readAttributes(expected.path(),BasicFileAttributes.class,
          java.nio.file.LinkOption.NOFOLLOW_LINKS);
      if(!actual.isRegularFile()||actual.isSymbolicLink()||actual.size()!=expected.size()
          ||!actual.lastModifiedTime().equals(expected.modified())
          ||!Objects.equals(actual.fileKey(),expected.fileKey()))
        throw new DuckException("input changed after byte-budget preflight: "+expected.path());
    } catch(java.io.IOException e) {
      throw new DuckException("cannot verify input file after byte-budget preflight: "+expected.path(),e);
    }
  }
  private static boolean isArrow(Path p){String n=p.getFileName().toString().toLowerCase(java.util.Locale.ROOT);return n.endsWith(".arrow")||n.endsWith(".feather");}
  private static String readerSql(Path p){
    String n=p.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
    String path=DuckExpr.literal(p.toString()).sql();
    if(n.endsWith(".parquet")) return "read_parquet("+path+")";
    if(n.endsWith(".json")||n.endsWith(".jsonl")) return "read_json_auto("+path+")";
    if(n.endsWith(".csv")) return "read_csv_auto("+path+")";
    if(n.endsWith(".tsv")) return "read_csv_auto("+path+", delim='\\t')";
    throw new DuckException("unsupported input format: "+p);
  }
}
