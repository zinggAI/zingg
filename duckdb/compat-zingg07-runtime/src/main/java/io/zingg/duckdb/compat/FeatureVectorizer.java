package io.zingg.duckdb.compat;
import io.zingg.duckdb.api.DuckException; import java.util.*;
public final class FeatureVectorizer {
 public enum MissingPolicy { FAIL, ZERO }
 private final List<String> names; private final MissingPolicy policy;
 public FeatureVectorizer(List<String> names,MissingPolicy policy){if(names==null||names.isEmpty())throw new IllegalArgumentException("feature names required");this.names=List.copyOf(names);this.policy=policy==null?MissingPolicy.FAIL:policy;}
 public double[] vector(Map<String,Object> values){if(values==null)throw new DuckException("feature values required");double[] out=new double[names.size()];for(int i=0;i<names.size();i++){Object value=values.get(names.get(i));if(value==null){if(policy==MissingPolicy.ZERO)continue;throw new DuckException("missing feature: "+names.get(i));}if(!(value instanceof Number n))throw new DuckException("feature is not numeric: "+names.get(i));out[i]=n.doubleValue();}return out;}
 public List<String> names(){return names;}
}
