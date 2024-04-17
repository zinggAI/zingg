package zingg.common.core.data.df;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.cols.SelectedCols;
import zingg.common.client.cols.ZidAndFieldDefSelector;
import zingg.common.client.util.ColName;
import zingg.common.core.context.Context;
import zingg.common.core.data.df.controller.BlockedDataController;
import zingg.common.core.data.df.controller.FieldDefDataController;
import zingg.common.core.data.df.controller.PreprocessorDataController;
import zingg.common.core.data.df.controller.RepartitionDataController;
import zingg.common.core.preprocess.IPreProcessor;

public class ZData<S, D, R, C, T> {

	protected ZFrame<D,R,C> rawData;
	protected IArguments args;
	protected Context<S,D,R,C,T> context;
	protected List<IPreProcessor<S,D,R,C,T>> preProcessors;
	
	protected ZFrameEnriched<D, R, C> fieldDefFrame;
	protected ZFrameEnriched<D, R, C> blockedFrame;
	protected ZFrameEnriched<D, R, C> preprocessedFrame;
	protected ZFrameEnriched<D, R, C> repartitionFrame;
	
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

	public ZFrameEnriched<D, R, C> getFieldDefFrame() throws ZinggClientException {
		if (fieldDefFrame==null) {
			ZFrame<D, R, C> originalDF = getRawData();
			FieldDefDataController<D, R, C> controller = new FieldDefDataController<D, R, C>(args.getFieldDefinition(),
					getColSelector());
			this.fieldDefFrame = new ZFrameEnriched<D, R, C>(originalDF, controller.process(originalDF));
		}
		return fieldDefFrame;
	}
	
	public ZFrameEnriched<D, R, C> getPreprocessedFrame() throws ZinggClientException {
		if (preprocessedFrame==null) {
			ZFrame<D, R, C> originalDF = getFieldDefFrame().getProcessedDF();
			PreprocessorDataController<S, D, R, C, T> controller = new PreprocessorDataController<S, D, R, C, T>(
					preProcessors);
			this.preprocessedFrame = new ZFrameEnriched<D, R, C>(originalDF, controller.process(originalDF));
		}
		return preprocessedFrame;
	}	
	
	public ZFrameEnriched<D, R, C> getRepartitionFrame() throws ZinggClientException {
		if (repartitionFrame==null) {
			ZFrame<D, R, C> originalDF = getPreprocessedFrame().getProcessedDF();
			RepartitionDataController<D, R, C> controller = new RepartitionDataController<D, R, C>(
					args.getNumPartitions(), ColName.ID_COL);
			this.repartitionFrame = new ZFrameEnriched<D, R, C>(originalDF, controller.process(originalDF));
		}
		return repartitionFrame;
	}	
	
	public ZFrameEnriched<D, R, C>  getBlockedFrame() throws ZinggClientException {
		if (blockedFrame==null) {
			try {
				ZFrame<D, R, C> originalDF = getRepartitionFrame().getProcessedDF();
				BlockedDataController<S, D, R, C, T> controller = new BlockedDataController<S, D, R, C, T>(args,
						context.getBlockingTreeUtil());
				this.blockedFrame = new ZFrameEnriched<D, R, C>(originalDF, controller.process(originalDF));
			} catch (ZinggClientException zce) {
				throw zce;
			} catch (Exception e) {
				throw new ZinggClientException(e);
			}
		}
		return blockedFrame;
	}

	protected SelectedCols getColSelector() {
		return new ZidAndFieldDefSelector(args.getFieldDefinition());
	}
	
}
