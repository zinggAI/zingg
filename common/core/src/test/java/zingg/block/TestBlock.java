package zingg.block;


public class TestBlock {

	/*
	@Test
	public void testCheckFunctionInNodeExists() {
		ScriptFunction sf = mock(ScriptFunction.class);
		FieldDefinition field = new FieldDefinition(1, MatchType.ALPHANUMERIC,
				DataType.INT);
		HashFunctionContext context = new HashFunctionContext(field);
		//Canopy can = new Canopy(null, null, sf, context);
		/*
		Block block = new Block(new ArrayList<Tuple>(), new ArrayList<Dupe>());
		assertTrue(block.checkFunctionInNode(can, 1, sf));
		assertFalse(block.checkFunctionInNode(can, 5, sf));
		ScriptFunction sf1 = mock(ScriptFunction.class);
		assertFalse(block.checkFunctionInNode(can, 1, sf1));
		*/
	//}

	/*
	 * public boolean checkFunctionInNode(Canopy node, int index, RFunction
	 * function) { if (node.getFunction() != null &&
	 * node.getFunction().equals(function) && node.getFieldNumber() == index) {
	 * return true; } return false; }
	 */

	/*
	 * public boolean isFunctionUsed(Tree<Canopy> tree, Canopy node, int index,
	 * RFunction function) { LOG.info("Tree " + tree); LOG.info("Node  " +
	 * node); LOG.info("Index " + index); LOG.info("Function " + function);
	 * boolean isUsed = false; if (node == null || tree == null) return false;
	 * if (checkFunctionInNode(node, index, function)) return true; Tree<Canopy>
	 * nodeTree = tree.getTree(node); if (nodeTree == null) return false;
	 * 
	 * Tree<Canopy> parent = nodeTree.getParent(); Canopy head =
	 * parent.getHead(); while (head != null) { //check siblings of node for
	 * (Tree<Canopy> siblings : parent.getSubTrees()) { Canopy sibling =
	 * siblings.getHead(); if (checkFunctionInNode(sibling, index, function))
	 * return true; } //check parent of node return isFunctionUsed(tree, head,
	 * index, function); } return isUsed; }
	 */
}
