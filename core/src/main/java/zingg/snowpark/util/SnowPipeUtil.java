package zingg.snowpark.util;

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
import org.apache.spark.api.java.JavaSparkContext;
import com.snowflake.snowpark_java.Column;
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
import zingg.client.SnowFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZFrame;
import zingg.client.util.ColName;
import zingg.client.pipe.CassandraPipe;
import zingg.client.pipe.ElasticPipe;
import zingg.client.pipe.FilePipe;
import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

//import com.datastax.driver.core.ProtocolVersion;
//import com.datastax.driver.core.ResultSet;
//import com.datastax.driver.core.Session;
//import com.datastax.spark.connector.DataFrameFunctions;
// import com.datastax.spark.connector.cql.CassandraConnector;
// import com.datastax.spark.connector.cql.ClusteringColumn;
// import com.datastax.spark.connector.cql.ColumnDef;
// import com.datastax.spark.connector.cql.TableDef;

// import com.datastax.spark.connector.cql.*;
import zingg.scala.DFUtil;
import zingg.util.PipeUtilBase;

//import com.datastax.spark.connector.cql.*;
//import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
//import zingg.scala.DFUtil;

public class SnowPipeUtil implements PipeUtilBase<Session, DataFrame, Row, Column>{

	Session snowSession;

	public  final Log LOG = LogFactory.getLog(SnowPipeUtil.class);

	public SnowPipeUtil(Session snow) {
		this.snowSession = snow;
	}
	

	public void setSession(Session session){
		this.snowSession = session;
	}

	private DataFrameReader getReader(Pipe p) {
		DataFrameReader reader = getSession().read();

		LOG.warn("Reading input " + p.getFormat());
		reader = reader.format(p.getFormat());
		if (p.getSchema() != null) {
			reader = reader.schema(p.getSchema());
		}
		for (String key : p.getProps().keySet()) {
			reader = reader.option(key, p.get(key));
		}
		reader = reader.option("mode", "PERMISSIVE");
		return reader;
	}

	private  SnowFrame read(DataFrameReader reader, Pipe p, boolean addSource) throws ZinggClientException{
		DataFrame input = null;
		LOG.warn("Reading " + p);
		try {

		if (p.getFormat() == Pipe.FORMAT_INMEMORY) {
			input = ((InMemoryPipe) p).getRecords();
		}
		else {		
			if (p.getProps().containsKey(FilePipe.LOCATION)) {
				input = reader.load(p.get(FilePipe.LOCATION));
			}
			else {
				input = reader.load();
			}
    }
			if (addSource) {
				input = input.withColumn(ColName.SOURCE_COL, Functions.lit(p.getName()));
			}
		} catch (Exception ex) {
			LOG.warn(ex.getMessage());
			throw new ZinggClientException("Could not read data.", ex);
		}
		return new SnowFrame(input);
	}

	private  ZFrame<DataFrame, Row, Column> readInternal(Session snow, Pipe p, boolean addSource) throws ZinggClientException {
		DataFrameReader reader = getReader(snow, p);
		return read(reader, p, addSource);		
	}

	/*
	public ZFrame<Dataset<Row>, Row, Column> joinTrainingSetstoGetLabels(ZFrame<Dataset<Row>, Row, Column> jdbc, 
	ZFrame<Dataset<Row>, Row, Column> file)  {
		file = file.drop(ColName.MATCH_FLAG_COL);
		file.printSchema();
		file.show();
		jdbc = jdbc.select(jdbc.col(ColName.ID_COL), jdbc.col(ColName.SOURCE_COL),jdbc.col(ColName.MATCH_FLAG_COL),
			jdbc.col(ColName.CLUSTER_COLUMN));
		String[] cols = jdbc.columns();
		for (int i=0; i < cols.length; ++i) {
			cols[i] = ColName.COL_PREFIX + cols[i];
		}
		jdbc = jdbc.toDF(cols).cache();

		jdbc = jdbc.withColumnRenamed(ColName.COL_PREFIX + ColName.MATCH_FLAG_COL, ColName.MATCH_FLAG_COL);
		jdbc.printSchema();
		jdbc.show();
		LOG.warn("Building labels ");
		Dataset<Row> pairs = file.join(jdbc, file.col(ColName.ID_COL).equalTo(
			jdbc.col(ColName.COL_PREFIX + ColName.ID_COL))
				.and(file.col(ColName.SOURCE_COL).equalTo(
					jdbc.col(ColName.COL_PREFIX + ColName.SOURCE_COL)))
				.and(file.col(ColName.CLUSTER_COLUMN).equalTo(
					jdbc.col(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN))));
		LOG.warn("Pairs are " + pairs.count());			
		//in training, we only need that record matches only with lines bigger than itself
		//in the case of normal as well as in the case of linking
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.SOURCE_COL);
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.ID_COL);
		pairs = pairs.drop(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);
		
		return pairs;
	}
	*/

