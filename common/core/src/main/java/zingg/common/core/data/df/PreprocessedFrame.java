package zingg.common.core.data.df;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.IPreProcessor;

public class PreprocessedFrame<S, D, R, C, T> extends AbstractZFrameProcessor<D, R, C> {

	protected List<IPreProcessor<S,D,R,C,T>> preProcessors;
	
	public static final Log LOG = LogFactory.getLog(PreprocessedFrame.class);   
	
	public PreprocessedFrame(ZFrame<D, R, C> originalDF, List<IPreProcessor<S,D,R,C,T>> preProcessors) throws ZinggClientException {
		super();
		this.originalDF = originalDF;
		this.preProcessors = preProcessors;
	}

	public void process() throws ZinggClientException {		
		processedDF = getOriginalDF();
		for (IPreProcessor<S,D,R,C,T> iPreProcessor : preProcessors) {
			processedDF = iPreProcessor.preprocess(processedDF);
		}
	}
	
}
