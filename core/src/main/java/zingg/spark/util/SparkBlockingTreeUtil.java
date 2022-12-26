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

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.Arguments;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.Util;
import zingg.spark.block.SparkBlockFunction;
import zingg.util.BlockingTreeUtil;

public class SparkBlockingTreeUtil extends BlockingTreeUtil<SparkSession, Dataset<Row>, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SparkBlockingTreeUtil.class);

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getBlockHashes(ZFrame<Dataset<Row>, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            Dataset<Row> retDF = testData.df().map(new SparkBlockFunction(tree), RowEncoder.apply(
                    new Block<Dataset<Row>,Row,Column,DataType>().appendHashCol(testData.df().schema())));
            return new SparkFrame(retDF);
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
}