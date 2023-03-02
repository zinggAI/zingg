package zingg.util;


import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ColValues;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class DSUtil<S, D, R, C> {

	S session;

	public DSUtil(S s) {
		this.session = s;
	}

	public S getSession() {
		return this.session;
	}

	public void setSession(S session) {
		this.session = session;
	}

    public static final Log LOG = LogFactory.getLog(DSUtil.class);	
				
	public static final String[] getPrefixedColumns(String[] cols) {
		for (int i=0; i < cols.length; ++i) {
			cols[i] = ColName.COL_PREFIX + cols[i];
		}
		return cols;
	}

	public ZFrame<D, R, C> getPrefixedColumnsDS(ZFrame<D, R, C> lines) {
		return lines.toDF(getPrefixedColumns(lines.columns()));
	}

	

	public ZFrame<D, R, C> join(ZFrame<D, R, C> lines, ZFrame<D, R, C> lines1, String joinColumn, boolean filter) {
		ZFrame<D, R, C> pairs = lines.join(lines1, joinColumn);
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		if (filter) {
			pairs = pairs.filter(pairs.gt(ColName.ID_COL));	
		}	
		pairs.show(true);
		return pairs;
	}

	public ZFrame<D, R, C> joinZColFirst(ZFrame<D, R, C> lines, ZFrame<D, R, C> lines1, String joinColumn, boolean filter) {
		ZFrame<D, R, C> pairs = lines.joinRight(lines1, joinColumn);
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		if (filter) pairs = pairs.filter(pairs.gt(ColName.ID_COL));		
		return pairs;
	}

	public ZFrame<D, R, C> addUniqueCol(ZFrame<D, R, C> dupesActual, String colName) {
		String append = System.currentTimeMillis() + ":";
		dupesActual = dupesActual.withColumn(colName + "temp", 
				append);
		dupesActual = dupesActual.withColumn(colName,
				dupesActual.concat(dupesActual.col(colName + "temp"),
						dupesActual.col(colName)));
		dupesActual = dupesActual.drop(colName + "temp");
		return dupesActual;
	}

	/*

	public  ZFrame<D, R, C> joinOnNamedColAndDropIt(ZFrame<D, R, C> lines, ZFrame<D, R, C> lines1, String joinColumn) {
		ZFrame<D, R, C> pairs = lines.join(lines1, lines.col(joinColumn).equalTo(lines1.col(joinColumn).as(
			ColName.COL_PREFIX + joinColumn)));
		pairs.show(false);
		pairs = pairs.drop(ColName.COL_PREFIX + joinColumn);
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		pairs.show(false);
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		return pairs;
	}
	*/
	
    public ZFrame<D, R, C> joinWithItself(ZFrame<D, R, C> lines, String joinColumn, boolean filter) throws Exception {
		ZFrame<D, R, C> lines1 = getPrefixedColumnsDS(lines); 
		System.out.println("prefixed");
		lines1.show(true);
		return join(lines, lines1, joinColumn, filter);
	}
	
	public  ZFrame<D, R, C> joinWithItselfSourceSensitive(ZFrame<D, R, C> lines, String joinColumn, Arguments args) throws Exception {
		ZFrame<D, R, C> lines1 = getPrefixedColumnsDS(lines).cache();
		String[] sourceNames = args.getPipeNames();
		lines = lines.filter(lines.equalTo(joinColumn, sourceNames[0]));
		lines1 = lines1.filter(lines1.notEqual(ColName.COL_PREFIX + ColName.SOURCE_COL, sourceNames[0]));
		return join(lines, lines1, joinColumn, false);
	}

	public  ZFrame<D, R, C> alignLinked(ZFrame<D, R, C> dupesActual, Arguments args) {
		dupesActual = dupesActual.cache();
		dupesActual = dupesActual.withColumnRenamed(ColName.ID_COL, ColName.CLUSTER_COLUMN);
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

	public  ZFrame<D, R, C> alignDupes(ZFrame<D, R, C> dupesActual, Arguments args) {
		dupesActual = dupesActual.cache();
		List<C> cols = new ArrayList<C>();
		
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.PREDICTION_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));						
		}
		cols.add(dupesActual.col(ColName.SOURCE_COL));
		
		ZFrame<D, R, C> dupes1 = dupesActual.select(cols);
	 	List<C> cols1 = new ArrayList<C>();
		cols1.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols1.add(dupesActual.col(ColName.COL_PREFIX + ColName.ID_COL)); 
		cols1.add(dupesActual.col(ColName.PREDICTION_COL));
		//cols1.add(dupesActual.col(ColName.PROBABILITY_COL));
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
		dupes1 = dupes1.withColumn(ColName.MATCH_FLAG_COL,ColValues.MATCH_TYPE_UNKNOWN);
		return dupes1;
	}

	public  ZFrame<D, R, C> allFieldsEqual(ZFrame<D, R, C> a, Arguments args) {
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				 a= a.filter(a.equalTo(field,ColName.COL_PREFIX + field));		
			}
		}
		LOG.info("All equals done");
		return a;	
		
	}

	public  List<C> getFieldDefColumns (ZFrame<D, R, C> ds, Arguments args, boolean includeZid, boolean showConcise) {
		List<C> cols = new ArrayList<C>();
		if (includeZid) {
			cols.add(ds.col(ColName.ID_COL));						
		}
		for (FieldDefinition def: args.getFieldDefinition()) {
			if (showConcise && def.matchType.contains(MatchType.DONT_USE)) {
				continue;
			}
			cols.add(ds.col(def.fieldName));						
		}
		cols.add(ds.col(ColName.SOURCE_COL));
		return cols;

	}

	public  ZFrame<D, R, C> getFieldDefColumnsDS(ZFrame<D, R, C> ds, Arguments args, boolean includeZid) {
		return select(ds, getFieldDefColumns(ds, args, includeZid, false));
	}

	public  ZFrame<D, R, C> select(ZFrame<D, R, C> ds, List<C> cols) {
		return ds.select(cols);
	}

	public  ZFrame<D, R, C> dropDuplicates(ZFrame<D, R, C> a, Arguments args) {
		LOG.info("duplicates before " + a.count());
		List<String> cols = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().contains(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				cols.add(field);	
			}
		}
		a = a.dropDuplicates(cols.stream().toArray(String[]::new));
		LOG.info("duplicates after " + a.count());
		return a;			
	}	

	public  ZFrame<D, R, C> getTraining(PipeUtilBase<S, D, R, C> pipeUtil, Arguments args) {
		return getTraining(pipeUtil, args, pipeUtil.getTrainingDataMarkedPipe(args)); 			
	}
	
	private  ZFrame<D, R, C> getTraining(PipeUtilBase<S, D, R, C> pipeUtil, Arguments args, Pipe<D,R,C> p) {
		ZFrame<D, R, C> trFile = null;
		try{
			trFile = pipeUtil.read(false, false, p); 
			LOG.warn("Read marked training samples ");
			trFile = trFile.drop(ColName.PREDICTION_COL);
			trFile = trFile.drop(ColName.SCORE_COL);				
		
			if (args.getTrainingSamples() != null) {
				ZFrame<D, R, C> trSamples = pipeUtil.read(true, false, args.getTrainingSamples()); 
				LOG.warn("Read all training samples ");
				trFile = (trFile == null) ? trSamples : trFile.unionByName(trSamples, true);
			} 
			else {
				LOG.warn("No configured training samples");
			}
		}
		catch (ZinggClientException e) {
			LOG.warn("No preexisting marked training samples");
		}
		if (trFile == null) LOG.warn("No training data found");
		return trFile;		
	}

	public  List<FieldDefinition> getFieldDefinitionFiltered(Arguments args, MatchType type) {
		return args.getFieldDefinition()
				.stream()
				.filter(f -> !(f.getMatchType() == null || f.getMatchType().contains(type)))
				.collect(Collectors.toList());
	}

    public ZFrame<D,R,C> postprocess(ZFrame<D,R,C> actual, ZFrame<D,R,C> orig) {
		System.out.println("postproc  actual");
		actual.show(true);
		System.out.println("postproc  orig");
		orig.show(true);
    	List<C> cols = new ArrayList<C>();	
    	cols.add(actual.col(ColName.CLUSTER_COLUMN));
    	cols.add(actual.col(ColName.ID_COL));
    	cols.add(actual.col(ColName.PREDICTION_COL));
    	cols.add(actual.col(ColName.SCORE_COL));
    	cols.add(actual.col(ColName.MATCH_FLAG_COL));
    
    	ZFrame<D,R,C> zFieldsFromActual = actual.select(cols);
    	System.out.println("postproc  selected");
		zFieldsFromActual.show(true);
    	ZFrame<D,R,C> joined = zFieldsFromActual.joinOnCol(orig, ColName.ID_COL);
		
    	return joined;
    }

    public ZFrame<D,R,C> postprocessLinked(ZFrame<D,R,C> actual, ZFrame<D,R,C> orig) {
    	List<C> cols = new ArrayList<C>();
        cols.add(actual.col(ColName.CLUSTER_COLUMN));	
    	cols.add(actual.col(ColName.ID_COL));
    	cols.add(actual.col(ColName.SCORE_COL));
    	cols.add(actual.col(ColName.SOURCE_COL));	
    
    	ZFrame<D,R,C> zFieldsFromActual = actual.select(cols);
    	ZFrame<D,R,C> joined = zFieldsFromActual.join(orig,ColName.ID_COL,ColName.SOURCE_COL)
    					.drop(ColName.SOURCE_COL)
    					.drop(ColName.ID_COL);
    
    	return joined;
    }

	public abstract ZFrame<D, R, C> addClusterRowNumber(ZFrame<D, R, C> ds);

	public abstract ZFrame<D, R, C> addRowNumber(ZFrame<D, R, C> ds);

	public ZFrame<D, R, C> emptyDataFrame() {
		return null;
	}
}
