package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException;
import io.zingg.duckdb.api.Frame;
import io.zingg.duckdb.api.CancellationToken;
import io.zingg.duckdb.engine.DuckCancellation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Spark-free full-batch trainer that computes gradients over DuckDB relations. */
public final class NativeClassifierTrainer {
  public record Config(String profile, List<String> features, String labelColumn, Path artifactDirectory,
      int iterations, double learningRate, double l2, long maxRows, long maxArtifactBytes) {
    public Config {
      if (profile == null || profile.isBlank() || labelColumn == null || labelColumn.isBlank() || artifactDirectory == null)
        throw new IllegalArgumentException("classifier profile, label and artifact directory are required");
      features = List.copyOf(features == null ? List.of() : features);
      if (features.isEmpty() || features.stream().anyMatch(f -> f == null || f.isBlank()))
        throw new IllegalArgumentException("classifier features are required");
      if (features.stream().distinct().count() != features.size())
        throw new IllegalArgumentException("classifier features must be unique");
      if (features.contains(labelColumn))
        throw new IllegalArgumentException("classifier label column cannot also be a feature");
      if (iterations < 1 || iterations > 1_000_000 || !Double.isFinite(learningRate) || learningRate <= 0
          || !Double.isFinite(l2) || l2 < 0 || maxRows < 1 || maxArtifactBytes < 0)
        throw new IllegalArgumentException("invalid classifier training parameters");
    }
    public Config(String profile, List<String> features, String labelColumn, Path artifactDirectory,
        int iterations, double learningRate, double l2, long maxRows) {
      this(profile, features, labelColumn, artifactDirectory, iterations, learningRate, l2, maxRows, 0);
    }
    public static Config defaults(String profile, List<String> features, String labelColumn, Path output) {
      return new Config(profile, features, labelColumn, output, 200, 0.05, 1e-4, 1_000_000);
    }
  }

  public record Result(Path artifactDirectory, long rows, double[] weights, double intercept) {
    public Result { weights = weights.clone(); }
    @Override public double[] weights() { return weights.clone(); }
  }

  /** Result plus the first optimizer iteration's physical DuckDB gradient plan. */
  public record TrainingDiagnostics(Result result, String firstGradientPhysicalPlan) {
    public TrainingDiagnostics {
      if (result == null || firstGradientPhysicalPlan == null || firstGradientPhysicalPlan.isBlank())
        throw new IllegalArgumentException("training result and gradient plan are required");
    }
  }

  public Result train(Frame input, Config config) {
    return train(input, config, null);
  }

  /** Runs normal training and additionally captures the first gradient aggregate's physical plan. */
  public TrainingDiagnostics trainWithDiagnostics(Frame input, Config config) {
    var plan = new StringBuilder();
    Result result = train(input, config, plan);
    return new TrainingDiagnostics(result, plan.toString());
  }

