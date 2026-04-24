package zingg.common.core.similarity.function;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static zingg.common.core.similarity.function.SimilarityFunctionTestHelper.*;

/**
 * Test class for OnlyAlphabetsAffineGapSimilarity.
 */
public class TestOnlyAlphabetsAffineGapSimilarity {

	/**
	 * Test null and empty handling.
	 */
	public void testNullAndEmptyHandling(SimilarityFunction function) {
		SimilarityFunctionTestHelper.testNullAndEmptyHandling(function);
	}

	/**
	 * Test exact matches.
	 */
	public void testExactMatches(SimilarityFunction function) {
		SimilarityFunctionTestHelper.testExactMatches(function);
	}

	/**
	 * Test that strings with same alphabets but different numbers match perfectly.
	 */
	public void testSameAlphabetsDiffNumbers(SimilarityFunction function) {
		double score = function.call("I have 1 number", "I have 3 number");
		assertEquals(1.0, score, "Same alphabets with different numbers should match");
	}

	/**
	 * Test that strings with different alphabets don't match perfectly.
	 */
	public void testNotSameAlphabets(SimilarityFunction function) {
		double score = function.call("I have 1 number", "I have no number");
		assertTrue(score < 1.0, "Different alphabets should have score < 1.0");
		assertTrue(score > 0.0, "Different alphabets should have score > 0.0");
	}

	/**
	 * Test same strings without numbers.
	 */
	public void testSameNoNum(SimilarityFunction function) {
		assertEquals(1.0, function.call("I have no number", "I have no number"),
				"Same strings without numbers should match");
	}

	/**
	 * Test different strings without numbers.
	 */
	public void testDiffNoNumber(SimilarityFunction function) {
		assertTrue(function.call("I have a no number", "I have r number") < 1.0,
				"Different alphabets should have score < 1.0");
	}

	@Test
	public void testCommonBehavior() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		testNullAndEmptyHandling((s1, s2) -> sim.call(s1, s2));
		testExactMatches((s1, s2) -> sim.call(s1, s2));
	}

	@Test
	public void testSameAlphabetsDiffNumbers() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		testSameAlphabetsDiffNumbers((s1, s2) -> sim.call(s1, s2));
	}

	@Test
	public void testNotSameAlphabets() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		testNotSameAlphabets((s1, s2) -> sim.call(s1, s2));
	}

	@Test
	public void testSameNoNum() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		testSameNoNum((s1, s2) -> sim.call(s1, s2));
	}

	@Test
	public void testDiffNoNumber() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		testDiffNoNumber((s1, s2) -> sim.call(s1, s2));
	}
}