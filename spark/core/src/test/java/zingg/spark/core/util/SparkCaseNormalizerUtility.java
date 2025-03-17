package zingg.spark.core.util;

import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.core.util.MultiFieldPreprocessorUtility;

public class SparkCaseNormalizerUtility extends MultiFieldPreprocessorUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public SparkCaseNormalizerUtility() throws ZinggClientException {
        super();
    }

    @Override
    public void addFieldDefinitionsList(List<FieldDefinition> fd){
        super.multiFieldsList.add(fd);
    }
    
}
