package zingg.spark.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.util.StopWordRemoverUtility;
import zingg.spark.core.preprocess.stopwords.SparkStopWordsRemover;

public class SparkStopWordRemoverUtility extends StopWordRemoverUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private final Context<SparkSession, Dataset<Row>, Row, Column, DataType> context;

    public SparkStopWordRemoverUtility(Context<SparkSession, Dataset<Row>, Row, Column, DataType> context) throws ZinggClientException {
        super();
        this.context = context;
    }

    @Override
    public void addStopWordRemover() {
        super.stopWordsRemovers.add(new SparkStopWordsRemover(context));
    }
}
