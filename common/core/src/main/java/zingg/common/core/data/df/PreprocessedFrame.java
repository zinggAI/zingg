package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.preprocess.StopWordsRemover;

public class PreprocessedFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected IArguments args;
	
	protected StopWordsRemover<S, D, R, C, T> stopWordsRemover;
	
	public static final Log LOG = LogFactory.getLog(PreprocessedFrame.class);   
	
	public PreprocessedFrame(ZFrame<D, R, C> originalDF, IArguments args, StopWordsRemover<S, D, R, C, T> stopWordsRemover) throws ZinggClientException {
		super();
		this.originalDF = originalDF;
		this.args = args;
		this.stopWordsRemover = stopWordsRemover;
		this.processedDF = preprocess();
	}

	protected ZFrame<D, R, C> preprocess() throws ZinggClientException {
		return this.stopWordsRemover.preprocessForStopWords(getOriginalDF());
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}

}
