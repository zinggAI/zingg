package zingg.spark.core.executor.trainer.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.ZFrame;
import zingg.common.core.executor.trainer.util.IDataFrameUtility;
import zingg.spark.client.SparkFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SparkDataFrameUtility implements IDataFrameUtility<SparkSession,Dataset<Row>,Row,Column,DataType> {

    public ZFrame<Dataset<Row>,Row,Column> createDFWithDoubles(int numRows, int numCols, SparkSession sparkSession) {

        StructType structType = new StructType();

        List<Double> rowValues = new ArrayList<Double>(); 

        for (int n = 0; n < numCols; ++n) {
            structType = structType.add("col" + n, DataTypes.DoubleType, false); 
            rowValues.add(0d);
        };

        List<Row> nums = new ArrayList<Row>();

        IntStream.range(0, numRows).forEachOrdered(n -> {
            nums.add(RowFactory.create(rowValues));
        });

        Dataset<Row> df = sparkSession.createDataFrame(nums, structType);
        return new SparkFrame(df.toDF());
    }

    
}
