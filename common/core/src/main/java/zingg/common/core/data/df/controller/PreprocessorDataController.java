package zingg.common.core.data.df.controller;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.IPreProcessor;

public class PreprocessorDataController<S,D,R,C,T> implements IDataController<D, R, C> {
	
	protected List<IPreProcessor<S,D,R,C,T>> preProcessors;
	
	public static final Log LOG = LogFactory.getLog(PreprocessorDataController.class);   
	
	public PreprocessorDataController(List<IPreProcessor<S,D,R,C,T>> preProcessors) throws ZinggClientException {
		this.preProcessors = preProcessors;
	}

	@Override
	public ZFrame<D, R, C> process(ZFrame<D,R,C> originalDF) throws ZinggClientException {
		ZFrame<D,R,C> processedDF = originalDF;
		if (preProcessors != null) {
			for (IPreProcessor<S, D, R, C, T> iPreProcessor : preProcessors) {
				processedDF = iPreProcessor.preprocess(processedDF);
			} 
		}
		return processedDF;
	}
}
