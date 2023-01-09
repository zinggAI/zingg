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
import zingg.client.ZinggClientException;
import zingg.client.ZFrame;
import zingg.client.util.ListMap;
import zingg.hash.HashFunction;

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
            Arguments args,
			ListMap<T, HashFunction<D,R,C,T>> hashFunctions) throws Exception {
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
	
	public abstract void writeBlockingTree(Tree<Canopy<R>> blockingTree, Arguments args) throws Exception, ZinggClientException; // {
		/*byte[] byteArray  = Util.convertObjectIntoByteArray(blockingTree);
		StructType schema = DataTypes.createStructType(new StructField[] { DataTypes.createStructField("BlockingTree", DataTypes.BinaryType, false) });
		List<Object> objList = new ArrayList<>();
		objList.add(byteArray);
		JavaRDD<Row> rowRDD = ctx.parallelize(objList).map((Object row) -> RowFactory.create(row));
		Dataset<Row> df = spark.sqlContext().createDataFrame(rowRDD, schema).toDF().coalesce(1);
		PipeUtil.write(df, args, ctx, PipeUtil.getBlockingTreePipe(args));*/
	//}

	public abstract Tree<Canopy<R>> readBlockingTree(Arguments args) throws Exception, ZinggClientException; //{
		/*
		Dataset<Row> tree = PipeUtil.read(spark, false, args.getNumPartitions(), false, PipeUtil.getBlockingTreePipe(args));
		byte [] byteArrayBack = (byte[]) tree.head().get(0);
		Tree<Canopy> blockingTree = null;
		blockingTree =  (Tree<Canopy>) Util.revertObjectFromByteArray(byteArrayBack);
		return blockingTree;
		*/
	

	public abstract ZFrame<D,R,C> getBlockHashes(ZFrame<D,R,C> testData, Tree<Canopy<R>> tree);
	//.map(new Block<D,R,C,T>().BlockFunction(tree), RowEncoder.apply(Block<D,R,C,T>.appendHashCol(sample.schema())));
	
}
