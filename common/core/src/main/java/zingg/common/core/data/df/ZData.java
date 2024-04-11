package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.preprocess.StopWordsRemover;

public class ZData<S, D, R, C, T> {

	protected ZFrame<D,R,C> rawData;
	protected IArguments args;
	protected Context<S,D,R,C,T> context;
	protected StopWordsRemover<S, D, R, C, T> stopWordsRemover;
	
	protected FieldDefFrame<S, D, R, C, T> fieldDefFrame;
	protected BlockedFrame<S, D, R, C, T> blockedData;
	protected PreprocessedFrame<S, D, R, C, T> preprocessedFrame;
	protected RepartitionFrame<S, D, R, C, T> repartitionFrame;
	
	public static final Log LOG = LogFactory.getLog(ZData.class);   
	
	public ZData(ZFrame<D, R, C> rawData, IArguments args, Context<S,D,R,C,T> context,StopWordsRemover<S, D, R, C, T> stopWordsRemover) throws ZinggClientException {
		try {
			this.rawData = rawData;
			this.args = args;
			this.context = context;
			this.stopWordsRemover = stopWordsRemover;
			this.fieldDefFrame = new FieldDefFrame<S, D, R, C, T>(getRawData(),args);
			this.preprocessedFrame = new PreprocessedFrame<S, D, R, C, T>(getFieldDefFrame().getProcessedDF(),args,stopWordsRemover);
			this.repartitionFrame = new RepartitionFrame<S, D, R, C, T>(getPreprocessedFrame().getProcessedDF(),args);
			this.blockedData = new BlockedFrame<S, D, R, C, T>(getRepartitionFrame().getProcessedDF(), args, context);
		} catch (ZinggClientException e) {
			throw e;
		} catch (Exception e) {
			throw new ZinggClientException(e);
		}
	}

	public ZFrame<D, R, C> getRawData() {
		return rawData;
	}

	public FieldDefFrame<S, D, R, C, T> getFieldDefFrame() {
		return fieldDefFrame;
	}
	
	public PreprocessedFrame<S, D, R, C, T> getPreprocessedFrame() {
		return preprocessedFrame;
	}	
	
	public RepartitionFrame<S, D, R, C, T> getRepartitionFrame() {
		return repartitionFrame;
	}	
	
	public BlockedFrame<S, D, R, C, T>  getBlockedFrame() throws Exception, ZinggClientException{
		return blockedData;
	}

}
