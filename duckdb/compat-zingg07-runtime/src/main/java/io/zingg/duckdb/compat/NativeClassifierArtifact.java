package io.zingg.duckdb.compat;
import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.model.*;
import io.zingg.duckdb.engine.DuckCancellation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/** Writes the neutral linear-classifier payload consumed by ModelScorerRegistry. */
public final class NativeClassifierArtifact {
  public record Config(String profile,List<String> features,double[] weights,double intercept,boolean logistic,Path output,
      double threshold,List<String> assemblerInputCols,int polynomialDegree,long maxArtifactBytes) {
    public Config {
      if(profile==null||profile.isBlank()||output==null) throw new IllegalArgumentException("profile and output are required");
      if(maxArtifactBytes<0) throw new IllegalArgumentException("maxArtifactBytes cannot be negative");
      features=List.copyOf(features==null?List.of():features); weights=weights==null?new double[0]:weights.clone();
      if(features.isEmpty()||features.size()!=weights.length) throw new IllegalArgumentException("classifier feature/weight dimensions differ");
      if(features.stream().anyMatch(f->f==null||f.isBlank())) throw new IllegalArgumentException("classifier feature is blank");
      if(features.stream().distinct().count()!=features.size()) throw new IllegalArgumentException("classifier features must be unique");
      if(java.util.Arrays.stream(weights).anyMatch(w->!Double.isFinite(w))||!Double.isFinite(intercept)) throw new IllegalArgumentException("classifier coefficients must be finite");
      if(!Double.isFinite(threshold)||threshold<0d||threshold>1d) throw new IllegalArgumentException("classifier threshold must be in [0,1]");
      assemblerInputCols=List.copyOf(assemblerInputCols==null?List.of():assemblerInputCols);
      if(assemblerInputCols.stream().anyMatch(f->f==null||f.isBlank())
          ||assemblerInputCols.stream().distinct().count()!=assemblerInputCols.size())
        throw new IllegalArgumentException("classifier assembler inputs must be unique nonblank names");
      if(assemblerInputCols.isEmpty()) {
        if(polynomialDegree!=0) throw new IllegalArgumentException("non-polynomial classifier degree must be zero");
      } else if(PolynomialFeatures.featureCount(assemblerInputCols.size(),polynomialDegree,features.size())!=features.size()) {
        throw new IllegalArgumentException("classifier polynomial feature dimensions differ");
      }
    }
    public Config(String profile,List<String> features,double[] weights,double intercept,boolean logistic,Path output,
        double threshold,List<String> assemblerInputCols,int polynomialDegree) {
      this(profile,features,weights,intercept,logistic,output,threshold,assemblerInputCols,polynomialDegree,0);
    }
    @Override public double[] weights(){return weights.clone();}
    public Config(String profile,List<String> features,double[] weights,double intercept,boolean logistic,Path output){this(profile,features,weights,intercept,logistic,output,0.5,List.of(),0);}
  }
  public Path write(Config config) {
    if(config==null) throw new IllegalArgumentException("classifier config is required");
    String features=config.features().stream().map(ModelArtifactJson::quote).collect(Collectors.joining(","));
    String weights=java.util.Arrays.stream(config.weights()).mapToObj(Double::toString).collect(Collectors.joining(","));
    String baseFeatures=config.assemblerInputCols().stream().map(ModelArtifactJson::quote).collect(Collectors.joining(","));
    String payload="{\"kind\":\"linear-classifier\",\"features\":["+features+"],\"weights\":["+weights+"],\"intercept\":"+config.intercept()+",\"logistic\":"+config.logistic()+",\"threshold\":"+config.threshold()+",\"assemblerInputCols\":["+baseFeatures+"],\"polynomialDegree\":"+config.polynomialDegree()+"}";
    try {
      var manifest=new ModelManifest(config.profile(),"0.7.0","zingg-0.1-native",ModelType.CLASSIFIER.name(),config.features(),"");
      var provenance=new ImportProvenance(config.output().toAbsolutePath().normalize().toString(),"0.7.0","zingg-duckdb-native-0.1",Instant.now(),"");
      var cancellation = DuckCancellation.current();
      new ModelArtifactWriter(config.output().toAbsolutePath().normalize(),
          cancellation == null ? () -> false : cancellation::isCancelled)
          .write(manifest,payload.getBytes(StandardCharsets.UTF_8),provenance,
              config.maxArtifactBytes());
      return config.output().toAbsolutePath().normalize();
    } catch(Exception e) {
      if(e instanceof DuckException d) throw d;
      if(e instanceof java.io.IOException io && io.getMessage()!=null
          && io.getMessage().startsWith("model artifact byte budget exceeded:"))
        throw new DuckException(io.getMessage(),io);
      throw new DuckException("classifier artifact write failed",e);
    }
  }
}
