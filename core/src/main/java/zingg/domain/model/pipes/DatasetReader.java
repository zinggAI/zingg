package zingg.domain.model.pipes;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.client.ZinggClientException;

public interface DatasetReader {
    public Dataset<Row> read(boolean addSource) throws ZinggClientException;
}
