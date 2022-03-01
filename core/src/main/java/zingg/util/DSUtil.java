package zingg.util;

import com.snowflake.snowpark.Column;
import com.snowflake.snowpark.DataFrame;
import com.snowflake.snowpark.Row;
import com.snowflake.snowpark.Session;
import com.snowflake.snowpark.functions;
import scala.collection.mutable.*;

import scala.collection.Seq;
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

	public static DataFrame getPrefixedColumnsDS(DataFrame lines) {
		//return lines.toDF(getPrefixedColumns(lines.columns()));
		return lines.toDF(getPrefixedColumns((String [])JavaConverters.asJavaCollectionConverter(lines.schema().names()).asJavaCollection().toArray()));
	}

	public static DataFrame join(DataFrame lines, DataFrame lines1, String joinColumn, boolean filter) {
		DataFrame pairs = lines.join(lines1, lines.col(joinColumn).$eq$eq$eq(lines1.col(ColName.COL_PREFIX + joinColumn)));
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

	public static DataFrame joinZColFirst(DataFrame lines, DataFrame lines1, String joinColumn, boolean filter) {
		DataFrame pairs = lines.join(lines1, lines.col(ColName.COL_PREFIX + joinColumn).$eq$eq$eq(lines1.col(joinColumn)), "right");
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		if (LOG.isDebugEnabled()) {
			LOG.debug("pairs length " + pairs.count());
		}
		if (filter) pairs = pairs.filter(pairs.col(ColName.ID_COL).gt(pairs.col(ColName.COL_PREFIX + ColName.ID_COL)));		
		return pairs;
	}

	/*

	public static DataFrame joinOnNamedColAndDropIt(DataFrame lines, DataFrame lines1, String joinColumn) {
		DataFrame pairs = lines.join(lines1, lines.col(joinColumn).equalTo(lines1.col(joinColumn).as(
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
	
    public static DataFrame joinWithItself(DataFrame lines, String joinColumn, boolean filter) throws Exception {
		DataFrame lines1 = getPrefixedColumnsDS(lines); 
		return join(lines, lines1, joinColumn, filter);
	}
	
	public static DataFrame joinWithItselfSourceSensitive(DataFrame lines, String joinColumn, Arguments args) throws Exception {
		DataFrame lines1 = getPrefixedColumnsDS(lines); //.cache();
		String[] sourceNames = args.getPipeNames();
		lines = lines.filter(lines.col(ColName.SOURCE_COL).$eq$eq$eq(sourceNames[0]));
		lines1 = lines1.filter(lines1.col(ColName.COL_PREFIX + ColName.SOURCE_COL).$eq$bang$eq(sourceNames[0]));
		return join(lines, lines1, joinColumn, false);
	}

	public static DataFrame alignLinked(DataFrame dupesActual, Arguments args) {
		//dupesActual = dupesActual.cache();
		dupesActual = dupesActual.rename( ColName.CLUSTER_COLUMN, dupesActual.col(ColName.ID_COL));
		List<Column> cols = new ArrayList<Column>();
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));					
		}	
		cols.add(dupesActual.col(ColName.SOURCE_COL));	

		DataFrame dupes1 = dupesActual.select((Column [])cols.toArray());
		List<String> colsDupes = new ArrayList<String>();
		colsDupes.add(ColName.CLUSTER_COLUMN);
		colsDupes.add(ColName.SOURCE_COL);
		dupes1 = dupes1.dropDuplicates(JavaConverters.asScalaIteratorConverter(colsDupes.iterator()).asScala().toSeq());
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
		
		
		DataFrame dupes2 = dupesActual.select((Column [])cols1.toArray());
	 	dupes2 = dupes2.toDF(dupes1.schema().names()); //.cache();
		dupes1 = dupes1.union(dupes2);
		return dupes1;
	}

	public static DataFrame alignDupes(DataFrame dupesActual, Arguments args) {
		//dupesActual = dupesActual.cache();
		List<Column> cols = new ArrayList<Column>();
		
		cols.add(dupesActual.col(ColName.CLUSTER_COLUMN));
		cols.add(dupesActual.col(ColName.ID_COL));
		cols.add(dupesActual.col(ColName.PREDICTION_COL));
		cols.add(dupesActual.col(ColName.SCORE_COL));
		
		for (FieldDefinition def: args.getFieldDefinition()) {
			cols.add(dupesActual.col(def.fieldName));						
		}
		cols.add(dupesActual.col(ColName.SOURCE_COL));
		
		DataFrame dupes1 = dupesActual.select((Column [])cols.toArray());
//		DataFrame dupes1 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());

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
		
		
		DataFrame dupes2 = dupesActual.select((Column [])cols1.toArray()); //.asScalaIteratorConverter(cols1.iterator()).asScala().toSeq());
		//DataFrame dupes2 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols1.iterator()).asScala().toSeq());
	 	dupes2 = dupes2.toDF(dupes1.schema().names()); //columns()).cache();
		dupes1 = dupes1.union(dupes2);
		dupes1 = dupes1.withColumn(ColName.MATCH_FLAG_COL, functions.lit(ColValues.MATCH_TYPE_UNKNOWN));
		return dupes1;
	}

	public static DataFrame allFieldsEqual(DataFrame a, Arguments args) {
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				 a= a.filter(a.col(field).$eq$eq$eq(
					 a.col(ColName.COL_PREFIX + field)));		
			}
		}
		LOG.info("All equals done");
		return a;	
		
	}

	public static List<Column> getFieldDefColumns (DataFrame ds, Arguments args, boolean includeZid) {
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

	public static DataFrame getFieldDefColumnsDS(DataFrame ds, Arguments args, boolean includeZid) {
		return select(ds, getFieldDefColumns(ds, args, includeZid));
	}

	public static DataFrame select(DataFrame ds, List<Column> cols) {
		return ds.select(cols.stream().toArray(String[]::new));
	}

	public static DataFrame dropDuplicates(DataFrame a, Arguments args) {
		LOG.info("duplicates before " + a.count());
		List<String> cols = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (! (def.getMatchType() == null || def.getMatchType().equals(MatchType.DONT_USE))) {
				//columns.add(def.getFieldName());
				String field = def.getFieldName();
				cols.add(field);	
			}
		}
		//a = a.dropDuplicates(cols.stream().toArray(String[]::new).asScala());
		a = a.dropDuplicates(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
		//DataFrame dupes1 = dupesActual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
		LOG.info("duplicates after " + a.count());
		return a;			
	}	

	public static DataFrame getTraining(Session snow, Arguments args) {
		return getTraining(snow, args, PipeUtil.getTrainingDataMarkedPipe(args)); 			
	}

	public static DataFrame getTrainingJdbc(Session snow, Arguments args) {
		return getTraining(snow, args, args.getOutput()[0]);
	}

	private static DataFrame getTraining(Session snow, Arguments args, Pipe p) {
		DataFrame trFile = null;
		try{
			trFile = PipeUtil.read(snow, 
					false, false, p); 
			LOG.warn("Read marked training samples ");
			trFile = trFile.drop(ColName.PREDICTION_COL, null);
			trFile = trFile.drop(ColName.SCORE_COL, null);				
		}
		catch (Exception e) {
			LOG.warn("No preexisting marked training samples");
		}
		if (args.getTrainingSamples() != null) {
			DataFrame trSamples = PipeUtil.read(snow, 
				true, false, args.getTrainingSamples()); 
			LOG.warn("Read all training samples ");
			trFile = (trFile == null) ? trSamples : trFile.unionByName(trSamples);
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