  private Result train(Frame input, Config config, StringBuilder firstGradientPlan) {
    if (input == null || config == null) throw new IllegalArgumentException("classifier input and config are required");
    DuckCancellation requestCancellation = DuckCancellation.current();
    CancellationToken cancellation = requestCancellation == null
        ? CancellationToken.none() : requestCancellation;
    cancellation.throwIfCancelled();
    long inputRows = input.count(cancellation);
    if (inputRows > config.maxRows())
      throw new DuckException("classifier maxRows exceeded: " + inputRows + " > " + config.maxRows());
    // Freeze the source once inside DuckDB so every optimizer iteration sees
    // identical rows without retaining the full dataset in Java heap.
    Frame data=input.cache();
    long materializedRows=data.count(cancellation);
    if(materializedRows!=inputRows)
      throw new DuckException("classifier input row count changed between count and materialization");
    var schema = data.schema();
    var types = new java.util.HashMap<String,Integer>();
    for(var column:schema)types.put(column.name(),column.jdbcType());
    requireNumericColumn(types,config.labelColumn(),true);
    var featureSql = new ArrayList<String>(config.features().size());
    for(String feature:config.features()){
      requireNumericColumn(types,feature,false);
      featureSql.add(quote(feature));
    }
    String labelSql=quote(config.labelColumn());
    String invalid="("+labelSql+" IS NULL OR TRY_CAST("+labelSql+" AS DOUBLE) IS NULL OR "
        +"TRY_CAST("+labelSql+" AS DOUBLE) NOT IN (0.0, 1.0)";
    for(String feature:featureSql)
      invalid+=" OR "+feature+" IS NULL OR NOT isfinite(TRY_CAST("+feature+" AS DOUBLE))";
    invalid+=")";
    var validation=data.aggregate(null,
        "COUNT(*) FILTER (WHERE "+invalid+") AS invalid_rows",
        "SUM(CASE WHEN TRY_CAST("+labelSql+" AS DOUBLE)=1.0 THEN 1 ELSE 0 END) AS positive_rows",
        "SUM(CASE WHEN TRY_CAST("+labelSql+" AS DOUBLE)=0.0 THEN 1 ELSE 0 END) AS negative_rows")
        .collect(cancellation).get(0);
    long invalidRows=((Number)validation.get("invalid_rows")).longValue();
    if(invalidRows>0)throw new DuckException("classifier labels must be 0/1 and features must be finite numeric; invalid rows: "+invalidRows);
    if(((Number)validation.get("positive_rows")).longValue()==0
        ||((Number)validation.get("negative_rows")).longValue()==0)
      throw new DuckException("classifier training requires both positive and negative labels");

    double[] weights = new double[featureSql.size()];
    double intercept = 0d;
    for (int iteration = 0; iteration < config.iterations(); iteration++) {
      cancellation.throwIfCancelled();
      StringBuilder linear=new StringBuilder(Double.toString(intercept));
      for(int i=0;i<weights.length;i++)linear.append(" + (").append(Double.toString(weights[i])).append(" * CAST(")
          .append(featureSql.get(i)).append(" AS DOUBLE))");
      String z=linear.toString();
      String prediction="CASE WHEN ("+z+") >= 0.0 THEN 1.0/(1.0+exp(-("+z+"))) "
          +"ELSE exp("+z+")/(1.0+exp("+z+")) END";
      String suffix=java.util.UUID.randomUUID().toString().replace("-","");
      String predColumn="__zd_train_pred_"+suffix,errorColumn="__zd_train_error_"+suffix;
      Frame errors=data.withColumn(predColumn,new io.zingg.duckdb.engine.DuckExpr(prediction))
          .withColumn(errorColumn,new io.zingg.duckdb.engine.DuckExpr(predColumn+" - CAST("+labelSql+" AS DOUBLE)"));
      String errorSql=quote(errorColumn);
      var aggregates=new ArrayList<String>();
      for(int i=0;i<featureSql.size();i++)aggregates.add("AVG("+errorSql+" * CAST("+featureSql.get(i)+" AS DOUBLE)) AS "+quote("gradient_"+i));
      aggregates.add("AVG("+errorSql+") AS gradient_intercept");
      Frame gradientAggregate = errors.aggregate(null,aggregates.toArray(String[]::new));
      if (firstGradientPlan != null && firstGradientPlan.isEmpty())
        firstGradientPlan.append(gradientAggregate.explain());
      var gradients=gradientAggregate.collect(cancellation).get(0);
      double interceptGradient=((Number)gradients.get("gradient_intercept")).doubleValue();
      intercept -= config.learningRate() * interceptGradient;
      for (int i = 0; i < weights.length; i++) {
        double gradient=((Number)gradients.get("gradient_"+i)).doubleValue()+config.l2()*weights[i];
        weights[i] -= config.learningRate() * gradient;
      }
    }
    if (!Double.isFinite(intercept) || java.util.Arrays.stream(weights).anyMatch(w -> !Double.isFinite(w)))
      throw new DuckException("classifier training produced non-finite coefficients");
    cancellation.throwIfCancelled();
    Path artifact = new NativeClassifierArtifact().write(new NativeClassifierArtifact.Config(
        config.profile(), config.features(), weights, intercept, true, config.artifactDirectory(),
        0.5, List.of(), 0, config.maxArtifactBytes()));
    return new Result(artifact, inputRows, weights, intercept);
  }

  private static void requireNumericColumn(java.util.Map<String,Integer> types,String name,boolean label){
    Integer type=types.get(name);
    if(type==null)throw new DuckException("classifier "+(label?"label":"feature")+" column is not present: "+name);
    if(label&&type==java.sql.Types.BOOLEAN)return;
    if(!java.util.Set.of(java.sql.Types.TINYINT,java.sql.Types.SMALLINT,java.sql.Types.INTEGER,
        java.sql.Types.BIGINT,java.sql.Types.FLOAT,java.sql.Types.REAL,java.sql.Types.DOUBLE,
        java.sql.Types.NUMERIC,java.sql.Types.DECIMAL).contains(type))
      throw new DuckException("classifier "+(label?"label":"feature")+" must be numeric"+(label?" or boolean":"")+": "+name);
  }
  private static String quote(String identifier){return "\""+identifier.replace("\"","\"\"")+"\"";}
}
