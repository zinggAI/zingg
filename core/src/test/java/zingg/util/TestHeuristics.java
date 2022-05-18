package zingg.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TestHeuristics {
	long blockSizeConfigured = 50L;

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsMoreThanConfigValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(70000, blockSizeConfigured);
		assertEquals(size, 50);
	}

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsLessThanConfigValueButMoreThanMinValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(25000, blockSizeConfigured);
		assertEquals(size, 25);
	}

	@Test
	public void testMaxBlockSizeWhenCalculatedValueIsLessThanMinValue() throws Throwable {
		long size = Heuristics.getMaxBlockSize(5000, blockSizeConfigured);
		assertEquals(size, Heuristics.MIN_SIZE);
	}

	@Test
	public void testMaxBlockSizeWhenConfigValueItselfIsLessThanMinValue() throws Throwable {
		long blockSizeConfigured = 5L;
		long size = Heuristics.getMaxBlockSize(25000, blockSizeConfigured);
		assertEquals(size, Heuristics.MIN_SIZE);
	}
}
