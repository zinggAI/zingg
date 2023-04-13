package zingg.spark.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;

import zingg.common.client.ZFrame;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.Block;
import zingg.common.core.hash.HashFunction;
import zingg.spark.core.feature.SparkFeatureFactory;

public class SparkBlock extends Block<Dataset<Row>, Row, Column, DataType> {

    private static final long serialVersionUID = 1L;


	public SparkBlock(){}
    

    public SparkBlock(ZFrame<Dataset<Row>, Row, Column> training, ZFrame<Dataset<Row>, Row, Column> dupes,
    ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> functionsMap, long maxSize) {
		super(training, dupes, functionsMap, maxSize);
	}


    @Override
    public DataType getDataTypeFromString(String t) {
    	//TODO MAY OR MAY NOT BE NEEDED, ALSO DUPLICATE CODE TO SparkFeatureFactory
        if (SparkFeatureFactory.ARR_DOUBLE_TYPE.equals(t)) {
        	return DataTypes.createArrayType(DataTypes.DoubleType);
        } 
        
        if (SparkFeatureFactory.ARR_STR_TYPE.equals(t)) {
        	return DataTypes.createArrayType(DataTypes.StringType);
        }
        
        return DataType.fromJson(t);
    }

}
