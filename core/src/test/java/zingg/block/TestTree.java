package zingg.block;

import org.junit.*;
import static org.junit.Assert.*;

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
