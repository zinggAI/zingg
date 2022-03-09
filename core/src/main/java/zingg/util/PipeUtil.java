package zingg.util;

import static com.snowflake.snowpark_java.Functions.col;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.snowflake.snowpark_java.DataFrameReader;
import com.snowflake.snowpark_java.DataFrameWriter;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.SaveMode;
import com.snowflake.snowpark_java.Session;
import com.snowflake.snowpark_java.Functions;
// import org.apache.spark.storage.StorageLevel;

//import zingg.scala.DFUtil;
import zingg.client.Arguments;
import zingg.client.util.ColName;
import zingg.client.util.Util;
import zingg.client.pipe.CassandraPipe;
import zingg.client.pipe.ElasticPipe;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.Format;
import zingg.client.pipe.Pipe;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

//import com.datastax.driver.core.ProtocolVersion;
//import com.datastax.driver.core.ResultSet;
//import com.datastax.driver.core.Session;
//import com.datastax.spark.connector.DataFrameFunctions;

import zingg.scala.DFUtil;

//import com.datastax.spark.connector.cql.*;
//import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
//import zingg.scala.DFUtil;

public class PipeUtil {

	public static final Log LOG = LogFactory.getLog(PipeUtil.class);

	private static DataFrameReader getReader(Session snow, Pipe p) {
		DataFrameReader reader = snow.read();

		LOG.warn("Reading input " + p.getFormat().type());
		reader = reader.format(p.getFormat().type());
		if (p.getSchema() != null) {
			reader = reader.schema(p.getSchema());
		}
		for (String key : p.getProps().keySet()) {
			reader = reader.option(key, p.get(key));
		}
		reader = reader.option("mode", "PERMISSIVE");
		return reader;
	}

	private static DataFrame read(DataFrameReader reader, Pipe p, boolean addSource) {
		DataFrame input = null;
		LOG.warn("Reading " + p);
		if (p.getProps().containsKey(FilePipe.LOCATION)) {
			input = reader.load(p.get(FilePipe.LOCATION));
		}
		else {
			input = reader.load();
		}
		if (addSource) {
			input = input.withColumn(ColName.SOURCE_COL, functions.lit(p.getName()));			
		}
		return input;
	}

	private static DataFrame readInternal(Session snow, Pipe p, boolean addSource) {
		DataFrameReader reader = getReader(snow, p);
		return read(reader, p, addSource);		
	}

