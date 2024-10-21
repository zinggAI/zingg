package zingg.common.core.block.blockingTree;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.core.block.Block;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class OptimizedBlockingTreeBuilder<D, R, C, T> extends Block<D, R, C, T> implements IBlockingTreeBuilder<D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(OptimizedBlockingTreeBuilder.class);


    @Override
    public Tree<Canopy<R>> getBlockingTree(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
                                           List<FieldDefinition> fieldsOfInterest, Block<D, R, C, T> cblock)
            throws Exception, ZinggClientException {
        LOG.info("--------- using optimized blocking tree builder ---------");
        cblock.setHashFunctionsInCurrentNodePath(new HashSet<>());
        Tree<Canopy<R>> blockingTree = dfsToGetBlockingTree(tree, parent, node, fieldsOfInterest, cblock.getHashFunctionsInCurrentNodePath(), cblock);
        return blockingTree;
    }

    private Tree<Canopy<R>> dfsToGetBlockingTree(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node, List<FieldDefinition> fieldsOfInterest,
                                                 Set<String> hashFunctionsInCurrentNodePath, Block<D, R, C, T> cblock) throws ZinggClientException, Exception {
        long size = node.getTrainingSize();
        if (size > cblock.getMaxSize() && node.getDupeN() != null && !node.getDupeN().isEmpty()) {
            Canopy<R> best = cblock.getBestNode(tree, parent, node, fieldsOfInterest);
            if (best != null) {
                if (tree == null && parent == null) {
                    tree = new Tree<>(node);
                }
                traverseThroughCanopies(best, tree, node, fieldsOfInterest, hashFunctionsInCurrentNodePath, cblock);
            } else {
                node.clearBeforeSaving();
            }
        } else {
            if ((node.getDupeN() == null) || (node.getDupeN().isEmpty())) {
                LOG.warn("Ran out of training at size " + size + " for node " + node);
            } else {
                if (tree == null) {
                    throw new ZinggClientException("Unable to create Zingg models due to insufficient data. Please run Zingg after adding more data");
                }
            }
            node.clearBeforeSaving();
        }
        return tree;
    }

    private void traverseThroughCanopies(Canopy<R> best, Tree<Canopy<R>> tree, Canopy<R> node, List<FieldDefinition> fieldsOfInterest,
                                         Set<String> hashFunctionsInCurrentNodePath, Block<D, R, C, T> cblock) throws ZinggClientException, Exception {
        hashFunctionsInCurrentNodePath.add(best.getFunction().getName() + ":" + best.getContext().fieldName);
        best.copyTo(node);
        List<Canopy<R>> canopies = node.getCanopies();
        for (Canopy<R> n : canopies) {
            node.clearBeforeSaving();
            tree.addLeaf(node, n);
            dfsToGetBlockingTree(tree, node, n, fieldsOfInterest, hashFunctionsInCurrentNodePath, cblock);
        }
        hashFunctionsInCurrentNodePath.remove(best.getFunction().getName() + ":" + best.getContext().fieldName);
    }
}
