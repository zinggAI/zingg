package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.*;
import io.zingg.duckdb.engine.SqlSafety;
import java.util.List;

/** Candidate generation and scoring seam for imported/native matchers. */
public final class Matcher {
  public record MatchConfig(String idColumn,String blockingColumn,String scoreExpression,double threshold){public MatchConfig{if(idColumn==null||idColumn.isBlank()||blockingColumn==null||blockingColumn.isBlank())throw new IllegalArgumentException("id and blocking columns required");if(!Double.isFinite(threshold))throw new IllegalArgumentException("threshold must be finite");scoreExpression=SqlSafety.predicate(scoreExpression);}}
  public Frame candidates(Frame input,MatchConfig config){
    if(input==null||config==null)throw new DuckException("candidate input and config are required");
    if(!input.columns().contains(config.idColumn()))throw new DuckException("match id column is not present: "+config.idColumn());
    if(!input.columns().contains(config.blockingColumn()))throw new DuckException("blocking column is not present: "+config.blockingColumn());
    String l="l",r="r";return input.joinProjected(input,l+"."+quote(config.blockingColumn())+" = "+r+"."+quote(config.blockingColumn())+" AND "+l+"."+quote(config.idColumn())+" < "+r+"."+quote(config.idColumn()),"z_");
  }
  public Frame score(Frame candidates,MatchConfig config){return candidates.withColumn("z_score",new io.zingg.duckdb.engine.DuckExpr(config.scoreExpression())).filter(new io.zingg.duckdb.engine.DuckExpr("z_score >= "+config.threshold()));}
  /** Scores numeric feature columns with a validated native linear classifier. */
  public Frame scoreLinear(Frame candidates,MatchConfig config,LinearClassifier classifier){
    if(classifier==null)throw new DuckException("classifier is required");
    if(candidates==null)throw new DuckException("classifier candidates are required");
    List<String> inputFeatures=classifier.baseFeatures().isEmpty()?classifier.features():classifier.baseFeatures();
    for(String feature:inputFeatures)if(!candidates.columns().contains(feature))throw new DuckException("classifier feature is not present in candidates: "+feature);
    var terms=new java.util.ArrayList<String>(); double[] weights=classifier.weights();
    List<String> expanded=classifier.baseFeatures().isEmpty()
        ?classifier.features().stream().map(Matcher::quote).toList()
        :PolynomialFeatures.sqlTerms(classifier.baseFeatures(),classifier.polynomialDegree());
    if(expanded.size()!=weights.length)throw new DuckException("classifier expanded feature dimensions differ");
    for(int i=0;i<weights.length;i++)terms.add("("+weights[i]+" * "+expanded.get(i)+")");
    // Keep the generated expression shallow: imported Spark models can have
    // thousands of polynomial terms, and a left-deep addition tree can exhaust
    // the DuckDB parser/native stack. LIST_SUM over one LIST_VALUE remains flat.
    String z="list_sum(list_value("+classifier.intercept()
        +(terms.isEmpty()?"":", "+String.join(", ",terms))+"))";
    String expression=classifier.logistic()?"(1.0 / (1.0 + exp(-("+z+"))))":"("+z+")";
    double threshold=classifier.threshold();
    var scored=candidates.withColumn("z_score",new io.zingg.duckdb.engine.ValidatedModelExpression(expression));
    return scored.withColumn("z_prediction",new io.zingg.duckdb.engine.DuckExpr("CASE WHEN z_score >= "+threshold+" THEN 1 ELSE 0 END"))
        .filter(new io.zingg.duckdb.engine.DuckExpr("z_prediction = 1"));
  }
  private static String quote(String value){return "\""+value.replace("\"","\"\"")+"\"";}
}
