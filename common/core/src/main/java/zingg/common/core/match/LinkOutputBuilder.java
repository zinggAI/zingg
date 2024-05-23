package zingg.common.core.match;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DSUtil;

public class LinkOutputBuilder<S,D,R,C>  extends AOutputBuilder<S,D,R,C> {

    public static final Log LOG = LogFactory.getLog(GraphMatchOutputBuilder.class); 

    public LinkOutputBuilder(DSUtil<S, D, R, C> dsUtil, IArguments args) {
        super(dsUtil, args);
    }
  
    @Override
    public ZFrame<D,R,C> getOutput(ZFrame<D, R, C> sampleOriginal, ZFrame<D, R, C> dupesActual) 
    throws ZinggClientException, Exception{
        dupesActual = dupesActual.withColumn(ColName.CLUSTER_COLUMN, dupesActual.col(ColName.ID_COL));
		dupesActual = getDSUtil().addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN);
		ZFrame<D,R,C>dupes2 =  alignLinked(dupesActual, args);
		dupes2 =  postprocessLinked(dupes2, sampleOriginal);
		LOG.debug("uncertain output schema is " + dupes2.showSchema());
        return dupes2;

    }

    public  ZFrame<D, R, C> alignLinked(ZFrame<D, R, C> dupesActual, IArguments args) {
		dupesActual = dupesActual.cache();
				
		List<C> cols = new ArrayList<C>();
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
				
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));					
		}	
		cols.add(dupesActual.col(ColName.SOURCE_COL));	

		ZFrame<D, R, C> dupes1 = dupesActual.select(cols);
		dupes1 = dupes1.dropDuplicates(ColName.CLUSTER_COLUMN, ColName.SOURCE_COL);
	 	List<C> cols1 = new ArrayList<C>();
		cols1.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols1.add(dupesActual.col(ColName.COL_PREFIX + ColName.ID_COL));
		cols1.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols1.add(dupesActual.col(ColName.COL_PREFIX + def.fieldName));			
		}		
		cols1.add(dupesActual.col(ColName.COL_PREFIX +ColName.SOURCE_COL));
		/*if (args.getJobId() != -1) {
			cols1.add(dupesActual.col(ColName.SPARK_JOB_ID_COL));
		}*/
		
		
		ZFrame<D, R, C> dupes2 = dupesActual.select(cols1);
	 	dupes2 = dupes2.toDF(dupes1.columns()).cache();
		dupes1 = dupes1.union(dupes2);
		return dupes1;
	}

    public ZFrame<D,R,C> getSelectedCols(ZFrame<D,R,C> actual){
        List<C> cols = new ArrayList<C>();
        cols.add(actual.col(ColName.CLUSTER_COLUMN));	
    	cols.add(actual.col(ColName.ID_COL));
    	cols.add(actual.col(ColName.SCORE_COL));
    	cols.add(actual.col(ColName.SOURCE_COL));	
    
    	ZFrame<D,R,C> zFieldsFromActual = actual.select(cols);
        return zFieldsFromActual;

    }

    public ZFrame<D,R,C> postprocessLinked(ZFrame<D,R,C> actual, ZFrame<D,R,C> orig) {
    	ZFrame<D,R,C> zFieldsFromActual = getSelectedCols(actual);
    	ZFrame<D,R,C> joined = zFieldsFromActual.join(orig,ColName.ID_COL,ColName.SOURCE_COL)
    					.drop(zFieldsFromActual.col(ColName.SOURCE_COL))
    					.drop(ColName.ID_COL);
    	
    	return joined;
    }
}
