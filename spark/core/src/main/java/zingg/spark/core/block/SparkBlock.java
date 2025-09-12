package zingg.spark.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZFrame;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.Block;
import zingg.common.core.block.FieldDefinitionStrategy;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.hash.HashFunction;
import zingg.spark.core.feature.SparkFeatureFactory;

public class SparkBlock extends Block<Dataset<Row>, Row, Column, DataType> {

    private static final long serialVersionUID = 1L;


	public SparkBlock(){
        super();
    }
    

    public SparkBlock(ZFrame<Dataset<Row>, Row, Column> training, ZFrame<Dataset<Row>, Row, Column> dupes,
                      ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> functionsMap, long maxSize, FieldDefinitionStrategy<Row> fieldDefinitionStrategy) {
		super(training, dupes, functionsMap, maxSize, fieldDefinitionStrategy);
	}
    
    @Override
    public FeatureFactory<DataType> getFeatureFactory() {
        return new SparkFeatureFactory();
    }    

}
