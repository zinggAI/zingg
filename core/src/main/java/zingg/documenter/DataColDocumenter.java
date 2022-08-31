package zingg.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.client.Arguments;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;

public class DataColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	
	public DataColDocumenter(S session, Arguments args) {
		super(session, args);
	}

	public void process(ZFrame<D, R, C> data) throws ZinggClientException {
	}

}