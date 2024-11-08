package zingg.common.client.util;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

import java.util.List;

public abstract class VerticalDisplayUtility<D, R, C> {

    public static String HEADER_COL_1 = "Field";
    public static String HEADER_COL_2 = "Value1";
    public static String HEADER_COL_3 = "Value2";
    public static String ORDER_COL = "order";
    public static Integer MAX_COLUMNS = 1000;

    public void showVertical(D df) throws ZinggClientException {
        ZFrame<D, R, C> vertical = this.transpose(df);
        vertical.show(MAX_COLUMNS);
    }


    private ZFrame<D, R, C> transpose(D df) throws ZinggClientException {
        D dataFrame = this.transposeDF(df, getColumns(df));
        return getZFrame(dataFrame);
    }

    public D transposeDF(D df, List<String> columns) throws ZinggClientException {

        try {
            List<Pair<String, String>> comparison_pairs = getComparisonPairs(df);
            D transposedDf = getTransformedDFInitial(df);

            for(int idx = 0; idx < columns.size(); idx++){
                transposedDf = getTransformedDfAtIteration(transposedDf, df, comparison_pairs, columns, idx);
            }
            return getTransformedDFFinal(transposedDf, columns.size());
        } catch (Exception exception) {
            throw new ZinggClientException("Error occurred while transposing dataframe" + exception.getMessage());
        }
    }

    protected abstract List<Pair<String, String>> getComparisonPairs(D df);
    protected abstract ZFrame<D, R, C> getZFrame(D df);
    protected abstract D getTransformedDFInitial(D df);
    protected abstract D getTransformedDfAtIteration(D transformedDf, D df, List<Pair<String, String>> comparison_pairs, List<String> columns, int iteration);
    protected abstract D getTransformedDFFinal(D df, int totalNumberOfColumns);
    protected abstract List<String> getColumns(D df);

}
