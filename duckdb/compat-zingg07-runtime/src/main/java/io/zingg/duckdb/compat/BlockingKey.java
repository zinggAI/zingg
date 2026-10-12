package io.zingg.duckdb.compat;
import java.util.Locale;
public final class BlockingKey {
 public enum Mode { RAW, TRIM_LOWER, JAVA_HASH }
 private final Mode mode; public BlockingKey(Mode mode){this.mode=mode==null?Mode.RAW:mode;}
 public String apply(Object value){if(value==null)return null;String text=String.valueOf(value);return switch(mode){case RAW->text;case TRIM_LOWER->text.trim().toLowerCase(Locale.ROOT);case JAVA_HASH->Integer.toString(text.hashCode());};}
 public Mode mode(){return mode;}
}
