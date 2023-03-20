package zingg.common.core.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.Context;

public abstract class DataColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	
	public DataColDocumenter(Context<S,D,R,C,T> context, Arguments args) {
		super(context, args);
	}

	public void process(ZFrame<D, R, C> data) throws ZinggClientException {
	}

	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

}