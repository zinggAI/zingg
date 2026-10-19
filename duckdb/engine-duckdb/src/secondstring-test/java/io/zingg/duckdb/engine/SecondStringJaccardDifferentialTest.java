package io.zingg.duckdb.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wcohen.ss.Jaccard;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/** Differential corpus against the exact SecondString jar shipped by Zingg v0.7.0. */
class SecondStringJaccardDifferentialTest {
  private static final List<String> CORPUS = Arrays.asList(
      null, "", " ", "\t\n", "\u2003", ".,;:!?", "-_/", "a", "A", "a-b", "a_b",
      "a b", "item42", "item 42", "sample/string", "Acme, Inc.", "acme inc",
      "alpha alpha beta", "alpha gamma", "Aa", "BB", "İstanbul", "istanbul", "I", "ı",
      "I-42", "ı 42", "e\u0301", "é", "中文", "ไทย", "emoji 😀", "emoji 😃",
      "123 456", "123-456", "\u0000value");

  @Test
  void currentExactProfileMatchesSecondStringAcrossCorpusAndLocales() {
    Locale original = Locale.getDefault();
    try {
      for (Locale locale : List.of(Locale.ENGLISH, Locale.forLanguageTag("tr-TR"))) {
        Locale.setDefault(locale);
        Jaccard oracle = new Jaccard();
        for (String left : CORPUS) {
          for (String right : CORPUS) {
            double expected = zinggV07Call(oracle, left, right);
            double actual = SimilarityFunctions.secondStringJaccard(left, right);
            assertEquals(expected, actual, 1e-15,
                () -> locale + ": " + printable(left) + " / " + printable(right));
          }
        }
      }
    } finally {
      Locale.setDefault(original);
    }
  }

  private static double zinggV07Call(Jaccard oracle, String left, String right) {
    if (left == null || left.isEmpty() || right == null || right.isEmpty() || left.equals(right)) {
      return 1d;
    }
    double score = oracle.score(left, right);
    return Double.isNaN(score) ? 0d : score;
  }

  private static String printable(String value) {
    return value == null ? "<null>" : value.replace("\n", "\\n").replace("\t", "\\t");
  }
}
