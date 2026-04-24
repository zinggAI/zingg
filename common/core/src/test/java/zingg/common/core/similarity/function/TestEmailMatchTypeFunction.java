package zingg.common.core.similarity.function;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static zingg.common.core.similarity.function.SimilarityFunctionTestHelper.*;

/**
 * Test class for EmailMatchTypeFunction.
 */
public class TestEmailMatchTypeFunction {

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
	 * Test exact email match.
	 */
	public void testBothExact(SimilarityFunction function) {
		String first = "xyz321@pqr.co";
		String second = "xyz321@pqr.co";
		assertEquals(1.0, function.call(first, second),
				"Exact email match should return 1.0");
	}

	/**
	 * Test same username with different domain.
	 */
	public void testFirstPartMatch(SimilarityFunction function) {
		String first = "pqr981@abc.in";
		String second = "pqr981@xyz.com";
		assertEquals(1.0, function.call(first, second),
				"Same username with different domain should return 1.0");
	}

	/**
	 * Test different usernames - should delegate to AffineGap.
	 */
	public void testbothDifferent(SimilarityFunction emailFunc, SimilarityFunction affineGapFunc) {
		String first = "xyz321@pqr.co";
		String second = "pqr981@abc.in";
		assertEquals(affineGapFunc.call(first.split("@", 0)[0], second.split("@", 0)[0]),
				emailFunc.call(first, second),
				"Different usernames should use AffineGap comparison");
	}

	/**
	 * Test different usernames with same domain - should delegate to AffineGap.
	 */
	public void testFirstPartDifferentSecondPartMatch(SimilarityFunction emailFunc, SimilarityFunction affineGapFunc) {
		String first = "pqr981@xyz.com";
		String second = "aqr981@xyz.com";
		assertEquals(affineGapFunc.call(first.split("@", 0)[0], second.split("@", 0)[0]),
				emailFunc.call(first, second),
				"Different usernames should use AffineGap comparison even with same domain");
	}

	/**
	 * Test first entry empty.
	 */
	public void testFirstEntryEmpty(SimilarityFunction emailFunc, SimilarityFunction affineGapFunc) {
		String first = "";
		String second = "xyz321@pqr.co";
		assertEquals(affineGapFunc.call(first.split("@", 0)[0], second.split("@", 0)[0]),
				emailFunc.call(first, second));
	}

	/**
	 * Test second entry empty.
	 */
	public void testSecondEntryEmpty(SimilarityFunction emailFunc, SimilarityFunction affineGapFunc) {
		String first = "xyz321@pqr.co";
		String second = "";
		assertEquals(affineGapFunc.call(first.split("@", 0)[0], second.split("@", 0)[0]),
				emailFunc.call(first, second));
	}

	@Test
	public void testCommonBehavior() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		testNullAndEmptyHandling((a, b) -> emailMatchFn.call(a, b));
		testExactMatches((a, b) -> emailMatchFn.call(a, b));
	}

	@Test
	public void testBothExact() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		testBothExact((a, b) -> emailMatchFn.call(a, b));
	}

	@Test
	public void testFirstPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		testFirstPartMatch((a, b) -> emailMatchFn.call(a, b));
	}

	@Test
	public void testbothDifferent() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		AffineGapSimilarityFunction affineGap = new AffineGapSimilarityFunction();
		testbothDifferent((a, b) -> emailMatchFn.call(a, b), (a, b) -> affineGap.call(a, b));
	}

	@Test
	public void testFirstPartDifferentSecondPartMatch() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		AffineGapSimilarityFunction affineGap = new AffineGapSimilarityFunction();
		testFirstPartDifferentSecondPartMatch((a, b) -> emailMatchFn.call(a, b), (a, b) -> affineGap.call(a, b));
	}

	@Test
	public void testFirstEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		AffineGapSimilarityFunction affineGap = new AffineGapSimilarityFunction();
		testFirstEntryEmpty((a, b) -> emailMatchFn.call(a, b), (a, b) -> affineGap.call(a, b));
	}

	@Test
	public void testSecondEntryEmpty() {
		EmailMatchTypeFunction emailMatchFn = new EmailMatchTypeFunction();
		AffineGapSimilarityFunction affineGap = new AffineGapSimilarityFunction();
		testSecondEntryEmpty((a, b) -> emailMatchFn.call(a, b), (a, b) -> affineGap.call(a, b));

	}
}
