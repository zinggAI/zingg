package zingg.common.core.executor;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.IZArgs;
import zingg.common.client.ILabelDataViewHelper;
import zingg.common.client.ITrainingDataModel;
import zingg.common.client.IZingg;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOption;
import zingg.common.client.options.ZinggOptions;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;
import zingg.common.client.util.DSUtil;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.context.Context;
import zingg.common.core.util.Analytics;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.GraphUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.Metric;
import zingg.common.core.util.ModelUtil;


public abstract class ZinggBase<S,D, R, C, T> extends ZinggBaseCommon<S, D, R, C, T> {

    protected IArguments args;

    public void track( boolean collectMetrics){
        Analytics.track(Metric.TOTAL_FIELDS_COUNT, args.getFieldDefinition().size(), collectMetrics);
        Analytics.track(Metric.MATCH_FIELDS_COUNT, getDSUtil().getFieldDefinitionFiltered(args, MatchType.DONT_USE).size(),
                collectMetrics);
		Analytics.track(Metric.DATA_FORMAT, getPipeUtil().getPipesAsString(args.getData()), collectMetrics);
		Analytics.track(Metric.OUTPUT_FORMAT, getPipeUtil().getPipesAsString(args.getOutput()), collectMetrics);
        Analytics.track(Metric.MODEL_ID, args.getModelId(), collectMetrics);


    }


   
   
    @Override
    public IArguments getArgs(){
        return args;
    }

	public void setArgs(IZArgs a){
        this.args = (IArguments) a;
    }

    public ZFrame<D,R,C> getMarkedRecords() {
		try {
            return getPipeUtil().read(false, false, 
                getModelHelper().getTrainingDataMarkedPipe(args));
        } catch (ZinggClientException e) {
            return null;
        }
	}

	public ZFrame<D,R,C> getUnmarkedRecords(){
        try{
            ZFrame<D,R,C> unmarkedRecords = null;
            ZFrame<D,R,C> markedRecords = null;
            unmarkedRecords = getPipeUtil().read(false, false, getModelHelper().getTrainingDataUnmarkedPipe(args));
            markedRecords = getMarkedRecords();
            if (markedRecords != null ) {
                unmarkedRecords = unmarkedRecords.join(markedRecords,ColName.CLUSTER_COLUMN, false, "left_anti");
            }
            return unmarkedRecords;
        }
        catch(ZinggClientException e) {
            return null;
        }
	}

   
    public Long getMarkedRecordsStat(ZFrame<D,R,C> markedRecords, long value) {
        return markedRecords.filter(markedRecords.equalTo(ColName.MATCH_FLAG_COL, value)).count() / 2;
    }

    
    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_MATCH);
    }

    
    public Long getUnmatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_A_MATCH);
    }

   
    public Long getUnsureMarkedRecordsStat(ZFrame<D,R,C> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_SURE);
    }
   
 }