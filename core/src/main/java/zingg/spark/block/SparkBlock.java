package zingg.spark.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.block.Block;

public class SparkBlock extends Block<Dataset<Row>, Row, Column, DataType> {
    
}
