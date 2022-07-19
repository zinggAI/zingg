package zingg.domain.model.records;

import org.apache.spark.sql.Row;
import zingg.client.pipe.Pipe;
import org.apache.spark.sql.Dataset;

public interface RecordRepository {

    Dataset<Row> getRecords(boolean addLineNo, boolean addSource, Pipe pipe);

}
