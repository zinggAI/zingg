package io.zingg.duckdb.protocol;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Versioned, delimiter-safe payload codec for operation-specific field lists. */
public final class PayloadCodec {
  private static final String VERSION = "v1";
  private PayloadCodec() {}

  public static String encode(String... fields) {
    if (fields == null) throw new IllegalArgumentException("payload fields are required");
    var out = new StringBuilder(VERSION).append('.').append(fields.length);
    for (String field : fields) {
      if (field == null) throw new IllegalArgumentException("payload fields cannot be null");
      out.append('.').append(Base64.getUrlEncoder().withoutPadding()
          .encodeToString(field.getBytes(StandardCharsets.UTF_8)));
    }
    return out.toString();
  }

  public static String[] decode(String payload, int expectedFields) {
    if (payload == null) throw new IllegalArgumentException("payload is required");
    String[] parts = payload.split("\\.", -1);
    if (parts.length < 2 || !VERSION.equals(parts[0]))
      throw new IllegalArgumentException("unsupported payload version");
    int count;
    try { count = Integer.parseInt(parts[1]); }
    catch (NumberFormatException e) { throw new IllegalArgumentException("invalid payload field count", e); }
    if (count != parts.length - 2 || (expectedFields >= 0 && count != expectedFields))
      throw new IllegalArgumentException("unexpected payload field count");
    String[] result = new String[count];
    for (int i=0;i<count;i++) {
      try { result[i] = new String(Base64.getUrlDecoder().decode(parts[i+2]), StandardCharsets.UTF_8); }
      catch (IllegalArgumentException e) { throw new IllegalArgumentException("invalid encoded payload field", e); }
    }
    return result;
  }
}
