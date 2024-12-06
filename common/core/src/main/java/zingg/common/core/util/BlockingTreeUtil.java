package zingg.common.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZFrame;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.ListMap;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.client.util.Util;
import zingg.common.core.block.Block;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.core.hash.HashFunction;

public abstract class BlockingTreeUtil<S, D,R,C,T> {

    public final Log LOG = LogFactory.getLog(BlockingTreeUtil.class);

	private PipeUtilBase<S, D, R, C> pipeUtil;

    public PipeUtilBase<S, D, R, C> getPipeUtil() {
		return pipeUtil;
	}


	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}


	public abstract Block<D,R,C,T> getBlock(ZFrame<D,R,C> sample, ZFrame<D,R,C> positives, 
		ListMap<T, HashFunction<D,R,C,T>>hashFunctions, long blockSize);


	public Tree<Canopy<R>> createBlockingTree(ZFrame<D,R,C> testData,
											  ZFrame<D,R,C> positives, double sampleFraction, long blockSize,
											  IArguments args,
											  ListMap<T, HashFunction<D,R,C,T>> hashFunctions) throws Exception, ZinggClientException {
		ZFrame<D,R,C> sample = testData.sample(false, sampleFraction);
		sample = sample.cache();
		long totalCount = sample.count();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Learning blocking rules for sample count " + totalCount  
				+ " and pos " + positives.count() + " and testData count " + testData.count());
		}
		if (blockSize == -1) blockSize = Heuristics.getMaxBlockSize(totalCount, args.getBlockSize());
		LOG.info("Learning indexing rules for block size " + blockSize);
       
		positives = positives.coalesce(1); 
		Block<D,R,C,T> cblock = getBlock(sample, positives, hashFunctions, blockSize);
		Canopy<R> root = new Canopy<R>(sample.collectAsList(), positives.collectAsList());

		List<FieldDefinition> fd = new ArrayList<FieldDefinition> ();

		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
				fd.add(def);	
			}
		}
		Tree<Canopy<R>> blockingTree =  cblock.getBlockingTree(null, null, root, fd);
		if (LOG.isDebugEnabled()) {
			LOG.debug("The blocking tree is ");
			blockingTree.print(2);
		}
		
		return blockingTree;
	}


	
	
	public  Tree<Canopy<R>> createBlockingTreeFromSample(ZFrame<D,R,C> testData,  
			ZFrame<D,R,C> positives, double sampleFraction, long blockSize, IArguments args, 
            ListMap hashFunctions) throws Exception, ZinggClientException {
		ZFrame<D,R,C> sample = testData.sample(false, sampleFraction); 
		return createBlockingTree(sample, positives, sampleFraction, blockSize, args, hashFunctions);
	}
	
	public void writeBlockingTree(Tree<Canopy<R>> blockingTree, IArguments args, IModelHelper mu) throws Exception, ZinggClientException {
		byte[] byteArray  = Util.convertObjectIntoByteArray(blockingTree);
        PipeUtilBase<S, D, R, C> pu = getPipeUtil();
        pu.write(getTreeDF(byteArray), mu.getBlockingTreePipe(args));
	}

	public abstract ZFrame<D, R, C> getTreeDF(byte[] tree) ;

	
	public byte[] getTreeFromDF(ZFrame<D, R, C> z){
        byte [] byteArrayBack = (byte[]) z.getOnlyObjectFromRow(z.head());
        return byteArrayBack;
	}


	public Tree<Canopy<R>> readBlockingTree(IArguments args, IModelHelper mu) throws Exception, ZinggClientException{
		PipeUtilBase<S, D, R, C> pu = getPipeUtil();
        ZFrame<D, R, C> tree = pu.read(false, 1, false, mu.getBlockingTreePipe(args));
        //tree.show();
        //tree.df().show();
        //byte [] byteArrayBack = (byte[]) tree.df().head().get(0);
		byte[] byteArrayBack = getTreeFromDF(tree);
        Tree<Canopy<R>> blockingTree = null;
        LOG.warn("byte array back is " + byteArrayBack);
        blockingTree =  (Tree<Canopy<R>>) Util.revertObjectFromByteArray(byteArrayBack);
        return blockingTree;
	}
	

	public abstract ZFrame<D,R,C> getBlockHashes(ZFrame<D,R,C> testData, Tree<Canopy<R>> tree);
	//.map(new Block<D,R,C,T>().BlockFunction(tree), RowEncoder.apply(Block<D,R,C,T>.appendHashCol(sample.schema())));
	
}
