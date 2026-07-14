package zingg.common.core.documenter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.IContext;

public abstract class DataColDocumenter<S,D,R,C,T> extends DocumenterBase<S,D,R,C,T> {
	protected static String name = "zingg.DataColDocumenter";
	public static final Log LOG = LogFactory.getLog(DataColDocumenter.class);
	
	public DataColDocumenter(IContext<S,D,R,C,T> context, IZArgs args, ClientOptions c) {
		super(context, args, c);
	}

	public void process(ZFrame<D, R, C> data) throws ZinggClientException {
	}

	@Override
	public void execute() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

}