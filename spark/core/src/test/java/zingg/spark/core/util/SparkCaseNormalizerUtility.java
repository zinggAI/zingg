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
import zingg.common.core.util.CaseNormalizerUtility;
import zingg.spark.core.preprocess.casenormalize.SparkCaseNormalizer;

public class SparkCaseNormalizerUtility extends CaseNormalizerUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private final Context<SparkSession, Dataset<Row>, Row, Column, DataType> context;

    public SparkCaseNormalizerUtility(Context<SparkSession, Dataset<Row>, Row, Column, DataType> context) throws ZinggClientException {
        super();
        this.context = context;
    }

    @Override
    public void addCaseNormalizersFields(List<FieldDefinition> fd){
        super.caseNormalizersFields.add(fd);
        new SparkCaseNormalizer(context,fd);
    }
    
}
