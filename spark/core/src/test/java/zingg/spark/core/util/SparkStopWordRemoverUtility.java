package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.util.AStopWordRemoverUtility;
import zingg.spark.core.preprocess.stopwords.SparkStopWordsRemover;

import java.util.List;
import java.util.Objects;

public class SparkStopWordRemoverUtility extends AStopWordRemoverUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private final Context<SparkSession, Dataset<Row>, Row, Column, DataType> context;

    public SparkStopWordRemoverUtility(Context<SparkSession, Dataset<Row>, Row, Column, DataType> context) throws ZinggClientException {
        super();
        this.context = context;
    }

    @Override
    public void addStopWordRemover(FieldDefinition fd) {
        super.stopWordsRemovers.add(new SparkStopWordsRemover(context,fd));
    }

    @Override
    protected List<String> getStopWordFileNames() {
        String fileName1 = Objects.requireNonNull(
                AStopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWords.csv")).getFile();
        String fileName2 = Objects.requireNonNull(
                AStopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWordsWithoutHeader.csv")).getFile();
        String fileName3 = Objects.requireNonNull(
                AStopWordRemoverUtility.class.getResource("../../../../preProcess/stopwords/stopWordsMultipleCols.csv")).getFile();

        return List.of(fileName1, fileName2, fileName3);
    }
}
