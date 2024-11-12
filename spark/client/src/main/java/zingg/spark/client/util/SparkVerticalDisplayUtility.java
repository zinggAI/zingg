package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import zingg.common.client.ZFrame;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.Pair;
import zingg.common.client.util.WithSession;
import zingg.common.client.util.verticalDisplay.VerticalDisplayUtility;

import java.util.ArrayList;
import java.util.List;

public class SparkVerticalDisplayUtility extends VerticalDisplayUtility<SparkSession, Dataset<Row>, Row, Column> {

    private static final IWithSession<SparkSession> iWithSession = new WithSession<>();

    public SparkVerticalDisplayUtility(SparkSession sparkSession) {
        super(new SparkDFObjectUtil(iWithSession));
        iWithSession.setSession(sparkSession);
    }

    @Override
    protected List<Pair<String, String>> getComparisonPairs(ZFrame<Dataset<Row>, Row, Column> zFrame) {

        ZFrame<Dataset<Row>, Row, Column> zFrame1 = zFrame.limit(1);
        ZFrame<Dataset<Row>, Row, Column> zFrame2 = zFrame.except(zFrame1);

        Row row1 = zFrame.df().toLocalIterator().next();
        Row row2 = zFrame2.df().toLocalIterator().next();

        List<Pair<String, String>> comparison_pairs = new ArrayList<>();
        int idx = 0;
        while(idx < row1.size()) {
            comparison_pairs.add(new Pair<String, String>(getString(row1.get(idx)), getString(row2.get(idx))));
            idx++;
        }

        return comparison_pairs;
    }

    private String getString(Object object) {
        return object == null ? null : object.toString();
    }
}
