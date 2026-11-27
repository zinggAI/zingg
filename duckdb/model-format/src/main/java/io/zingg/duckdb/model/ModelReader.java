package io.zingg.duckdb.model;
import io.zingg.duckdb.api.DuckException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
public final class ModelReader {
  private static final Set<String> PROFILES=Set.of("zingg-0.7.0");
  private static final long MAX_METADATA_BYTES=64L*1024*1024;
  private ModelReader(){}
  public static LoadedModel load(Path root,long maxBytes) throws IOException {
    Path dir=root.toAbsolutePath().normalize();
    Path parent=dir.getParent();
    if(parent==null||!Files.isDirectory(parent,LinkOption.NOFOLLOW_LINKS))
      throw new DuckException("model artifact directory is required: "+dir);
    // The writer performs two directory renames during replacement. Holding a
    // shared publication lock for the complete read prevents a reader from
    // observing the intentional gap between those renames or a torn model.
    try(var ignored=ModelPublicationLock.read(dir)) {
      return loadUnlocked(dir,maxBytes);
    }
  }
  private static LoadedModel loadUnlocked(Path dir,long maxBytes) throws IOException {
    if(Files.isSymbolicLink(dir)||!Files.isDirectory(dir,LinkOption.NOFOLLOW_LINKS))throw new DuckException("model artifact directory is required: "+dir);
    try(var paths=Files.walk(dir)){for(var it=paths.iterator();it.hasNext();)if(Files.isSymbolicLink(it.next()))throw new DuckException("symbolic links are not allowed in model artifacts");}catch(UncheckedIOException e){throw e.getCause();}
    if(maxBytes>0){try(var paths=Files.walk(dir)){long total=0;for(var it=paths.filter(Files::isRegularFile).iterator();it.hasNext();){long size=Files.size(it.next());if(size>maxBytes-total)throw new DuckException("model artifact exceeds configured size limit");total+=size;}}catch(UncheckedIOException e){throw e.getCause();}}
    Path manifest=dir.resolve("manifest.json");
    byte[] manifestBytes=readBounded(manifest,metadataLimit(maxBytes));
    Map<String,Object> manifestFields=ModelArtifactJson.parseObject(decodeUtf8(manifestBytes,"model manifest"));
    if(!manifestFields.keySet().equals(Set.of("profile","zinggVersion","formatVersion","modelType","features","sha256")))
      throw new DuckException("model manifest is incomplete or contains unknown fields");
    String profile=stringField(manifestFields,"profile"),type=stringField(manifestFields,"modelType"),hash=stringField(manifestFields,"sha256"),format=stringField(manifestFields,"formatVersion"),version=stringField(manifestFields,"zinggVersion");
    Object featureValue=manifestFields.get("features");
    if(!(featureValue instanceof List<?> featureList)||featureList.stream().anyMatch(value->!(value instanceof String)))
      throw new DuckException("model manifest features must be a string array");
    List<String> modelFeatures=featureList.stream().map(String.class::cast).toList();
    if(!PROFILES.contains(profile)&&!"duckdb-native-0.1".equals(profile))throw new DuckException("unsupported model profile: "+profile);
    if(!Set.of("BLOCKING_TREE","BACKEND_BLOCKING_HISTOGRAM","CLASSIFIER").contains(type))throw new DuckException("unsupported model type: "+type);
    ModelFormat.requireSupported(format);
    if(!"0.7.0".equals(version)&&!"0.1.0".equals(version))throw new DuckException("unsupported model version: "+version);
    boolean backendHistogram=ModelType.BACKEND_BLOCKING_HISTOGRAM.name().equals(type);
    if(backendHistogram != "duckdb-native-0.1".equals(profile) || backendHistogram != "0.1.0".equals(version))
      throw new DuckException("model profile, version, and type are inconsistent");
    Path provenance = dir.resolve("provenance.json");
    if (!Files.isRegularFile(provenance,LinkOption.NOFOLLOW_LINKS)) throw new DuckException("model provenance is missing");
    byte[] provenanceBytes=readBounded(provenance,metadataLimit(maxBytes)-manifestBytes.length);
    Map<String, Object> parsedProvenance=ModelArtifactJson.parseObject(decodeUtf8(provenanceBytes,"model provenance"));
    Map<String,String> provenanceFields=new HashMap<>();
    parsedProvenance.forEach((key,value)->{if(value instanceof String string)provenanceFields.put(key,string);});
    if(provenanceFields.size()!=parsedProvenance.size())throw new DuckException("model provenance values must be strings");
    if (!provenanceFields.keySet().equals(Set.of("sourcePath", "sourceVersion", "importerVersion", "importedAt", "sha256")))
      throw new DuckException("model provenance is incomplete or contains unknown fields");
    if (!hash.equalsIgnoreCase(provenanceFields.get("sha256"))) throw new DuckException("provenance checksum mismatch");
    if (provenanceFields.get("sourcePath").isBlank() || provenanceFields.get("importerVersion").isBlank())
      throw new DuckException("model provenance is incomplete");
    try { java.time.Instant.parse(provenanceFields.get("importedAt")); }
    catch (java.time.DateTimeException error) { throw new DuckException("model provenance timestamp is invalid", error); }
    long payloadLimit=maxBytes>0?maxBytes-manifestBytes.length-provenanceBytes.length:Integer.MAX_VALUE;
    if(payloadLimit<1)throw new DuckException("model artifact exceeds configured size limit");
    byte[] bytes=readBounded(dir.resolve("model.bin"),payloadLimit);
    if(maxBytes>0&&manifestBytes.length+provenanceBytes.length+bytes.length>maxBytes)
      throw new DuckException("model artifact exceeds configured size limit");
    if(hash.isBlank()||!hash.equalsIgnoreCase(NativeModelStore.sha256(bytes)))throw new DuckException("model checksum mismatch");
    if (format.equals("zingg-0.1-native")) validateNativePayload(type, bytes);
    return new LoadedModel(new ModelManifest(profile,version,format,type,modelFeatures,hash),bytes);
  }
  private static long metadataLimit(long maxBytes){return maxBytes>0?Math.min(MAX_METADATA_BYTES,maxBytes):MAX_METADATA_BYTES;}
  private static byte[] readBounded(Path path,long maxBytes)throws IOException{
    if(maxBytes<1)throw new DuckException("model artifact exceeds configured size limit");
    try(var channel=FileChannel.open(path,StandardOpenOption.READ,LinkOption.NOFOLLOW_LINKS)){
      long size=channel.size();
      if(size<1||size>maxBytes||size>Integer.MAX_VALUE)throw new DuckException("model artifact file size is invalid: "+path.getFileName());
      var data=ByteBuffer.allocate((int)size);
      while(data.hasRemaining()){
        int read=channel.read(data);
        if(read<0)throw new DuckException("model artifact file changed while being read: "+path.getFileName());
      }
      if(channel.size()!=size)throw new DuckException("model artifact file changed while being read: "+path.getFileName());
      return data.array();
    }
  }
  private static String decodeUtf8(byte[] bytes,String label)throws IOException{
    try{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
        .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString();}
    catch(CharacterCodingException error){throw new IOException(label+" is not valid UTF-8",error);}
  }
  private static void validateNativePayload(String type, byte[] bytes) {
    String payload = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    if (payload.indexOf('\ufffd') >= 0) throw new DuckException("native model payload is not valid UTF-8");
    String expected = ModelType.CLASSIFIER.name().equals(type) ? "\"kind\":\"linear-classifier\"" : ModelType.BACKEND_BLOCKING_HISTOGRAM.name().equals(type) ? "\"kind\":\"duckdb-blocking-histogram\"" : "\"kind\":\"native-blocking-tree\"";
    if (!payload.contains(expected)) throw new DuckException("native model payload does not match manifest type: " + type);
  }
  private static String stringField(Map<String,Object> fields,String name){Object value=fields.get(name);if(!(value instanceof String string))throw new DuckException("model manifest field must be a string: "+name);return string;}
  public record LoadedModel(ModelManifest manifest,byte[] payload){public LoadedModel{payload=payload.clone();}}
}
