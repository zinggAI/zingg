package zingg.common.core.executor;

import zingg.common.client.ZFrame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class VerticalDisplayUtility<D, R, C> {


    public void showVertical() {
        ZFrame<D, R, C> headerIncludedFrame = getHeaderIncludedDataFrame(getZFrame(df));
        ZFrame<D, R, C> vertical = headerIncludedFrame.transpose(PIVOT_COLUMN);
        vertical.sortAscending(ORDER).drop(ORDER).show(1000);
    }

    /***
     * return new ZFrame with new Column added as PIVOT used for transposing the matrix
     * @param records
     * @return header included zFrame
     */
    private ZFrame<D, R, C> getHeaderIncludedDataFrame(ZFrame<D, R, C> records) {
        ZFrame<D, R, C> orderedRowAdded = addAutoIncrementalRow();

        ZFrame<D, R, C> firstRecord = orderedRowAdded.limit(1);
        ZFrame<D, R, C> secondRecord = orderedRowAdded.except(firstRecord).limit(1);
        ZFrame<D, R, C> thirdRecord = orderedRowAdded.except(firstRecord.union(secondRecord));

        //return new ZFrame with Field column added to be used as pivot
        return firstRecord.withColumn(PIVOT_COLUMN, VALUE_1).
                union(secondRecord.withColumn(PIVOT_COLUMN, VALUE_2)).
                union(thirdRecord.withColumn(PIVOT_COLUMN, ORDER));
    }

    /***
     * Add auto incremental row like {1, 2, 3, 4, 5} to the dataframe
     * @return ZFrame
     */
    private ZFrame<D, R, C> addAutoIncrementalRow() {
        String[] columns = df.columns();
        D temporaryDF = df.limit(1);
        List<String> monotonicIncreasing = new ArrayList<>();
        for (int idx = 0; idx < columns.length; idx++) {
            monotonicIncreasing.add(String.valueOf(idx));
        }
        Collections.sort(monotonicIncreasing);
        for (int idx = 0; idx < columns.length; idx++) {
            temporaryDF = temporaryDF.withColumn(columns[idx], functions.lit(monotonicIncreasing.get(idx)));
        }
        return getZFrame(df.union(temporaryDF));
    }

    protected abstract ZFrame<D, R, C> getZFrame(D df);
}
