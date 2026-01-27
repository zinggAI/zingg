package zingg.common.core.executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;
import zingg.common.core.documenter.DataDocumenter;
import zingg.common.core.documenter.ModelDocumenter;

public abstract class Documenter<S,D,R,C,T> extends ZinggBase<S,D,R,C,T> {

	protected static String name = "zingg.Documenter";
	public static final Log LOG = LogFactory.getLog(Documenter.class);

	public Documenter() {
		setZinggOption(ZinggOptions.GENERATE_DOCS);
 	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Documenter starts");
			//Documentation out of model
			ModelDocumenter modelDoc = getModelDocumenter();
			modelDoc.process();

			//Documentation of data
			DataDocumenter dataDoc = getDataDocumenter();
			dataDoc.process();
			

			LOG.info("Documenter finishes");
		} catch (Exception e) {
			throw new ZinggClientException("Error in Documenter.execute ", e);
		}
	}
	
	protected abstract ModelDocumenter<S,D,R,C,T> getModelDocumenter();
	
	protected abstract DataDocumenter<S,D,R,C,T> getDataDocumenter();
	
}
