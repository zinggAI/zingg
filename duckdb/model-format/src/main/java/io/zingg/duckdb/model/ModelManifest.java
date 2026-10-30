package io.zingg.duckdb.model;
import java.util.List;
public record ModelManifest(String profile,String zinggVersion,String formatVersion,String modelType,List<String> features,String sha256) {
 public ModelManifest {
  if(profile==null||profile.isBlank()||zinggVersion==null||zinggVersion.isBlank()||formatVersion==null||formatVersion.isBlank()||modelType==null||modelType.isBlank()) throw new IllegalArgumentException("model manifest identity is required");
  features=List.copyOf(features==null?List.of():features);
  if(features.stream().anyMatch(f->f==null||f.isBlank())) throw new IllegalArgumentException("model feature names must be non-empty");
  sha256=sha256==null?"":sha256;
  try { ModelType.valueOf(modelType); } catch (IllegalArgumentException e) { throw new IllegalArgumentException("unsupported model type: " + modelType, e); }
 }
}
