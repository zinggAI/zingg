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

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDoubleSimilarityFunction {


	public TestDoubleSimilarityFunction() throws URISyntaxException {
	}

	@Test
	public void testFirstNumsimFn() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(null, 1d));
	}

	@Test
	public void testFirstNumIsNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(Double.NaN, 1d));
	}

	@Test
	public void testSecondNumsimFn() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(1d, null));
	}

	@Test
	public void testSecondNumIsNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(1d, Double.NaN));
	}
	@Test
	public void testBothNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(Double.NaN, Double.NaN));
	}

	@Test
	public void testBothNull() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(null, null));
	}

	@Test
	public void testBothNotNullNorNAN() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(1d, 1d));
	}

	@Test
	public void testValues0And0() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(1d, simFn.call(0d, 0d));
	}

	@Test
	public void testValues10And9() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0.95d, simFn.call(10d, 9d), 0.01d);
	}

	@Test
	public void testValues1And8() {
		DoubleSimilarityFunction simFn = new DoubleSimilarityFunction();
		assertEquals(0.3d, simFn.call(1.0, 8d), 0.01d);
	}

}
