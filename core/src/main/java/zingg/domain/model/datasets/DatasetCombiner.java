package zingg.domain.model.datasets;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface DatasetCombiner {

    Dataset<Row> combine(Dataset<Row> dataset1, Dataset<Row> dataset2);

}
