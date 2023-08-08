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

import static org.junit.jupiter.api.Assertions.assertEquals;

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
		assertEquals(0.5d, score);
	}

	@Test
	public void testSpecificInputsDifferent() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "sonicwall client/server ";
		String second = "sonicwall businesses ";
		Double score = strDistanceFn.call(first, second);
		assertEquals(0.25d, score);
	}

	@Test
	public void testInputsSameWithSlashes() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "sample/string";
		String second = "sample/string";
		Double score = strDistanceFn.call(first, second);
		assertEquals(1d, score);
	}

	@Test
	public void testInputsDifferentWithSlashesAndColons() {
		StringSimilarityDistanceFunction strDistanceFn = new JaccSimFunction("test");
		String first = "slashes/and:colons.,";
		String second = "slashes and colons";
		Double score = strDistanceFn.call(first, second);
		assertEquals(1d, score);
	}
}
