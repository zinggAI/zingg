package zingg.spark.core.preprocess.casenormalize;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.casenormalize.CaseNormalizer;

import java.util.List;

import static org.apache.spark.sql.functions.lower;

public class SparkCaseNormalizer extends CaseNormalizer<SparkSession, Dataset<Row>, Row, Column, DataType> {
    private static final long serialVersionUID = 1L;
    protected static String name = "zingg.spark.core.preprocess.casenormalize.SparkCaseNormalizer";

    public SparkCaseNormalizer() {
        super();
    }
    public SparkCaseNormalizer(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, List<? extends FieldDefinition> fieldDefinitions) {
        super(context, fieldDefinitions);
    }

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> applyCaseNormalizer(ZFrame<Dataset<Row>, Row, Column> incomingDataFrame, List<String> relevantFields) {
        String[] incomingDFColumns = incomingDataFrame.columns();
        Column[] caseNormalizedValues = new Column[relevantFields.size()];
        String[] relevantFieldsArray = new String[relevantFields.size()];
        for (int idx = 0; idx < relevantFields.size(); idx++) {
            caseNormalizedValues[idx] = lower(incomingDataFrame.col(relevantFields.get(idx)));
            relevantFieldsArray[idx] = relevantFields.get(idx);
        }

        return incomingDataFrame.withColumns(relevantFieldsArray, caseNormalizedValues).select(incomingDFColumns);
    }
}
