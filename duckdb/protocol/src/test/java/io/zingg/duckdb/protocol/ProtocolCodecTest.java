package io.zingg.duckdb.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ProtocolCodecTest {
  @Test
  void roundTripPreservesEscapedFields() {
    var original = new WorkerMessage("request-1", "match.run", "{\"text\":\"line1\nline2\tvalue\\x\"}");
    assertEquals(original, ProtocolCodec.decode(ProtocolCodec.encode(original)));
  }

  @Test
  void encodingMatchesCrossLanguageWireGolden() {
    var message = new WorkerMessage("golden", "ping", "slash\\tab\tline\nunicode-λ\rend");
    assertEquals("golden\tping\tslash\\\\tab\\tline\\nunicode-λ\rend", ProtocolCodec.encode(message));
    assertEquals(message, ProtocolCodec.decode(ProtocolCodec.encode(message)));
  }

  @Test
  void rejectsMalformedOperationsAndMessages() {
    assertThrows(IllegalArgumentException.class, () -> ProtocolCodec.requireOperation("match/run"));
    assertThrows(IllegalArgumentException.class, () -> ProtocolCodec.decode("only-two-fields\tmatch.run"));
    assertThrows(IllegalArgumentException.class, () -> ProtocolCodec.decode("id\tbad/op\tpayload"));
  }

  @Test
  void enforcesLineSizeLimit() {
    String oversized = "id\tmatch\t" + "x".repeat(ProtocolCodec.MAX_LINE_CHARS);
    assertThrows(IllegalArgumentException.class, () -> ProtocolCodec.decode(oversized));
  }

  @Test
  void payloadCodecPreservesDelimiterRichPathsAndExpressions() {
    var fields = new String[] {"/tmp/a|b;part.csv", "a || b", "C:\\data\\x.y"};
    assertArrayEquals(fields, PayloadCodec.decode(PayloadCodec.encode(fields), -1));
  }
}
