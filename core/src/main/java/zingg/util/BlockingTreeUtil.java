package zingg.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.ZFrame;
import zingg.client.util.ListMap;
import zingg.client.util.Util;

public abstract class BlockingTreeUtil<D,R,C,T,T1> {

    public final Log LOG = LogFactory.getLog(BlockingTreeUtil.class);
	

    public Tree<Canopy<R>> createBlockingTree(ZFrame<D,R,C> testData,  
			ZFrame<D,R,C> positives, double sampleFraction, long blockSize,
            Arguments args,
            ListMap hashFunctions) throws Exception {
		ZFrame<D,R,C> sample = testData.sample(false, sampleFraction);
		sample = sample.cache();
		long totalCount = sample.count();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Learning blocking rules for sample count " + totalCount  
				+ " and pos " + positives.count() + " and testData count " + testData.count());
		}
		if (blockSize == -1) blockSize = Heuristics.getMaxBlockSize(totalCount);
		LOG.info("Learning indexing rules for block size " + blockSize);
       
		positives = positives.coalesce(1); 
		Block<D,R,C,T,T1> cblock = new Block<D,R,C,T,T1>(sample, positives, hashFunctions, blockSize);
		Canopy<R> root = new Canopy<R>(sample.collectAsList(), positives.collectAsList());

		List<FieldDefinition> fd = new ArrayList<FieldDefinition> ();

		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				fd.add(def);	
			}
		}

		Tree<Canopy<R>> blockingTree = cblock.getBlockingTree(null, null, root,
				fd);
		if (LOG.isDebugEnabled()) {
			LOG.debug("The blocking tree is ");
			blockingTree.print(2);
		}
		
		return blockingTree;
	}

	
	public  Tree<Canopy<R>> createBlockingTreeFromSample(ZFrame<D,R,C> testData,  
			ZFrame<D,R,C> positives, double sampleFraction, long blockSize, Arguments args, 
            ListMap hashFunctions) throws Exception {
		ZFrame<D,R,C> sample = testData.sample(false, sampleFraction); 
		return createBlockingTree(sample, positives, sampleFraction, blockSize, args, hashFunctions);
	}
	
	public  void writeBlockingTree(Tree<Canopy<R>> blockingTree, Arguments args) throws Exception {
		Util.writeToFile(blockingTree, args.getBlockFile());
	}

	public  Tree<Canopy<R>> readBlockingTree(Arguments args) throws Exception {
		return (Tree<Canopy<R>>) Util.readfromFile(args.getBlockFile());
	}

	public abstract ZFrame<D,R,C> getBlockHashes(ZFrame<D,R,C> testData, Tree<Canopy<R>> tree);
}
