package zingg.common.core.block;

import zingg.common.client.FieldDefinition;
import zingg.common.core.hash.HashFunction;

public class DefaultHashFunctionUtility<D, R, C, T> implements IHashFunctionUtility<D, R, C, T>{
    @Override
    public boolean isHashFunctionUsed(FieldDefinition fieldDefinition, HashFunction<D, R, C, T> hashFunction, Tree<Canopy<R>> tree, Canopy<R> node) {
        boolean isUsed = false;
        if (node == null || tree == null) {
            return false;
        }
        if (checkFunctionInNode(node, fieldDefinition.fieldName, hashFunction)) {
            return true;
        }
        Tree<Canopy<R>> nodeTree = tree.getTree(node);
        if (nodeTree == null) {
            return false;
        }

        Tree<Canopy<R>> parent = nodeTree.getParent();
        if (parent != null) {
            Canopy<R>head = parent.getHead();
            while (head != null) {
                // check siblings of node
				/*for (Tree<Canopy<R>> siblings : parent.getSubTrees()) {
					Canopy<R>sibling = siblings.getHead();
					if (checkFunctionInNode(sibling, index, function))
						return true;
				}*/
                // check parent of node
                return isHashFunctionUsed(fieldDefinition, hashFunction, tree, head);
            }
        }
        return isUsed;
    }

    @Override
    public void addHashFunctionIfRequired(Canopy<R> node) {
        //don't add hashFunction to cache
        //as we are in default mode
    }

    @Override
    public void removeHashFunctionIfRequired(Canopy<R> node) {
        //don't remove hashFunction from cache
        //as we are in default mode
    }

    private boolean checkFunctionInNode(Canopy<R>node, String name,
                                       HashFunction<D, R, C, T> function) {
        return node.getFunction() != null && node.getFunction().equals(function)
                && node.context.fieldName.equals(name);
    }
}
