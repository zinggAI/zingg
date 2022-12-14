package zingg.snowpark.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaRDD;

import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
// import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.types.DataType;

import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.client.SnowFrame;
import zingg.client.ZFrame;
import zingg.snowpark.SnowBlockFunction;
import zingg.util.BlockingTreeUtil;

public class SnowBlockingTreeUtil extends BlockingTreeUtil<DataFrame, Row, Column, DataType>{

    public static final Log LOG = LogFactory.getLog(SnowBlockingTreeUtil.class);

    @Override
    public ZFrame<DataFrame, Row, Column> getBlockHashes(ZFrame<DataFrame, Row, Column> testData,
            Tree<Canopy<Row>> tree) {
            DataFrame retDF = testData.df().map(new SnowBlockFunction(tree), RowEncoder.apply(
                    new Block<DataFrame,Row,Column,DataType>().appendHashCol(testData.df().schema())));
            return new SnowFrame(retDF);
    }

    }
