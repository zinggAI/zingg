package io.zingg.duckdb.model;

import io.zingg.duckdb.api.DuckException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Strict small JSON codec for model metadata, including nested objects and scalar arrays. */
public final class ModelArtifactJson {
  private ModelArtifactJson() {}

  static String escape(String value) {
    StringBuilder escaped = new StringBuilder(value.length() + 16);
    for (int index = 0; index < value.length(); index++) {
      char character = value.charAt(index);
      if (Character.isHighSurrogate(character)) {
        if (index + 1 >= value.length() || !Character.isLowSurrogate(value.charAt(index + 1)))
          throw new IllegalArgumentException("model metadata contains an unpaired UTF-16 surrogate");
        escaped.append(character).append(value.charAt(++index));
        continue;
      }
      if (Character.isLowSurrogate(character))
        throw new IllegalArgumentException("model metadata contains an unpaired UTF-16 surrogate");
      switch (character) {
        case '"' -> escaped.append("\\\"");
        case '\\' -> escaped.append("\\\\");
        case '\b' -> escaped.append("\\b");
        case '\f' -> escaped.append("\\f");
        case '\n' -> escaped.append("\\n");
        case '\r' -> escaped.append("\\r");
        case '\t' -> escaped.append("\\t");
        default -> {
          if (character < 0x20) escaped.append(String.format(java.util.Locale.ROOT, "\\u%04x", (int) character));
          else escaped.append(character);
        }
      }
    }
    return escaped.toString();
  }

  public static String quote(String value) { return "\"" + escape(value) + "\""; }

  public static Map<String, Object> parseObject(String json) {
    try {
      Parser parser = new Parser(json);
      Map<String, Object> fields = parser.object();
      parser.whitespace();
      if (!parser.atEnd()) throw parser.invalid();
      return Map.copyOf(fields);
    } catch (IllegalArgumentException error) {
      throw new DuckException("model JSON is malformed", error);
    }
  }

  private static final class Parser {
    private final String input;
    private int cursor;

    private Parser(String input) {
      if (input == null) throw new IllegalArgumentException("JSON input is null");
      this.input = input;
    }

    private Map<String, Object> object() {
      whitespace();
      expect('{');
      whitespace();
      Map<String, Object> fields = new LinkedHashMap<>();
      if (take('}')) return fields;
      while (true) {
        whitespace();
        String key = string();
        whitespace();
        expect(':');
        whitespace();
        Object value = value();
        if (fields.containsKey(key)) throw invalid();
        fields.put(key, value);
        whitespace();
        if (take('}')) return fields;
        expect(',');
      }
    }

    private Object value() {
      if (cursor < input.length() && input.charAt(cursor) == '"') return string();
      if (cursor < input.length() && input.charAt(cursor) == '{') return object();
      if (take('[')) {
        whitespace();
        ArrayList<Object> values = new ArrayList<>();
        if (take(']')) return List.copyOf(values);
        while (true) {
          whitespace();
          values.add(arrayValue());
          whitespace();
          if (take(']')) return List.copyOf(values);
          expect(',');
        }
      }
      if (takeLiteral("true")) return Boolean.TRUE;
      if (takeLiteral("false")) return Boolean.FALSE;
      return number();
    }

    private Object arrayValue() {
      if (cursor < input.length() && input.charAt(cursor) == '"') return string();
      if (cursor < input.length() && input.charAt(cursor) == '{') return object();
      if (cursor < input.length() && input.charAt(cursor) == '[') return value();
      if (takeLiteral("true")) return Boolean.TRUE;
      if (takeLiteral("false")) return Boolean.FALSE;
      return number();
    }

    private java.math.BigDecimal number() {
      int start = cursor;
      while (cursor < input.length() && "-+0123456789.eE".indexOf(input.charAt(cursor)) >= 0) cursor++;
      String token = input.substring(start, cursor);
      if (!token.matches("-?(?:0|[1-9][0-9]*)(?:\\.[0-9]+)?(?:[eE][+-]?[0-9]+)?")) throw invalid();
      try { return new java.math.BigDecimal(token); }
      catch (NumberFormatException error) { throw invalid(); }
    }

    private boolean takeLiteral(String literal) {
      if (!input.startsWith(literal, cursor)) return false;
      cursor += literal.length();
      return true;
    }

    private String string() {
      expect('"');
      StringBuilder value = new StringBuilder();
      while (cursor < input.length()) {
        char character = input.charAt(cursor++);
        if (character == '"') {
          validateSurrogates(value);
          return value.toString();
        }
        if (character < 0x20) throw invalid();
        if (character == '\\') {
          if (cursor == input.length()) throw invalid();
          char escape = input.charAt(cursor++);
          switch (escape) {
            case '"', '\\', '/' -> value.append(escape);
            case 'b' -> value.append('\b');
            case 'f' -> value.append('\f');
            case 'n' -> value.append('\n');
            case 'r' -> value.append('\r');
            case 't' -> value.append('\t');
            case 'u' -> value.append(unicodeEscape());
            default -> throw invalid();
          }
        } else {
          value.append(character);
        }
      }
      throw invalid();
    }

    private char unicodeEscape() {
      if (input.length() - cursor < 4) throw invalid();
      int value = 0;
      for (int digit = 0; digit < 4; digit++) {
        int nibble = Character.digit(input.charAt(cursor++), 16);
        if (nibble < 0) throw invalid();
        value = (value << 4) | nibble;
      }
      return (char) value;
    }

    private void validateSurrogates(CharSequence value) {
      for (int index = 0; index < value.length(); index++) {
        char character = value.charAt(index);
        if (Character.isHighSurrogate(character)) {
          if (++index >= value.length() || !Character.isLowSurrogate(value.charAt(index))) throw invalid();
        } else if (Character.isLowSurrogate(character)) {
          throw invalid();
        }
      }
    }

    private void whitespace() {
      while (cursor < input.length()) {
        char character = input.charAt(cursor);
        if (character != ' ' && character != '\t' && character != '\n' && character != '\r') return;
        cursor++;
      }
    }

    private void expect(char expected) {
      if (!take(expected)) throw invalid();
    }

    private boolean take(char expected) {
      if (cursor < input.length() && input.charAt(cursor) == expected) {
        cursor++;
        return true;
      }
      return false;
    }

    private boolean atEnd() { return cursor == input.length(); }
    private IllegalArgumentException invalid() { return new IllegalArgumentException("invalid JSON at offset " + cursor); }
  }
}
