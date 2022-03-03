package zingg.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import scala.collection.JavaConverters;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.MatchType;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DSUtil {

    public static final Log LOG = LogFactory.getLog(DSUtil.class);	

	public static final String[] getPrefixedColumns(String[] cols) {
		for (int i=0; i < cols.length; ++i) {
			cols[i] = ColName.COL_PREFIX + cols[i];
		}
		return cols;
	}

	public static Dataset<Row> getPrefixedColumnsDS(Dataset<Row> lines) {
		return lines.toDF(getPrefixedColumns(lines.columns()));
	}

	public static Dataset<Row> join(Dataset<Row> lines, Dataset<Row> lines1, String joinColumn, boolean filter) {
		Dataset<Row> pairs = lines.join(lines1, lines.col(joinColumn).equalTo(lines1.col(ColName.COL_PREFIX + joinColumn)));
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		if (filter) {
			pairs = pairs.filter(pairs.col(ColName.ID_COL).gt(pairs.col(ColName.COL_PREFIX + ColName.ID_COL)));	
		}	
		return pairs;
	}

	public static Dataset<Row> joinZColFirst(Dataset<Row> lines, Dataset<Row> lines1, String joinColumn, boolean filter) {
		Dataset<Row> pairs = lines.join(lines1, lines.col(ColName.COL_PREFIX + joinColumn).equalTo(lines1.col(joinColumn)), "right");
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		if (filter) pairs = pairs.filter(pairs.col(ColName.ID_COL).gt(pairs.col(ColName.COL_PREFIX + ColName.ID_COL)));		
		return pairs;
	}

	/*

	public static Dataset<Row> joinOnNamedColAndDropIt(Dataset<Row> lines, Dataset<Row> lines1, String joinColumn) {
		Dataset<Row> pairs = lines.join(lines1, lines.col(joinColumn).equalTo(lines1.col(joinColumn).as(
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
	
    public static Dataset<Row> joinWithItself(Dataset<Row> lines, String joinColumn, boolean filter) throws Exception {
		Dataset<Row> lines1 = getPrefixedColumnsDS(lines); 
		return join(lines, lines1, joinColumn, filter);
	}
	
	public static Dataset<Row> joinWithItselfSourceSensitive(Dataset<Row> lines, String joinColumn, Arguments args) throws Exception {
		Dataset<Row> lines1 = getPrefixedColumnsDS(lines).cache();
		String[] sourceNames = args.getPipeNames();
		lines = lines.filter(lines.col(ColName.SOURCE_COL).equalTo(sourceNames[0]));
		lines1 = lines1.filter(lines1.col(ColName.COL_PREFIX + ColName.SOURCE_COL).notEqual(sourceNames[0]));
		return join(lines, lines1, joinColumn, false);
	}

	public static Dataset<Row> alignLinked(Dataset<Row> dupesActual, Arguments args) {
		dupesActual = dupesActual.cache();
		dupesActual = dupesActual.withColumnRenamed(ColName.ID_COL, ColName.CLUSTER_COLUMN);
		List<Column> cols = new ArrayList<Column>();
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));					
		}	
		cols.add(dupesActual.col(ColName.SOURCE_COL));	

		Dataset<Row> dupes1 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
		dupes1 = dupes1.dropDuplicates(ColName.CLUSTER_COLUMN, ColName.SOURCE_COL);
	 	List<Column> cols1 = new ArrayList<Column>();
		cols1.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols1.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols1.add(dupesActual.col(ColName.COL_PREFIX + def.fieldName));			
		}		
		cols1.add(dupesActual.col(ColName.COL_PREFIX +ColName.SOURCE_COL));
		/*if (args.getJobId() != -1) {
			cols1.add(dupesActual.col(ColName.SPARK_JOB_ID_COL));
		}*/
		
		
		Dataset<Row> dupes2 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols1.iterator()).asScala().toSeq());
	 	dupes2 = dupes2.toDF(dupes1.columns()).cache();
		dupes1 = dupes1.union(dupes2);
		return dupes1;
	}

	public static Dataset<Row> alignDupes(Dataset<Row> dupesActual, Arguments args) {
		dupesActual = dupesActual.cache();
		List<Column> cols = new ArrayList<Column>();
		
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.PREDICTION_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));						
		}
		cols.add(dupesActual.col(ColName.SOURCE_COL));
		
		Dataset<Row> dupes1 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
	 	List<Column> cols1 = new ArrayList<Column>();
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
		
		
		Dataset<Row> dupes2 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols1.iterator()).asScala().toSeq());
	 	dupes2 = dupes2.toDF(dupes1.columns()).cache();
		dupes1 = dupes1.union(dupes2);
		dupes1 = dupes1.withColumn(ColName.MATCH_FLAG_COL, functions.lit(ColValues.MATCH_TYPE_UNKNOWN));
		return dupes1;
	}

	public static Dataset<Row> allFieldsEqual(Dataset<Row> a, Arguments args) {
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				 a= a.filter(a.col(field).equalTo(
					 a.col(ColName.COL_PREFIX + field)));		
			}
		}
		LOG.info("All equals done");
		return a;	
		
	}

	public static List<Column> getFieldDefColumns (Dataset<Row> ds, Arguments args, boolean includeZid) {
		List<Column> cols = new ArrayList<Column>();
		if (includeZid) {
			cols.add(ds.col(ColName.ID_COL));						
		}
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(ds.col(def.fieldName));						
		}
		cols.add(ds.col(ColName.SOURCE_COL));
		return cols;

	}

	public static Dataset<Row> getFieldDefColumnsDS(Dataset<Row> ds, Arguments args, boolean includeZid) {
		return select(ds, getFieldDefColumns(ds, args, includeZid));
	}

	public static Dataset<Row> select(Dataset<Row> ds, List<Column> cols) {
		return ds.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
	}

	public static Dataset<Row> dropDuplicates(Dataset<Row> a, Arguments args) {
		LOG.info("duplicates before " + a.count());
		List<String> cols = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				cols.add(field);	
			}
		}
		a = a.dropDuplicates(cols.stream().toArray(String[]::new));
		LOG.info("duplicates after " + a.count());
		return a;			
	}	

	public static Dataset<Row> getTraining(SparkSession spark, Arguments args) {
		return getTraining(spark, args, PipeUtil.getTrainingDataMarkedPipe(args)); 			
	}

	public static Dataset<Row> getTrainingJdbc(SparkSession spark, Arguments args) {
		return getTraining(spark, args, args.getOutput()[0]);
	}

	private static Dataset<Row> getTraining(SparkSession spark, Arguments args, Pipe p) {
		Dataset<Row> trFile = null;
		try{
			trFile = PipeUtil.read(spark, 
					false, false, p); 
			LOG.warn("Read marked training samples ");
			trFile = trFile.drop(ColName.PREDICTION_COL);
			trFile = trFile.drop(ColName.SCORE_COL);				
		}
		catch (Exception e) {
			LOG.warn("No preexisting marked training samples");
		}
		if (args.getTrainingSamples() != null) {
			Dataset<Row> trSamples = PipeUtil.read(spark, 
				true, false, args.getTrainingSamples()); 
			LOG.warn("Read all training samples ");
			trFile = (trFile == null) ? trSamples : trFile.unionByName(trSamples, true);
		} 
		else {
			LOG.warn("No configured training samples");
		}
		if (trFile == null) LOG.warn("No training data found");
		return trFile;		
	}

	public static List<FieldDefinition> getFieldDefinitionFiltered(Arguments args, MatchType type) {
		return args.getFieldDefinition()
				.stream()
				.filter(f -> !(f.getMatchType() == null || f.getMatchType().equals(MatchType.DONT_USE)))
				.collect(Collectors.toList());
	}
}
