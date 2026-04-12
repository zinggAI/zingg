package zingg.common.core.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimilarityFunctionTestHelper {

    /**
     * Functional interface for calling similarity functions.
     */
    @FunctionalInterface
    public interface SimilarityFunction {
        Double call(String first, String second);
    }

    /**
     * Test null and empty handling - used by ALL similarity functions.
     */
    public static void testNullAndEmptyHandling(SimilarityFunction function) {
        assertEquals(1d, function.call(null, null), "Both null should return 1.0");
        assertEquals(1d, function.call(null, "test"), "First null should return 1.0");
        assertEquals(1d, function.call("test", null), "Second null should return 1.0");
        assertEquals(1d, function.call("", ""), "Both empty should return 1.0");
        assertEquals(1d, function.call("", "test"), "First empty should return 1.0");
        assertEquals(1d, function.call("test", ""), "Second empty should return 1.0");
    }

    /**
     * Test exact matches - used by ALL similarity functions.
     */
    public static void testExactMatches(SimilarityFunction function) {
        assertEquals(1d, function.call("test", "test"), "Exact match should return 1.0");
        assertEquals(1d, function.call("testing similarity", "testing similarity"),
                "Exact match of longer string should return 1.0");
    }
}
