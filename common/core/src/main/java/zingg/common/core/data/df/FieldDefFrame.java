package zingg.common.core.data.df;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.cols.ZidAndFieldDefSelector;

public class FieldDefFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected IArguments args;
	
	public static final Log LOG = LogFactory.getLog(FieldDefFrame.class);   
	
	public FieldDefFrame(ZFrame<D, R, C> originalDF, IArguments args) {
		super();
		this.originalDF = originalDF;
		this.args = args;
		this.processedDF = getOriginalDF().select(new ZidAndFieldDefSelector(args.getFieldDefinition()).getCols());
//		return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
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
