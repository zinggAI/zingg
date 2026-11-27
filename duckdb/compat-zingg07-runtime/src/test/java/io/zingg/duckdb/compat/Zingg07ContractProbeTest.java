package io.zingg.duckdb.compat;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

class Zingg07ContractProbeTest {
  @Test
  @EnabledIfSystemProperty(named = "zingg07.contract.profile", matches = "true")
  void pinnedClientCapsuleExposesRequiredGenericContracts() throws Exception {
    String jar = System.getProperty("zingg07.contract.jar");
    assertNotNull(jar, "zingg07.contract.jar must point to the pinned capsule");
    String[] contracts = {
        "zingg.common.client.ZFrame",
        "zingg.common.client.util.DSUtil",
        "zingg.common.client.util.PipeUtilBase",
        "zingg.common.client.util.PipeUtil",
        "zingg.common.client.util.ClientUtils",
        "zingg.common.client.MatchType",
        "zingg.common.client.FieldDefinition"
    };
    try (var loader = new URLClassLoader(new URL[] {Path.of(jar).toUri().toURL()}, getClass().getClassLoader())) {
      for (String contract : contracts) {
        assertDoesNotThrow(() -> Class.forName(contract, false, loader), contract);
      }
    }
  }

  @Test
  @EnabledIfSystemProperty(named = "zingg07.contract.profile", matches = "true")
  void pinnedMatchTypeInventoryIsExplicit() throws Exception {
    String jar = System.getProperty("zingg07.contract.jar");
    assertNotNull(jar, "zingg07.contract.jar must point to the pinned capsule");
    try (var loader = new URLClassLoader(new URL[] {Path.of(jar).toUri().toURL()}, getClass().getClassLoader())) {
      var matchTypes = Class.forName("zingg.common.client.MatchTypes", true, loader);
      var names = (String[]) matchTypes.getMethod("getAllMatchTypes").invoke(null);
      assertEquals(Set.of("FUZZY", "EXACT", "PINCODE", "EMAIL", "TEXT", "NUMERIC",
          "NUMERIC_WITH_UNITS", "NULL_OR_BLANK", "ONLY_ALPHABETS_EXACT", "ONLY_ALPHABETS_FUZZY", "DONT_USE"),
          Set.copyOf(Arrays.asList(names)));
      assertEquals(11, names.length, "upstream MatchType identifiers must not silently duplicate");
    }
  }
}