	private ZFrame<DataFrame, Row, Column> readInternal(Session snow, boolean addLineNo,
			boolean addSource, Pipe... pipes) throws ZinggClientException {
		ZFrame<DataFrame, Row, Column> input = null;

		for (Pipe p : pipes) {
			if (input == null) {
				input = readInternal(p, addSource);
				LOG.debug("input size is " + input.count());				
			} else {
					input = input.union(readInternal(p, addSource));
			}
		}
		// we will probably need to create row number as string with pipename/id as
		// suffix
		if (addLineNo)
			input = new SnowFrame(getDFUtil().addRowNumber(input.df(), getSession()));
		// we need to transform the input here by using stop words
		return input;
	}

	public ZFrame<DataFrame, Row, Column> read(Session snow, boolean addLineNo, boolean addSource, Pipe... pipes) throws ZinggClientException {
		ZFrame<DataFrame, Row, Column> rows = readInternal(snow, addLineNo, addSource, pipes);
		rows = rows.cache();
		return rows;
	}

	/*
	public ZFrame<Dataset<Row>, Row, Column> sample(SparkSession spark, Pipe p) throws ZinggClientException {
		DataFrameReader reader = getReader(spark, p);
		reader.option("inferSchema", true);
		reader.option("mode", "DROPMALFORMED");
		LOG.info("reader is ready to sample with inferring " + p.get(FilePipe.LOCATION));
		LOG.warn("Reading input of type " + p.getFormat());
		ZFrame<Dataset<Row>, Row, Column> input = read(reader, p, false);
		// LOG.warn("inferred schema " + input.schema());
		List<Row> values = input.takeAsList(10);
		values.forEach(r -> LOG.warn(r));
		Dataset<Row> ret = spark.createDataFrame(values, input.schema());
		return ret;
	}
	*/

	public  ZFrame<DataFrame, Row, Column> read(Session snow, boolean addLineNo, int numPartitions,
			boolean addSource, Pipe... pipes) throws ZinggClientException {
		ZFrame<DataFrame, Row, Column> rows = readInternal(snow, addLineNo, addSource, pipes);
		rows = rows.repartition(numPartitions);
		rows = rows.cache();
		return rows;
	}

	public  void write(ZFrame<DataFrame, Row, Column> toWriteOrig, Arguments args, JavaSparkContext ctx, Pipe... pipes) throws ZinggClientException {
		try {
			for (Pipe p: pipes) {
			DataFrame toWrite = toWriteOrig.df();
			DataFrameWriter writer = toWrite.write();
		
			LOG.warn("Writing output " + p);
			
			if (p.getFormat() == Pipe.FORMAT_INMEMORY) {
 				p.setDataset(toWriteOrig);
				return;
			}

			if (p.getMode() != null) {
				writer.mode(p.getMode());
			}
			else {
				writer.mode("Append");
			}
			if (p.getFormat().equals(Pipe.FORMAT_ELASTIC)) {
				ctx.getConf().set(ElasticPipe.NODE, p.getProps().get(ElasticPipe.NODE));
				ctx.getConf().set(ElasticPipe.PORT, p.getProps().get(ElasticPipe.PORT));
				ctx.getConf().set(ElasticPipe.ID, ColName.ID_COL);
				ctx.getConf().set(ElasticPipe.RESOURCE, p.getName());
			}
			writer = writer.format(p.getFormat());
			
			for (String key: p.getProps().keySet()) {
				writer = writer.option(key, p.get(key));
			}
			if (p.getFormat() == Pipe.FORMAT_CASSANDRA) {
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
			else if (p.getFormat().equals(Pipe.FORMAT_JDBC)){
				writer = toWrite.write();
				writer = writer.format(p.getFormat());				

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
		} catch (Exception ex) {
			throw new ZinggClientException(ex.getMessage());
		}
	}

	/*
	public  void writePerSource(Dataset<Row> toWrite, Arguments args, JavaSparkContext ctx, Pipe[] pipes ) throws ZinggClientException {
		List<Row> sources = toWrite.select(ColName.SOURCE_COL).distinct().collectAsList();
		for (Row r : sources) {
			Dataset<Row> toWriteNow = toWrite.filter(toWrite.col(ColName.SOURCE_COL).equalTo(r.get(0)));
			toWriteNow = toWriteNow.drop(ColName.SOURCE_COL);
			write(toWriteNow, args, ctx, pipes);

		}
	}
	*/

	public Pipe getTrainingDataUnmarkedPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataUnmarkedDir());
		return p;
	}

	public  Pipe getTrainingDataMarkedPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataMarkedDir());
		return p;
	}
	
	public  Pipe getModelDocumentationPipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_TEXT);
		p.setProp(FilePipe.LOCATION, args.getZinggModelDocFile());
		return p;
	}
	
	public Pipe getStopWordsPipe(Arguments args, String fileName) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_CSV);
		p.setProp(FilePipe.HEADER, "true");
		p.setProp(FilePipe.LOCATION, fileName);
		p.setMode(SaveMode.Overwrite);
		return p;
	}

	public  Pipe getBlockingTreePipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getBlockFile());
		p.setMode(SaveMode.Overwrite);
		return p;
	}

	public  String getPipesAsString(Pipe[] pipes) {
		return Arrays.stream(pipes)
			.map(p -> p.getFormat())
			.collect(Collectors.toList())
			.stream().reduce((p1, p2) -> p1 + "," + p2)
			.map(Object::toString)
			.orElse("");
	}


	/*
	 * public  String getTableCreateCQL(Pipe p, Dataset<Row> df) {
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