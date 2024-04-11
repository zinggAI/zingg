package zingg.common.core.data.df;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.cols.ZidAndFieldDefSelector;

public class FieldDefFrame<S, D, R, C, T> implements IZFrameProcessor<S, D, R, C, T> {

	protected ZFrame<D,R,C> originalDF;
	
	protected ZFrame<D,R,C> processedDF;
	
	protected ZidAndFieldDefSelector zidAndFieldDefSelector;
	
	protected List<? extends FieldDefinition> fieldDefinition;
	
	public static final Log LOG = LogFactory.getLog(FieldDefFrame.class);   
	
	public FieldDefFrame(ZFrame<D, R, C> originalDF, List<? extends FieldDefinition> fieldDefinition) {
		this(originalDF,fieldDefinition,new ZidAndFieldDefSelector(fieldDefinition));
	}

	public FieldDefFrame(ZFrame<D, R, C> originalDF, List<? extends FieldDefinition> fieldDefinition,
			ZidAndFieldDefSelector zidAndFieldDefSelector) {
		this.originalDF = originalDF;
		this.fieldDefinition = fieldDefinition;
		this.zidAndFieldDefSelector = zidAndFieldDefSelector;
	}

	@Override
	public ZFrame<D, R, C> getOriginalDF() {
		return originalDF;
	}

	@Override
	public ZFrame<D, R, C> getProcessedDF() {
		return processedDF;
	}
	
	@Override
	public void process() {
		this.processedDF = getOriginalDF().select(zidAndFieldDefSelector.getCols());
//		return getDSUtil().getFieldDefColumnsDS(testDataOriginal, args, true);
	}

}
