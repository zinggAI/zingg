package zingg.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.storage.StorageLevel;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.util.ListMap;
import zingg.hash.HashFunction;

public class BlockingTreeUtil {

    public static final Log LOG = LogFactory.getLog(BlockingTreeUtil.class);
	

    public static Tree<Canopy> createBlockingTree(Dataset<Row> testData,  
			Dataset<Row> positives, double sampleFraction, long blockSize,
            Arguments args,
            ListMap<DataType, HashFunction> hashFunctions) throws Exception {
		Dataset<Row> sample = testData.sample(false, sampleFraction);
		sample = sample.persist(StorageLevel.MEMORY_ONLY());
		long totalCount = sample.count();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Learning blocking rules for sample count " + totalCount  
				+ " and pos " + positives.count() + " and testData count " + testData.count());
		}
		if (blockSize == -1) blockSize = Heuristics.getMaxBlockSize(totalCount);
		LOG.info("Learning indexing rules for block size " + blockSize);
       
		positives = positives.coalesce(1); 
		Block cblock = new Block(sample, positives, hashFunctions, blockSize);
		Canopy root = new Canopy(sample.collectAsList(), positives.collectAsList());

		List<FieldDefinition> fd = new ArrayList<FieldDefinition> ();

		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				fd.add(def);	
			}
		}

		Tree<Canopy> blockingTree = cblock.getBlockingTree(null, null, root,
				fd);
		if (LOG.isDebugEnabled()) {
			LOG.debug("The blocking tree is ");
			blockingTree.print(2);
		}
		
		return blockingTree;
	}

	
	public static Tree<Canopy> createBlockingTreeFromSample(Dataset<Row> testData,  
			Dataset<Row> positives, double sampleFraction, long blockSize, Arguments args, 
            ListMap<DataType, HashFunction> hashFunctions) throws Exception {
		Dataset<Row> sample = testData.sample(false, sampleFraction); 
		return createBlockingTree(sample, positives, sampleFraction, blockSize, args, hashFunctions);
	}
	
	
    
}
