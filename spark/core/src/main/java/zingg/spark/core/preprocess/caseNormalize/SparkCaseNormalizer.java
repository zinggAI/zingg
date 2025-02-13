package zingg.spark.core.preprocess.caseNormalize;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import scala.collection.JavaConverters;
import scala.collection.Seq;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.caseNormalize.CaseNormalizer;
import zingg.spark.client.SparkFrame;

import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.lower;

public class SparkCaseNormalizer extends CaseNormalizer<SparkSession, Dataset<Row>, Row, Column, DataType> {
    private static final long serialVersionUID = 1L;
    protected static String name = "zingg.spark.core.preprocess.caseNormalize.SparkCaseNormalizer";

    public SparkCaseNormalizer() {
        super();
    }
    public SparkCaseNormalizer(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> context, List<? extends FieldDefinition> fieldDefinitions) {
        super(context, fieldDefinitions);
    }

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> applyCaseNormalizer(ZFrame<Dataset<Row>, Row, Column> incomingDataFrame, List<String> relevantFields) {
        String[] incomingDFColumns = incomingDataFrame.columns();
        Seq<String> columnsSeq = JavaConverters.asScalaIteratorConverter(relevantFields.iterator())
                .asScala()
                .toSeq();
        List<Column> caseNormalizedValues = new ArrayList<>();
        for (String relevantField : relevantFields) {
            caseNormalizedValues.add(lower(incomingDataFrame.col(relevantField)));
        }
        Seq<Column> caseNormalizedSeq = JavaConverters.asScalaIteratorConverter(caseNormalizedValues.iterator())
                .asScala()
                .toSeq();
        return new SparkFrame(incomingDataFrame.df().withColumns(columnsSeq, caseNormalizedSeq)).select(incomingDFColumns);
    }
}
