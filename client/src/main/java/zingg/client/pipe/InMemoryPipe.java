package zingg.client.pipe;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class InMemoryPipe extends Pipe{
    Dataset <Row> dataset;
	
    public InMemoryPipe(Dataset<Row> ds) {
		dataset = ds;
	}

	public InMemoryPipe() {
	}

    public Dataset <Row> getRecords() {
		return dataset;
	}

	public void setRecords(Dataset <Row> ds) {
		dataset = ds;
	}

	public InMemoryPipe(Pipe p) {
		clone(p);
	}
	
	@Override
	public Format getFormat() {
		return Format.INMEMORY;
	}
}