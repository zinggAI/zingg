package zingg.common.core.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.spark.core.preprocess.SparkStopWordsRemover;

public class SparkStopWordRemoverUtility extends StopWordRemoverUtility<SparkSession, Dataset<Row>, Row, Column, DataType> {

    private final Context<SparkSession, Dataset<Row>, Row, Column, DataType> context;

    public SparkStopWordRemoverUtility(Context<SparkSession, Dataset<Row>, Row, Column, DataType> context) throws ZinggClientException {
        super();
        this.context = context;
    }

    @Override
    public void addStopWordRemover(IArguments iArguments) {
        super.stopWordsRemovers.add(new SparkStopWordsRemover(context, iArguments));
    }
}
