package zingg.spark.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.types.DataType;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.spark.SparkBlockFunction;
import zingg.util.BlockingTreeUtil;

public class SparkBlockingTreeUtil extends BlockingTreeUtil<Dataset<Row>, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SparkBlockingTreeUtil.class);

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getBlockHashes(ZFrame<Dataset<Row>, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            Dataset<Row> retDF = testData.df().map(new SparkBlockFunction(tree), RowEncoder.apply(
                    new Block<Dataset<Row>,Row,Column,DataType>().appendHashCol(testData.df().schema())));
            return new SparkFrame(retDF);
    }

    }