	public static DataFrame joinTrainingSetstoGetLabels(DataFrame jdbc, 
		DataFrame file)  {
		file = file.drop(ColName.MATCH_FLAG_COL);
		file.printSchema();
		file.show();
		jdbc = jdbc.select(jdbc.col(ColName.ID_COL), jdbc.col(ColName.SOURCE_COL),jdbc.col(ColName.MATCH_FLAG_COL),
			jdbc.col(ColName.CLUSTER_COLUMN));
		String[] cols = jdbc.schema().names();
		for (int i=0; i < cols.length; ++i) {
			cols[i] = ColName.COL_PREFIX + cols[i];
		}
		jdbc = jdbc.toDF(cols).cacheResult();

		jdbc = jdbc.rename(ColName.MATCH_FLAG_COL, col(ColName.COL_PREFIX + ColName.MATCH_FLAG_COL));
		jdbc.printSchema();
		jdbc.show();
		LOG.warn("Building labels ");
		DataFrame pairs = file.join(jdbc, file.col(ColName.ID_COL).equal_to(
			jdbc.col(ColName.COL_PREFIX + ColName.ID_COL))
				.and(file.col(ColName.SOURCE_COL).equal_to(
					jdbc.col(ColName.COL_PREFIX + ColName.SOURCE_COL)))
				.and(file.col(ColName.CLUSTER_COLUMN).equal_to(
					jdbc.col(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN))));
		LOG.warn("Pairs are " + pairs.count());			
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.SOURCE_COL);
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.ID_COL);
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);
		
		return pairs;
	}


	private static DataFrame readInternal(Session snow, boolean addLineNo,
			boolean addSource, Pipe... pipes) {
		DataFrame input = null;

		for (Pipe p : pipes) {
			if (input == null) {
				input = readInternal(snow, p, addSource);
				LOG.debug("input size is " + input.count());				
			} else {
					if (p.get("type") != null && p.get("type").equals("join")) {
						LOG.warn("joining inputs");
						DataFrame input1 = readInternal(snow, p, addSource);
						LOG.warn("input now size is " + input1.count());	
						input = joinTrainingSetstoGetLabels(input, input1 );
					}
					else {
						input = input.union(readInternal(snow, p, addSource));
					}				
			}
		}
		// we will probably need to create row number as string with pipename/id as
		// suffix
		if (addLineNo)
			input = DFUtil.addRowNumber(input, snow);
		// we need to transform the input here by using stop words
		return input;
	}

	public static DataFrame read(Session snow, boolean addLineNo, boolean addSource, Pipe... pipes) {
		DataFrame rows = readInternal(snow, addLineNo, addSource, pipes);
		//rows = rows.persist(StorageLevel.MEMORY_ONLY());
		return rows;
	}

	public static DataFrame sample(Session snow, Pipe p) {
		DataFrameReader reader = getReader(snow, p);
		reader.option("inferSchema", true);
		reader.option("mode", "DROPMALFORMED");
		LOG.info("reader is ready to sample with inferring " + p.get(FilePipe.LOCATION));
		LOG.warn("Reading input of type " + p.getFormat().type());
		DataFrame input = read(reader, p, false);
		// LOG.warn("inferred schema " + input.schema());
		Row[] values = input.sample(10).collect();
		//values.forEach(r -> LOG.warn(r));
		DataFrame ret = snow.createDataFrame(values, input.schema());
		return ret;

	}

	public static DataFrame read(Session snow, boolean addLineNo, int numPartitions,
			boolean addSource, Pipe... pipes) {
		DataFrame rows = readInternal(snow, addLineNo, addSource, pipes);
		//rows = rows.repartition(numPartitions);
		//rows = rows.persist(StorageLevel.MEMORY_ONLY());
		return rows;
	}

	public static void write(DataFrame toWriteOrig, Arguments args, JavaSparkContext ctx, Pipe... pipes) {
			for (Pipe p: pipes) {
			DataFrame toWrite = toWriteOrig;
			DataFrameWriter writer = toWrite.write();
		
			LOG.warn("Writing output " + p);
			
			if (p.getMode() != null) {
				writer.mode(p.getMode());
			}
			else {
				writer.mode("Append");
			}
			if (p.getFormat().equals(Format.ELASTIC)) {
				ctx.getConf().set(ElasticPipe.NODE, p.getProps().get(ElasticPipe.NODE));
				ctx.getConf().set(ElasticPipe.PORT, p.getProps().get(ElasticPipe.PORT));
				ctx.getConf().set(ElasticPipe.ID, ColName.ID_COL);
				ctx.getConf().set(ElasticPipe.RESOURCE, p.getName());
			}
			writer = writer.format(p.getFormat().type());
			
			for (String key: p.getProps().keySet()) {
				writer = writer.option(key, p.get(key));
			}
			if (p.getFormat() == Format.CASSANDRA) {
				/*
				ctx.getConf().set(CassandraPipe.HOST, p.getProps().get(CassandraPipe.HOST));
				toWrite.sparkSession().conf().set(CassandraPipe.HOST, p.getProps().get(CassandraPipe.HOST));
				//df.createCassandraTable(p.get("keyspace"), p.get("table"), opPk, opCl, CassandraConnector.apply(ctx.getConf()));
				
				CassandraConnector connector = CassandraConnector.apply(ctx.getConf());
				try (Session session = connector.openSession()) {
					ResultSet rs = session.execute("SELECT table_name FROM system_schema.tables WHERE keyspace_name='" 
								+ p.get(CassandraPipe.KEYSPACE) + "' AND table_name='" + p.get(CassandraPipe.TABLE) + "'");
					if (rs.all().size() == 0) {
						List<String> pk =  new ArrayList<String>();
						if (p.get(CassandraPipe.PRIMARY_KEY) != null) {
							//pk.add(p.get(CassandraPipe.PRIMARY_KEY));
							pk = Arrays.asList(p.get(CassandraPipe.PRIMARY_KEY).split(","));
						}
						Option<Seq<String>> opPk = Option.apply(JavaConverters.asScalaIteratorConverter(pk.iterator()).asScala().toSeq());
						List<String> cl =  new ArrayList<String>();
						
						if (p.getAddProps()!= null && p.getAddProps().containsKey("clusterBy")) {
							cl=Arrays.asList(p.getAddProps().get("clusterBy").split(","));
						}
						Option<Seq<String>> opCl = Option.apply(JavaConverters.asScalaIteratorConverter(cl.iterator()).asScala().toSeq());
						
						DataFrameFunctions df = new DataFrameFunctions(toWrite); 
						LOG.warn("received cassandra table  - " + p.get(CassandraPipe.KEYSPACE) + " and " + p.get(CassandraPipe.TABLE));
						df.createCassandraTable(p.get(CassandraPipe.KEYSPACE), p.get(CassandraPipe.TABLE), opPk, opCl, CassandraConnector.apply(ctx.getConf()));
						if (p.getAddProps()!= null && p.getAddProps().containsKey("indexBy")) {
							LOG.warn("creating index on cassandra");

							session.execute("CREATE INDEX " +  p.getAddProps().get("indexBy") + p.get(CassandraPipe.KEYSPACE) + "_" +  
									p.get(CassandraPipe.TABLE) + "_idx ON " + p.get(CassandraPipe.KEYSPACE) + "." +  
									p.get(CassandraPipe.TABLE) + "(" + p.getAddProps().get("indexBy") + 
									")");
						}
					}
					else {
						LOG.warn("existing cassandra table  - " + p.get(CassandraPipe.KEYSPACE) + " and " + p.get(CassandraPipe.TABLE));
					
					}
					
				}
				catch(Exception e) {
					e.printStackTrace();
					LOG.warn("Writing issue");
				}*/
			}
			else if (p.getProps().containsKey("location")) {
				LOG.warn("Writing file");
				writer.save(p.get(FilePipe.LOCATION));
			}	
			else if (p.getFormat().equals(Format.JDBC)){
				writer = toWrite.write();
				writer = writer.format(p.getFormat().type());				

				if (p.getMode() != null) {
					writer.mode(p.getMode());
				}
				else {
					writer.mode("Append");
				}
				for (String key: p.getProps().keySet()) {
					writer = writer.option(key, p.get(key));
				}
				writer.save();
			}
			else {			
				writer.save();
			
			}
			
			
		}

	}

	public static void writePerSource(DataFrame toWrite, Arguments args, JavaSparkContext ctx, Pipe[] pipes ) {
		List<Row> sources = Arrays.asList(toWrite.select(ColName.SOURCE_COL).distinct().collect());
		for (Row r : sources) {
			DataFrame toWriteNow = toWrite.filter(toWrite.col(ColName.SOURCE_COL).equal_to(Functions.lit(r.get(0))));
			toWriteNow = toWriteNow.drop(ColName.SOURCE_COL);
			PipeUtil.write(toWriteNow, args, ctx, pipes);

		}
	}

	public static Pipe getTrainingDataUnmarkedPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Format.PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataUnmarkedDir());
		return p;
	}

	public static Pipe getTrainingDataMarkedPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Format.PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataMarkedDir());
		return p;
	}
	
	public static Pipe getModelDocumentationPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Format.TEXT);
		p.setProp(FilePipe.LOCATION, args.getZinggDocFile());
		return p;
	}
	
	public static Pipe getBlockingTreePipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Format.PARQUET);
		p.setProp(FilePipe.LOCATION, args.getBlockFile());
		p.setMode(SaveMode.Overwrite);
		return p;
	}

	public static String getPipesAsString(Pipe[] pipes) {
		return Arrays.stream(pipes)
			.map(p -> p.getFormat().type())
			.collect(Collectors.toList())
			.stream().reduce((p1, p2) -> p1 + "," + p2)
			.map(Object::toString)
			.orElse("");
	}

	/*
	 * public static String getTableCreateCQL(Pipe p, DataFrame df) {
	 * 
	 * Set<String> partitionKeys = new TreeSet<String>() { {
	 * add(p.get(CassandraPipe.PRIMARY_KEY)); } }; int c = 0; Map<String, Integer>
	 * clustereingKeys = new TreeMap<String, Integer>(); if (p.getAddProps()!= null
	 * && p.getAddProps().containsKey("clusterBy")) { for (String clBy :
	 * p.getAddProps().get("clusterBy").split(",")) { { clustereingKeys.put(clBy,
	 * c++); } } }
	 * 
	 * TableDef td = TableDef.fromDataFrame(df, p.get(CassandraPipe.KEYSPACE),
	 * p.get(CassandraPipe.TABLE), ProtocolVersion.NEWEST_SUPPORTED);
	 * 
	 * List<ColumnDef> partKeyList = new ArrayList<ColumnDef>(); List<ColumnDef>
	 * clusterColumnList = new ArrayList<ColumnDef>(); List<ColumnDef>
	 * regColulmnList = new ArrayList<ColumnDef>();
	 * 
	 * scala.collection.Iterator<ColumnDef> iter = td.allColumns() .iterator();
	 * while (iter.hasNext()) { ColumnDef col = iter.next(); String colName =
	 * col.columnName(); if (partitionKeys.contains(colName)) { partKeyList.add(new
	 * ColumnDef(colName, PartitionKeyColumn$.MODULE$, col.columnType())); } else if
	 * (clustereingKeys.containsKey(colName)) { int idx =
	 * clustereingKeys.get(colName); clusterColumnList.add(new ColumnDef(colName,
	 * new ClusteringColumn(idx), col.columnType())); } else { if
	 * (colName.equals("dob")) { LOG.warn("yes dob"); regColulmnList.add(new
	 * ColumnDef(colName, new StaticColumn$.MODULE$, col.columnType())); } else {
	 * regColulmnList.add(new ColumnDef(colName, RegularColumn$.MODULE$,
	 * col.columnType())); } } }
	 * 
	 * TableDef newTd = new TableDef(td.keyspaceName(), td.tableName(),
	 * JavaConverters.asScalaIteratorConverter(partKeyList.iterator()).asScala().
	 * toSeq(),
	 * JavaConverters.asScalaIteratorConverter(clusterColumnList.iterator()).asScala
	 * ().toSeq() ,
	 * JavaConverters.asScalaIteratorConverter(regColulmnList.iterator()).asScala().
	 * toSeq(), td.indexes(), td.isView()); String cql = newTd.cql();
	 * System.out.println(cql); System.out.println(cql.replace(",dob", newChar));
	 * 
	 * return cql;
	 * 
	 * }
	 */

}
