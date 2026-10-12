package io.zingg.duckdb.compat;

import io.zingg.duckdb.api.DuckException; import io.zingg.duckdb.model.ModelScorer; import java.util.Map;

/** Neutral scorer seam. Concrete imported estimator implementations are selected by manifest metadata. */
public final class ClassifierScorer implements ModelScorer {
  private final double threshold;
  public ClassifierScorer(double threshold){if(Double.isNaN(threshold))throw new IllegalArgumentException("threshold");this.threshold=threshold;}
  public double score(Map<String,Object> features){if(features==null)throw new DuckException("features are required");Object value=features.get("score");if(!(value instanceof Number n))throw new DuckException("classifier feature 'score' is required");return n.doubleValue();}
  public boolean isMatch(Map<String,Object> features){return score(features)>=threshold;}
}
