package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.zingg.duckdb.api.DuckException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SimilarityRegistryTest {
  @Test
  void registeredSimilarityInventoryIsStable() {
    assertEquals(Set.of("exact", "jaccard", "normalized_levenshtein", "jaro", "jaro_winkler"),
        new SimilarityRegistry().names());
  }

  @Test
  void strictJaccardVectorsPreserveReleasedNormalization() {
    var registry = new SimilarityRegistry();
    assertEquals(1.0, registry.apply("jaccard", null, "value"));
    assertEquals(1.0, registry.apply("jaccard", "", "value"));
    assertEquals(1.0, registry.apply("jaccard", "Acme, Inc.", "acme inc"));
    assertEquals(1.0 / 3.0, registry.apply("jaccard", "acme north", "acme south"), 1e-12);
    assertEquals(0.0, registry.apply("jaccard", "alpha", "beta"));
    assertEquals(1.0, registry.apply("jaccard", "item42", "item 42"),
        "SecondString tokenizes letter and digit runs separately");
    assertEquals(1.0, registry.apply("jaccard", "a-b", "a b"));
  }

  @Test
  void jaccardLocaleMatrixMakesDefaultLocaleCaseFoldingExplicit() {
    var original = java.util.Locale.getDefault();
    try {
      java.util.Locale.setDefault(java.util.Locale.ENGLISH);
      assertEquals(0.0, SimilarityFunctions.jaccard("I", "ı"),
          "English lowercasing keeps dotted and dotless i distinct");
      assertEquals(0.0, SimilarityFunctions.jaccard("İstanbul", "istanbul"),
          "English lowercasing splits at the combining-dot token boundary");

      java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"));
      assertEquals(1.0, SimilarityFunctions.jaccard("I", "ı"));
      assertEquals(1.0, SimilarityFunctions.jaccard("İstanbul", "istanbul"));
      assertEquals(1.0, SimilarityFunctions.jaccard("I-42", "ı 42"),
          "punctuation and letter/digit boundaries must remain stable under Turkish casing");
    } finally {
      java.util.Locale.setDefault(original);
    }
  }

  @Test
  void safeBlankAndNullJaccardBehaviorIsExplicitlyOne() {
    assertEquals(1.0, SimilarityFunctions.jaccard(null, null));
    assertEquals(1.0, SimilarityFunctions.jaccard(null, "value"));
    assertEquals(1.0, SimilarityFunctions.jaccard("value", null));
    assertEquals(1.0, SimilarityFunctions.jaccard("   ", "value"));
    assertEquals(1.0, SimilarityFunctions.jaccard("value", "\t\n"));
  }

  @Test
  void jaccardUsesSetSemanticsForRepeatedAndHashCollidingTokens() {
    assertEquals("Aa".hashCode(), "BB".hashCode(), "fixture must exercise a Java hash collision");
    assertEquals(1.0, SimilarityFunctions.jaccard("alpha alpha alpha", "alpha"));
    assertEquals(1.0 / 3.0,
        SimilarityFunctions.jaccard("alpha beta alpha", "alpha gamma"), 1e-12);
    assertEquals(0.0, SimilarityFunctions.jaccard("Aa", "BB"),
        "distinct colliding tokens must remain distinct set members");
    assertEquals(1.0 / 3.0,
        SimilarityFunctions.jaccard("Aa BB", "Aa cc"), 1e-12);
  }

  @Test
  void nonBlankJaccardScoresAreFiniteSymmetricAndBounded() {
    List<String> values = List.of("alpha", "alpha beta", "42 cats", "cats42",
        "İstanbul", "a-b", "a_b", "repeated repeated token");
    for (String left : values) {
      for (String right : values) {
        double forward = SimilarityFunctions.jaccard(left, right);
        double reverse = SimilarityFunctions.jaccard(right, left);
        assertTrue(Double.isFinite(forward), () -> "non-finite score for " + left + " / " + right);
        assertTrue(forward >= 0.0 && forward <= 1.0,
            () -> "score outside [0,1] for " + left + " / " + right + ": " + forward);
        assertEquals(forward, reverse, 1e-12,
            () -> "asymmetric score for " + left + " / " + right);
      }
    }
  }

  @Test
  void releasedJaroWinklerDelegatesToJaro() {
    var registry = new SimilarityRegistry();
    assertEquals(registry.apply("jaro", "MARTHA", "MARHTA"),
        registry.apply("jaro_winkler", "MARTHA", "MARHTA"), 1e-12);
    assertEquals(1.0, registry.apply("jaro", "same", "same"));
    assertEquals(0.0, registry.apply("jaro", null, "same"));
  }

  @Test
  void unsupportedSimilarityFailsExplicitly() {
    assertThrows(DuckException.class, () -> new SimilarityRegistry().apply("cosine", "a", "b"));
  }
}
