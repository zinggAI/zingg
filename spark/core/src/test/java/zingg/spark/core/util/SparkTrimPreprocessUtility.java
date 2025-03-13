package zingg.spark.core.util;

import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.util.MultiFieldPreprocessorUtility;
import zingg.spark.core.preprocess.trim.SparkTrimPreprocessor;

public class SparkTrimPreprocessUtility extends MultiFieldPreprocessorUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private final Context<SparkSession, Dataset<Row>, Row, Column, DataType> context;

    public SparkTrimPreprocessUtility(Context<SparkSession, Dataset<Row>, Row, Column, DataType> context) throws ZinggClientException {
        super();
        this.context = context;
    }

    @Override
    public void addFieldDefinitionsList(List<FieldDefinition> fd){
        super.multiFieldsList.add(fd);
        new SparkTrimPreprocessor(context,fd);
    }
    
}
