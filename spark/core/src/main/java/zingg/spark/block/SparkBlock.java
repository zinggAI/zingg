package zingg.spark.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.block.Block;
import zingg.client.ZFrame;
import zingg.client.util.ListMap;
import zingg.hash.HashFunction;

public class SparkBlock extends Block<Dataset<Row>, Row, Column, DataType> {

    public SparkBlock(){}
    

    public SparkBlock(ZFrame<Dataset<Row>, Row, Column> training, ZFrame<Dataset<Row>, Row, Column> dupes,
    ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> functionsMap, long maxSize) {
		super(training, dupes, functionsMap, maxSize);
	}


    @Override
    public DataType getDataTypeFromString(String t) {
      return DataType.fromJson(t);
    }

}
