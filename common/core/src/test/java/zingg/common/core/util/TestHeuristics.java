/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.util;

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
