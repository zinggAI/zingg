package zingg.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public abstract class DataColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	
	public DataColDocumenter(S session, Arguments args) {
		super(session, args);
	}

	public void process(ZFrame<D, R, C> data) throws ZinggClientException {
	}

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

}