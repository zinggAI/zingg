package zingg.common.core.block.blockingTree;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.core.block.Block;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;

import java.util.List;

public interface IBlockingTreeBuilder<D, R, C, T> {

    Tree<Canopy<R>> getBlockingTree(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
                                    List<FieldDefinition> fieldsOfInterest, Block<D, R, C, T> cblock) throws Exception, ZinggClientException;
}
