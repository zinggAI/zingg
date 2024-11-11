package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import zingg.common.client.ZFrame;
import zingg.common.client.util.Pair;
import zingg.common.client.util.VerticalDisplayUtility;
import zingg.spark.client.SparkFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SparkVerticalDisplayUtility extends VerticalDisplayUtility<Dataset<Row>, Row, Column> {

    @Override
    protected List<Pair<Object, Object>> getComparisonPairs(Dataset<Row> df) {
        Dataset<Row> df1 = df.limit(1);
        Dataset<Row> df2 = df.except(df1);

        Row row1 = df.toLocalIterator().next();
        Row row2 = df2.toLocalIterator().next();

        List<Pair<Object, Object>> comparison_pairs = new ArrayList<>();
        int idx = 0;
        while(idx < row1.size()) {
            comparison_pairs.add(new Pair<Object, Object>(row1.get(idx), row2.get(idx)));
            idx++;
        }

        return comparison_pairs;
    }

    @Override
    protected ZFrame<Dataset<Row>, Row, Column> getZFrame(Dataset<Row> df) {
        return new SparkFrame(df);
    }

    @Override
    protected Dataset<Row> getTransformedDFInitial(Dataset<Row> df) {
        Map<String, Column> columnMap = Map.of(HEADER_COL_1, functions.lit(null), HEADER_COL_2,
                functions.lit(null), HEADER_COL_3, functions.lit(null), ORDER_COL, functions.lit(MAX_COLUMNS));
        return df.withColumns(columnMap);

    }


    @Override
    protected Dataset<Row> getTransformedDfAtIteration(Dataset<Row> transformedDf, Dataset<Row> df, List<Pair<Object, Object>> comparison_pairs, List<String> columns, int iteration) {
        Map<String, Column> columnMap = Map.of(HEADER_COL_1, functions.lit(columns.get(iteration)), HEADER_COL_2,
                functions.lit(comparison_pairs.get(iteration).getFirst()), HEADER_COL_3, functions.lit(comparison_pairs.get(iteration).getSecond()),
                ORDER_COL, functions.lit(iteration));
        return transformedDf.unionByName(df.withColumns(columnMap));
    }

    @Override
    protected Dataset<Row> getTransformedDFFinal(Dataset<Row> df, int totalNumberOfColumns) {
        return df.select(HEADER_COL_1, HEADER_COL_2, HEADER_COL_3, ORDER_COL).distinct().
                sort(df.col(ORDER_COL)).drop(df.col(ORDER_COL)).limit(totalNumberOfColumns);
    }

    @Override
    protected List<String> getColumns(Dataset<Row> df) {
        return List.of(df.columns());
    }
}
