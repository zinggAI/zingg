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

package zingg.common.core.similarity.function;

import java.util.Arrays;

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
