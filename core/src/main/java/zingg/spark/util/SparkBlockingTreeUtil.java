package zingg.spark.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.ml.util.SchemaUtils;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.Arguments;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.client.util.ListMap;
import zingg.client.util.Util;
import zingg.hash.HashFunction;
import zingg.spark.block.SparkBlock;
import zingg.spark.block.SparkBlockFunction;
import zingg.util.BlockingTreeUtil;

public class SparkBlockingTreeUtil extends BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SparkBlockingTreeUtil.class);

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getBlockHashes(ZFrame<Dataset<Row>, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            Dataset<Row> retDF = testData.df().map(new SparkBlockFunction(tree), RowEncoder.apply(
                    appendHashCol(testData.df().schema())));
            return new SparkFrame(retDF);
    }

    public StructType appendHashCol(StructType s) {
        StructType retSchema = SchemaUtils.appendColumn(s, ColName.HASH_COL, DataTypes.IntegerType, false);
        LOG.debug("returning schema after step 1 is " + retSchema);
        return retSchema;
}


@Override
public void writeBlockingTree(Tree<Canopy<Row>> blockingTree, Arguments args) throws Exception, ZinggClientException {
        byte[] byteArray  = Util.convertObjectIntoByteArray(blockingTree);
        StructType schema = DataTypes.createStructType(new StructField[] { DataTypes.createStructField("BlockingTree", DataTypes.BinaryType, false) });
        List<Object> objList = new ArrayList<>();
        objList.add(byteArray);
        //TODOJavaRDD<Row> rowRDD = ctx.parallelize(objList).map((Object row) -> RowFactory.create(row));
        //TODODataset<Row> df = spark.sqlContext().createDataFrame(rowRDD, schema).toDF().coalesce(1);
        //TODOPipeUtil.write(df, args, ctx, PipeUtil.getBlockingTreePipe(args));
        
}

@Override
public Tree<Canopy<Row>> readBlockingTree(Arguments args) throws Exception, ZinggClientException {
        ZFrame<Dataset<Row>, Row, Column> tree = getPipeUtil().read(false, 1, false, getPipeUtil().getBlockingTreePipe(args));
        byte [] byteArrayBack = (byte[]) tree.df().head().get(0);
        Tree<Canopy<Row>> blockingTree = null;
        blockingTree =  (Tree<Canopy<Row>>) Util.revertObjectFromByteArray(byteArrayBack);
        return blockingTree;
}

@Override
public Block<Dataset<Row>, Row, Column, DataType> getBlock(ZFrame<Dataset<Row>, Row, Column> sample,
                ZFrame<Dataset<Row>, Row, Column> positives,
                ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> hashFunctions, long blockSize) {
        // TODO Auto-generated method stub
        return new SparkBlock(sample, positives, hashFunctions, blockSize);
}
}