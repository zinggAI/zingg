package zingg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestHeuristics {
	long maxBlockSizeConfigured = 50L;

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsMoreThanConfigValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(70000, maxBlockSizeConfigured);
		assertEquals(size, 50);
	}

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsLessThanConfigValueButMoreThanMinValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(25000, maxBlockSizeConfigured);
		assertEquals(size, 25);
	}

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsLessThanMinValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(5000, maxBlockSizeConfigured);
		assertEquals(size, Heuristics.MIN_SIZE);
	}

	@Test
	public void testMaxBlockSizeWhenConfigValueItselfIsLessThanMinValue() throws Throwable {
		long maxBlockSizeConfigured = 5L;
		long size = Heuristics.getMaxBlockSize(25000, maxBlockSizeConfigured);
		assertEquals(size, Heuristics.MIN_SIZE);
	}
}
