package io.zingg.duckdb.engine;

import io.zingg.duckdb.api.DuckException;
import java.util.Set;
import java.util.regex.Pattern;

public final class SqlSafety {
  private static final Pattern DISALLOWED = Pattern.compile(
      "(?is)\\b(ATTACH|DETACH|COPY|INSTALL|LOAD|PRAGMA|CREATE|DROP|ALTER|INSERT|UPDATE|DELETE|EXPORT|IMPORT)\\b");
  private static final Pattern MODEL_NUMBER = Pattern.compile("(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)(?:[eE][+-]?[0-9]+)?");
  private static final Set<String> MODEL_FUNCTIONS = Set.of("COALESCE", "EXP", "LIST_SUM", "LIST_VALUE");
  private static final int MAX_MODEL_EXPRESSION_LENGTH = 1_048_576;

  private SqlSafety() {}

  public static String predicate(String expression) {
    if (expression == null || expression.isBlank()) return "TRUE";
    String value = expression.trim();
    var blocked = DISALLOWED.matcher(value);
    String token = blocked.find() ? blocked.group(1) : null;
    if (value.length() > 4096 || value.contains(";") || value.contains("--")
        || value.contains("/*") || value.contains("*/") || token != null) {
      throw new DuckException(token == null ? "predicate contains disallowed SQL"
          : "predicate contains disallowed SQL token: " + token);
    }
    return value;
  }

  /**
   * Validates the limited expression language emitted from a validated classifier artifact.
   * It intentionally allows no statements, comments, casts, subqueries, or arbitrary functions.
   */
  public static String modelNumericExpression(String expression) {
    if (expression == null || expression.isBlank() || expression.length() > MAX_MODEL_EXPRESSION_LENGTH)
      throw new DuckException("model score expression is empty or exceeds the size limit");
    int depth = 0;
    for (int i = 0; i < expression.length();) {
      char c = expression.charAt(i);
      if (Character.isWhitespace(c)) { i++; continue; }
      if ((c == '-' && i + 1 < expression.length() && expression.charAt(i + 1) == '-')
          || (c == '/' && i + 1 < expression.length() && expression.charAt(i + 1) == '*')
          || (c == '*' && i + 1 < expression.length() && expression.charAt(i + 1) == '/'))
        throw new DuckException("model score expression cannot contain SQL comments");
      if (c == '"') {
        i++;
        boolean closed = false;
        while (i < expression.length()) {
          if (expression.charAt(i++) == '"') {
            if (i < expression.length() && expression.charAt(i) == '"') { i++; continue; }
            closed = true;
            break;
          }
        }
        if (!closed) throw new DuckException("model score expression has an unterminated identifier");
        continue;
      }
      if (Character.isDigit(c) || c == '.') {
        var number = MODEL_NUMBER.matcher(expression);
        number.region(i, expression.length());
        if (!number.lookingAt()) throw new DuckException("model score expression contains an invalid number");
        i = number.end();
        continue;
      }
      if (Character.isLetter(c) || c == '_') {
        int start = i++;
        while (i < expression.length()
            && (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) i++;
        String function = expression.substring(start, i).toUpperCase(java.util.Locale.ROOT);
        if (!MODEL_FUNCTIONS.contains(function))
          throw new DuckException("model score expression contains an unsupported token");
        continue;
      }
      if (c == '(') depth++;
      if (c == ')' && --depth < 0) throw new DuckException("model score expression has unbalanced parentheses");
      if (!"+-*/(),".contains(String.valueOf(c)))
        throw new DuckException("model score expression contains a disallowed character");
      i++;
    }
    if (depth != 0) throw new DuckException("model score expression has unbalanced parentheses");
    return expression;
  }

  public static String readOnlyQuery(String query) {
    if (query == null || query.isBlank()) throw new DuckException("query is empty");
    String value = query.trim();
    if (!value.matches("(?is)^(WITH\\b[\\s\\S]+|SELECT\\b[\\s\\S]+)$"))
      throw new DuckException("only SELECT/WITH queries are allowed");
    return predicate(value);
  }
}
