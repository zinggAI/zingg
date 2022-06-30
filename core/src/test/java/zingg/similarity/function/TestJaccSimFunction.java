package zingg.similarity.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class TestJaccSimFunction {

	@Test
	public void testFirstStringNull() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call(null, "text 2"));
	}

	@Test
	public void testFirstStringEmpty() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("", "text 2"));
	}

	@Test
	public void testSecondStringNull() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("text 1", null));
	}

	@Test
	public void testSecondStringEmpty() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("text 1", ""));
	}

	@Test
	public void testBothEmpty() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("", ""));
	}

	@Test
	public void testBothNull() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call(null, null));
	}

	@Test
	public void testBothSame() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("sample text", "sample text"));
	}

	@Test
	public void testBothSameButCaseDifferent() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		assertEquals(1d, strDistanceFn.call("sample text", "sAmPle TeXt"));
	}

	@Test
	public void testBothNotEmptyDifferent() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		Double score = strDistanceFn.call("sample text first", "sample text second");
		assertFalse(score == 0d || score == 1d);
	}

	@Test
	public void testSpecificInputsDifferent() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "sonicwall 01-ssc-6997 : usually ships in 24 hours : : sonicwall client/server anti-virus suite leverages the award-winning mcafee netshield and groupshield applications for networks with windows -based file print and exchange servers.,";
		String second = "sonicwall 01-ssc-5670 : usually ships in 24 hours : : more and more businesses schools government agencies and libraries are connecting to the internet to meet their organizational and educational goals.";
		Double score = strDistanceFn.call(first, second);
		assertFalse(score == 0d || score == 1d);
	}

	@Test
	public void testInputsSameWithSlashes() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "sample/string/with/slashes";
		String second = "sample/string/with/slashes";
		Double score = strDistanceFn.call(first, second);
		assertEquals(1d, score);
	}

	@Test
	public void testInputsDifferentWithSlashesAndColons() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "sample/string/with/slashes:and:colons.,";
		String second = "sample string/with slash:and,.";
		Double score = strDistanceFn.call(first, second);
		assertFalse(score == 0d || score == 1d);
	}
}
