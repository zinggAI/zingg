package io.zingg.duckdb.api;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ConnectorPolicyTest {
  @Test
  void offlinePolicyRejectsEveryExtension() {
    assertThrows(DuckException.class, () -> ConnectorPolicy.offline().requireAllowed("httpfs"));
  }

  @Test
  void allowlistIsCaseNormalizedAtUse() {
    var policy = new ConnectorPolicy(false, java.util.Set.of("httpfs"));
    assertDoesNotThrow(() -> policy.requireAllowed("HTTPFS"));
    assertThrows(DuckException.class, () -> policy.requireAllowed("sqlite"));
  }

  @Test
  void configuredAllowlistIsCaseNormalized() {
    var policy = new ConnectorPolicy(false, java.util.Set.of("HTTPFS"));
    assertDoesNotThrow(() -> policy.requireAllowed("httpfs"));
  }

  @Test
  void invalidExtensionNamesAreRejected() {
    assertThrows(IllegalArgumentException.class, () -> new ConnectorPolicy(false, java.util.Set.of("http-fs")));
  }
}
