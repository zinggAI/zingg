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

package zingg.block;

import org.junit.jupiter.api.*;

import zingg.common.core.block.Tree;

import static org.junit.jupiter.api.Assertions.*;

public class TestTree {

	@Test
	public void testParent() {
		Tree<Integer> tree = new Tree<Integer>();
		Tree<Integer> root = tree.addLeaf(0);
		Tree<Integer> one = root.addLeaf(1);
		Tree<Integer> two = root.addLeaf(2);
		Tree<Integer> oneA = one.addLeaf(11);
		Tree<Integer> oneB = one.addLeaf(12);
		Tree<Integer> oneAA = oneA.addLeaf(111);
		Tree<Integer> oneAB = oneA.addLeaf(112);
	}
}
