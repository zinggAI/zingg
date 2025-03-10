package zingg.spark.core.preprocess.trim;

import java.util.List;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.trim.TrimPreprocessor;
import static org.apache.spark.sql.functions.trim;

public class SparkTrimPreprocessor extends TrimPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType>{

    private static final long serialVersionUID = 1L;
    protected static String name = "zingg.spark.core.preprocess.trim.SparkTrimPreprocessor";

    public SparkTrimPreprocessor() {
        super();
    }
    
    public SparkTrimPreprocessor(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, List<? extends FieldDefinition> fieldDefinitions) {
        super(context, fieldDefinitions);
    }

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> applyTrimPreprocess(ZFrame<Dataset<Row>, Row, Column> incomingDataFrame, List<String> relevantFields) {
        String[] incomingDFColumns = incomingDataFrame.columns();
        Column[] trimValues = new Column[relevantFields.size()];
        String[] relevantFieldsArray = new String[relevantFields.size()];
        for (int idx = 0; idx < relevantFields.size(); idx++) {
            trimValues[idx] = trim(incomingDataFrame.col(relevantFields.get(idx)));
            relevantFieldsArray[idx] = relevantFields.get(idx);
        }

        return incomingDataFrame.withColumns(relevantFieldsArray, trimValues).select(incomingDFColumns);
    }
    
}
