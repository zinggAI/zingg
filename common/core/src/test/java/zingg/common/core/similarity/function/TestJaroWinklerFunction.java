package zingg.common.core.similarity.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zingg.common.core.similarity.function.SimilarityFunctionTestHelper.SimilarityFunction;

public class TestJaroWinklerFunction {

    public void testNullAndEmptyHandling(SimilarityFunction function) {
        SimilarityFunctionTestHelper.testNullAndEmptyHandling(function);
    }

    public void testExactMatches(SimilarityFunction function) {
        SimilarityFunctionTestHelper.testExactMatches(function);
    }

    public void testKnownCases(SimilarityFunction function) {
        // Transposition
        assertTrue(function.call("MARTHA", "MARHTA") > 0.9);
        // Homophones
        assertTrue(function.call("DWAYNE", "DUANE") > 0.8);
        // No similarity
        assertEquals(0.0, function.call("abc", "xyz"), 1e-6);
        // Partial match
        assertTrue(function.call("CRATE", "TRACE") > 0.7);
        // Reverse
        assertTrue(function.call("CRATE", "ETARC") > 0.5);
    }

    public void testPrefix(SimilarityFunction function) {
        // Jaro-Winkler gives a higher score for common prefixes
        double score1 = function.call("prefix", "prefixation");
        double score2 = function.call("prefix", "suffix");
        assertTrue(score1 > score2, "Prefix should apply");
    }

    @Test
    public void testCommonBehavior() {
        JaroWinklerFunction sim = new JaroWinklerFunction();
        testNullAndEmptyHandling((s1, s2) -> sim.call(s1, s2));
        testExactMatches((s1, s2) -> sim.call(s1, s2));
        testKnownCases((s1, s2) -> sim.call(s1, s2));
        testPrefix((s1, s2) -> sim.call(s1, s2));
    }
}

