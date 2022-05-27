package zingg.client.pipe;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public class InMemoryPipe extends Pipe{
    
	public InMemoryPipe() {
	}

	public InMemoryPipe(Dataset <Row> ds){
		dataset = ds;
	}

    public Dataset <Row> getRecords() {
		return dataset;
	}

	public InMemoryPipe(Pipe p) {
		clone(p);
	}
	
	@Override
	public Format getFormat() {
		return Format.INMEMORY;
	}
}