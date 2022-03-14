package zingg.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.types.DataType;
import com.snowflake.snowpark_java.types.DataTypes;
import com.snowflake.snowpark_java.types.StructField;
import com.snowflake.snowpark_java.types.StructType;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.util.ListMap;
import zingg.client.util.Util;
import zingg.hash.HashFunction;

public class BlockingTreeUtil {

    public static final Log LOG = LogFactory.getLog(BlockingTreeUtil.class);
	

    public static Tree<Canopy> createBlockingTree(DataFrame testData,  
			DataFrame positives, double sampleFraction, long blockSize,
            Arguments args,
            ListMap<DataType, HashFunction> hashFunctions) throws Exception {
		DataFrame sample = testData.sample(sampleFraction);
		//sample = sample.persist(StorageLevel.MEMORY_ONLY());
		long totalCount = sample.count();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Learning blocking rules for sample count " + totalCount  
				+ " and pos " + positives.count() + " and testData count " + testData.count());
		}
		if (blockSize == -1) blockSize = Heuristics.getMaxBlockSize(totalCount);
		LOG.info("Learning indexing rules for block size " + blockSize);
       
		Block cblock = new Block(sample, positives, hashFunctions, blockSize);
		Canopy root = new Canopy(Arrays.asList(sample.collect()), Arrays.asList(positives.collect()));

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

	
	public static Tree<Canopy> createBlockingTreeFromSample(DataFrame testData,  
			DataFrame positives, double sampleFraction, long blockSize, Arguments args, 
            ListMap<DataType, HashFunction> hashFunctions) throws Exception {
		DataFrame sample = testData.sample(sampleFraction); 
		return createBlockingTree(sample, positives, sampleFraction, blockSize, args, hashFunctions);
	}
	
	public static void writeBlockingTree(Session snow, Tree<Canopy> blockingTree, Arguments args) throws Exception {
		byte[] byteArray  = Util.convertObjectIntoByteArray(blockingTree);
		StructType schema = StructType.create(new StructField[] { new StructField("BlockingTree", DataTypes.BinaryType, false) });
		List<Object> objList = new ArrayList<>();
		objList.add(byteArray);
		DataFrame df = snow.createDataFrame(objList.toArray(Row[]::new), schema).toDF();
		PipeUtil.write(df, args, PipeUtil.getBlockingTreePipe(args));
	}

	public static Tree<Canopy> readBlockingTree(Session snow, Arguments args) throws Exception {
		DataFrame tree = PipeUtil.read(snow, false, args.getNumPartitions(), false, PipeUtil.getBlockingTreePipe(args));
		byte [] byteArrayBack = (byte[]) tree.first().get().get(0);
		Tree<Canopy> blockingTree = null;
		blockingTree =  (Tree<Canopy>) Util.revertObjectFromByteArray(byteArrayBack);
		return blockingTree;
	}
}
