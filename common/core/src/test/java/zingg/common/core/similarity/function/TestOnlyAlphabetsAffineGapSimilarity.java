package zingg.common.core.similarity.function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestOnlyAlphabetsAffineGapSimilarity {
	
	
	@Test
	public void testNotSameAlhpabets() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		double score = sim.call("I have 1 number", "I have no number");
		assertTrue(1 > score);
		assertTrue(0 < score);
	}

	@Test
	public void testSameAlphabetsDiffNumbers() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		double score = sim.call("I have 1 number", "I have 3 number");
		assertEquals(1d, score);
	}
	
	@Test
	public void testSameNoNum() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		assertEquals(1d, sim.call("I have no number", "I have no number"));
	}

	@Test
	public void testDiffNoNumber() {
		OnlyAlphabetsAffineGapSimilarity sim = new OnlyAlphabetsAffineGapSimilarity();
		System.out.println(" the sim i s" + sim.call("I have a no number", "I have r number"));
		assertTrue(1d > sim.call("I have a no number", "I have r number"));
	}	
}
