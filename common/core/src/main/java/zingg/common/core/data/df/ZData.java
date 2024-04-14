package zingg.common.core.data.df;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.util.ColName;
import zingg.common.core.context.Context;
import zingg.common.core.preprocess.IPreProcessor;

public class ZData<S, D, R, C, T> {

	protected ZFrame<D,R,C> rawData;
	protected IArguments args;
	protected Context<S,D,R,C,T> context;
	protected List<IPreProcessor<S,D,R,C,T>> preProcessors;
	
	protected FieldDefFrame<D, R, C> fieldDefFrame;
	protected BlockedFrame<S, D, R, C, T> blockedFrame;
	protected PreprocessedFrame<S, D, R, C, T> preprocessedFrame;
	protected RepartitionFrame<D, R, C> repartitionFrame;
	
	public static final Log LOG = LogFactory.getLog(ZData.class);   
	
	public ZData(ZFrame<D, R, C> rawData, IArguments args, Context<S,D,R,C,T> context,List<IPreProcessor<S,D,R,C,T>> preProcessors) throws ZinggClientException {
		this.rawData = rawData;
		this.args = args;
		this.context = context;
		this.preProcessors = preProcessors;
	}

	public ZFrame<D, R, C> getRawData() {
		return rawData;
	}

	public FieldDefFrame<D, R, C> getFieldDefFrame() {
		return fieldDefFrame;
	}
	
	public PreprocessedFrame<S, D, R, C, T> getPreprocessedFrame() {
		return preprocessedFrame;
	}	
	
	public RepartitionFrame<D, R, C> getRepartitionFrame() {
		return repartitionFrame;
	}	
	
	public BlockedFrame<S, D, R, C, T>  getBlockedFrame() {
		return blockedFrame;
	}

	public void process() throws ZinggClientException {
		try {
			setFieldDefFrame();
			setPreprocessedFrame();
			setRepartitionFrame();
			setBlockedFrame();
		} catch (ZinggClientException e) {
			throw e;
		} catch (Exception e) {
			throw new ZinggClientException(e);
		}
	}

	protected void setFieldDefFrame() {
		this.fieldDefFrame = new FieldDefFrame<D, R, C>(getRawData(),args.getFieldDefinition(),getColSelector());
		this.fieldDefFrame.process();
	}

	protected ZidAndFieldDefSelector getColSelector() {
		return new ZidAndFieldDefSelector(args.getFieldDefinition());
	}

	protected void setPreprocessedFrame() throws ZinggClientException {
		this.preprocessedFrame = new PreprocessedFrame<S, D, R, C, T>(getFieldDefFrame().getProcessedDF(),preProcessors);
		this.preprocessedFrame.process();
	}

	protected void setRepartitionFrame() {
		this.repartitionFrame = new RepartitionFrame<D, R, C>(getPreprocessedFrame().getProcessedDF(),args.getNumPartitions(),ColName.ID_COL);
		this.repartitionFrame.process();
	}

	protected void setBlockedFrame() throws Exception, ZinggClientException {
		this.blockedFrame = new BlockedFrame<S, D, R, C, T>(getRepartitionFrame().getProcessedDF(), args, context.getBlockingTreeUtil());
		this.blockedFrame.process();
	}
	
}
