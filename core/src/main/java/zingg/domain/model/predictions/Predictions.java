package zingg.domain.model.predictions;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.client.util.ColName;

import java.util.Iterator;
import java.util.List;

public class Predictions implements Iterable<Prediction> {

    public Prediction getPrediction(int index){
        Dataset<Row> pair = lines.filter(
                lines.col(ColName.CLUSTER_COLUMN).equalTo(clusterIds.get(index).getAs(ColName.CLUSTER_COLUMN))
        ).cache();
        return new Prediction(pair);
    }

    private final Dataset<Row> lines;
    private final List<Row> clusterIds;

    private Predictions(Dataset<Row> lines, List<Row> clusterIds) {
        this.lines = lines;
        this.clusterIds = clusterIds;
    }

    public static Predictions fromLines(Dataset<Row> lines){
        List<Row> clusterIds = getClusterIds(lines);
        return new Predictions(lines, clusterIds);
    }

    private static List<Row> getClusterIds(Dataset<Row> lines) {
        return 	lines.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();
    }

    public int size(){
        return clusterIds.size();
    }

    @Override
    public PredictionIterator iterator() {
        return new PredictionIteratorImpl();
    }



    class PredictionIteratorImpl implements PredictionIterator{
        int idx = 0;

        @Override
        public boolean hasNext() {
            return currentIterationIndex() < totalIterations();
        }

        @Override
        public Prediction next() {
            return getPrediction(idx);
        }

        @Override
        public int currentIterationIndex() {
            return idx;
        }

        @Override
        public int totalIterations() {
            return size();
        }
    }
}
