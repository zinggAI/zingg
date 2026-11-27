package io.zingg.duckdb.model;

import io.zingg.duckdb.api.DuckException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.HexFormat;

public final class NativeModelStore {
 private NativeModelStore(){}
 public static void write(Path root,ModelManifest manifest,byte[] payload)throws IOException{Files.createDirectories(root); Files.writeString(root.resolve("manifest.json"),toJson(manifest)); Files.write(root.resolve("model.bin"),payload);}
 public static byte[] readPayload(Path root)throws IOException{
  Path manifest=root.resolve("manifest.json"),payload=root.resolve("model.bin");
  if(Files.isSymbolicLink(manifest)||!Files.isRegularFile(manifest,LinkOption.NOFOLLOW_LINKS))throw new FileNotFoundException("manifest.json");
  try(var channel=FileChannel.open(payload,StandardOpenOption.READ,LinkOption.NOFOLLOW_LINKS)){
   long size=channel.size();if(size<0||size>Integer.MAX_VALUE)throw new IOException("model payload size is invalid");
   var bytes=ByteBuffer.allocate((int)size);while(bytes.hasRemaining()){int read=channel.read(bytes);if(read<0)throw new IOException("model payload changed while being read");}
   if(channel.size()!=size)throw new IOException("model payload changed while being read");return bytes.array();
  }
 }
 static String toJson(ModelManifest m){String features=m.features().stream().map(ModelArtifactJson::quote).collect(java.util.stream.Collectors.joining(",","[","]"));return "{\"profile\":"+ModelArtifactJson.quote(m.profile())+",\"zinggVersion\":"+ModelArtifactJson.quote(m.zinggVersion())+",\"formatVersion\":"+ModelArtifactJson.quote(m.formatVersion())+",\"modelType\":"+ModelArtifactJson.quote(m.modelType())+",\"features\":"+features+",\"sha256\":"+ModelArtifactJson.quote(m.sha256())+"}";}
 public static String sha256(byte[] b){try{var d=MessageDigest.getInstance("SHA-256");return HexFormat.of().formatHex(d.digest(b));}catch(Exception e){throw new DuckException("cannot hash model",e);}}
}
