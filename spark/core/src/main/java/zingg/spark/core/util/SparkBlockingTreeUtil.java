package zingg.spark.core.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ListMap;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.block.*;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.spark.client.SparkFrame;
import org.apache.spark.sql.SparkSession;
import zingg.spark.core.block.SparkBlock;
import zingg.spark.core.block.SparkBlockFunction;

public class SparkBlockingTreeUtil extends BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SparkBlockingTreeUtil.class);
    protected SparkSession spark;
    
    public SparkBlockingTreeUtil(SparkSession s, PipeUtilBase pipeUtil) {
        this.spark = s;
        setPipeUtil(pipeUtil);
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getBlockHashes(ZFrame<Dataset<Row>, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            Dataset<Row> retDF = testData.df().map(new SparkBlockFunction(tree), Encoders.row(
                    appendHashCol(testData.df().schema())));
            return new SparkFrame(retDF);
    }

    public StructType appendHashCol(StructType s) {
        StructType retSchema = SchemaUtils.appendColumn(s, ColName.HASH_COL, DataTypes.IntegerType, false);
        LOG.debug("returning schema after step 1 is " + retSchema);
        return retSchema;
}




@Override 
public ZFrame<Dataset<Row>, Row, Column> getTreeDF(byte[] blockingTree){
        StructType schema = DataTypes.createStructType(new StructField[] { DataTypes.createStructField("BlockingTree", DataTypes.BinaryType, false) });
        List<Row> objList = new ArrayList<Row>();
        objList.add(RowFactory.create(blockingTree));
        Dataset<Row> df = spark.sqlContext().createDataFrame(objList, schema).toDF().coalesce(1);
        return new SparkFrame(df);
}


/* 
@Override
public Tree<Canopy<Row>> readBlockingTree(Arguments args) throws Exception, ZinggClientException {
        PipeUtilBase<SparkSession, Dataset<Row>, Row, Column> pu = getPipeUtil();
        ZFrame<Dataset<Row>, Row, Column> tree = pu.read(false, 1, false, pu.getBlockingTreePipe(args));
        tree.show();
        tree.df().show();
        byte [] byteArrayBack = (byte[]) tree.df().head().get(0);
        Tree<Canopy<Row>> blockingTree = null;
        LOG.warn("byte array back is " + byteArrayBack);
        blockingTree =  (Tree<Canopy<Row>>) Util.revertObjectFromByteArray(byteArrayBack);
        return blockingTree;
}
*/

@Override
public Block<Dataset<Row>, Row, Column, DataType> getBlock(ZFrame<Dataset<Row>, Row, Column> sample,
                                                           ZFrame<Dataset<Row>, Row, Column> positives,
                                                           ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> hashFunctions,
                                                           long blockSize, IArguments arguments) {
        // TODO Auto-generated method stub
        return new SparkBlock(sample, positives, hashFunctions, blockSize, new DefaultFieldDefinitionStrategy<Row>());
}
}