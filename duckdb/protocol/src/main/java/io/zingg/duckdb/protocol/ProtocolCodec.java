package io.zingg.duckdb.protocol;

import java.util.regex.Pattern;

/** Minimal line-safe protocol codec; payload is opaque JSON owned by the operation. */
public final class ProtocolCodec {
  public static final int MAX_LINE_CHARS = 8 * 1024 * 1024;
  private static final Pattern SAFE=Pattern.compile("^[A-Za-z0-9_.-]+$"); private ProtocolCodec(){}
  public static String encode(WorkerMessage m){if(m==null)throw new IllegalArgumentException("worker message is required");requireOperation(m.operation());return esc(m.id())+"\t"+esc(m.operation())+"\t"+esc(m.payload());}
  public static WorkerMessage decode(String line){if(line==null||line.length()>MAX_LINE_CHARS)throw new IllegalArgumentException("worker message exceeds size limit");String[] p=line.split("\\t",-1);if(p.length!=3)throw new IllegalArgumentException("invalid worker message");String operation=unesc(p[1]);requireOperation(operation);return new WorkerMessage(unesc(p[0]),operation,unesc(p[2]));}
  private static String esc(String s){return s==null?"":s.replace("\\","\\\\").replace("\t","\\t").replace("\n","\\n");}
  private static String unesc(String s){StringBuilder out=new StringBuilder(s.length());boolean escaped=false;for(int i=0;i<s.length();i++){char c=s.charAt(i);if(escaped){if(c=='n')out.append('\n');else if(c=='t')out.append('\t');else if(c=='\\')out.append('\\');else{out.append('\\').append(c);}escaped=false;}else if(c=='\\')escaped=true;else out.append(c);}if(escaped)out.append('\\');return out.toString();}
  public static void requireOperation(String op){if(op==null||!SAFE.matcher(op).matches())throw new IllegalArgumentException("invalid operation");}
}
