package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;

/** Explicit compatibility registry. Names are versioned API, not arbitrary SQL identifiers. */
public final class HashRegistry {
  private final Map<String,Function<String,Object>> functions=new LinkedHashMap<>();
  public HashRegistry(){
    functions.put("java_hash",CompatibilityFunctions::javaHash);
    functions.put("lower",CompatibilityFunctions::normalize);
    functions.put("trim",s->s==null?null:s.trim());
    functions.put("sha256",HashRegistry::sha256);
    functions.put("md5",HashRegistry::md5);
    functions.put("length",s->s==null?null:s.length());
    functions.put("java_round",HashRegistry::javaRound);
  }
  public Set<String> names(){return Collections.unmodifiableSet(functions.keySet());}
  public Object apply(String name,String value){var fn=functions.get(name);if(fn==null)throw new DuckException("unsupported compatibility function: "+name);return fn.apply(value);}
  public void register(String name,Function<String,Object> function){if(name==null||!name.matches("[a-z][a-z0-9_]*"))throw new IllegalArgumentException("invalid function name");if(functions.putIfAbsent(name,Objects.requireNonNull(function))!=null)throw new DuckException("function already registered: "+name);}
  private static String sha256(String s){return digest("SHA-256",s);}
  private static String md5(String s){return digest("MD5",s);}
  private static Long javaRound(String s){if(s==null)return null;try{return CompatibilityFunctions.javaRound(Double.parseDouble(s));}catch(NumberFormatException e){throw new DuckException("java_round requires a numeric string",e);}}
  private static String digest(String algorithm,String s){if(s==null)return null;try{return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(s.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new DuckException("hash unavailable: "+algorithm,e);}}
}
