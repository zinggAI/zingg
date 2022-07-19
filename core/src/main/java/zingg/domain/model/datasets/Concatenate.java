package zingg.domain.model.datasets;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class Concatenate implements DatasetCombiner{

    @Override
    public Dataset<Row> combine(Dataset<Row> dataset1, Dataset<Row> dataset2) {
        return dataset1.union(dataset2);
    }
}
