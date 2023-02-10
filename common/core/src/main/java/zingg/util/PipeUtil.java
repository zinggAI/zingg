package zingg.util;

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

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZFrame;
import zingg.client.util.ColName;
import zingg.client.pipe.FilePipe;
//import zingg.client.pipe.InMemoryPipe;
import zingg.client.pipe.Pipe;
import scala.Option;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import zingg.util.PipeUtilBase;

//import com.datastax.spark.connector.cql.*;
//import org.elasticsearch.spark.sql.api.java.JavaEsSparkSQL;
//import zingg.scala.DFUtil;

public abstract class PipeUtil<S,D,R,C> implements PipeUtilBase<S,D,R,C>{

	protected S session;

	public  final Log LOG = LogFactory.getLog(PipeUtil.class);

	public PipeUtil(S spark) {
		this.session = spark;
	}
	
	public S getSession(){
		return this.session;
	}

	public void setSession(S session){
		this.session = session;
	}

	//public abstract DFReader<S,D,R,C> getReader();//getSession().read()

	public DFReader<D,R,C> getReader(Pipe<D,R,C> p) {
		DFReader<D,R,C> reader = getReader();
		reader = reader.format(p.getFormat());
		if (p.getSchema() != null) {
			reader = reader.setSchema(p.getSchema()); //.schema(StructType.fromDDL(p.getSchema()));
		}
		
		for (String key : p.getProps().keySet()) {
			reader = reader.option(key, p.get(key));
		}
		reader = reader.option("mode", "PERMISSIVE");
		return reader;
	}

	private  ZFrame<D,R,C> read(DFReader<D,R,C> reader, Pipe<D,R,C> p, boolean addSource) throws ZinggClientException{
		ZFrame<D,R,C> input = null;
		LOG.warn("Reading " + p);
		try {

		if (p.getFormat() == Pipe.FORMAT_INMEMORY) {
			input = p.getDataset(); //.df();
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
				input = input.withColumn(ColName.SOURCE_COL, p.getName());
			}
			p.setDataset(input);
		} catch (Exception ex) {
			LOG.warn(ex.getMessage());
			throw new ZinggClientException("Could not read data.", ex);
		}
		return getZFrame(input); //new SparkFrame(input);
	}

	public  ZFrame<D,R,C> readInternal(Pipe<D,R,C> p, 
		boolean addSource) throws ZinggClientException {
		DFReader<D,R,C> reader = getReader(p);
		return read(reader, p, addSource);		
	}

	/*
	public ZFrame<D,R,C> joinTrainingSetstoGetLabels(ZFrame<D,R,C> jdbc, 
	ZFrame<D,R,C> file)  {
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

	public ZFrame<D,R,C> readInternal(boolean addLineNo,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> input = null;

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
			input = addLineNo(input); //new SparkFrame(new SparkDSUtil(getSession()).addRowNumber(input).df());
		// we need to transform the input here by using stop words
		return input;
	}

	

	public ZFrame<D,R,C> read(boolean addLineNo, boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> rows = readInternal(addLineNo, addSource, pipes);
		rows = rows.cache();
		return rows;
	}

	/*
	public ZFrame<D,R,C> sample(S spark, Pipe p) throws ZinggClientException {
		DataFrameReader reader = getReader(spark, p);
		reader.option("inferSchema", true);
		reader.option("mode", "DROPMALFORMED");
		LOG.info("reader is ready to sample with inferring " + p.get(FilePipe.LOCATION));
		LOG.warn("Reading input of type " + p.getFormat());
		ZFrame<D,R,C> input = read(reader, p, false);
		// LOG.warn("inferred schema " + input.schema());
		List<Row> values = input.takeAsList(10);
		values.forEach(r -> LOG.warn(r));
		Dataset<Row> ret = spark.createDataFrame(values, input.schema());
		return ret;
	}
	*/

	public  ZFrame<D,R,C> read(boolean addLineNo, int numPartitions,
			boolean addSource, Pipe<D,R,C>... pipes) throws ZinggClientException {
		ZFrame<D,R,C> rows = readInternal(addLineNo, addSource, pipes);
		rows = rows.repartition(numPartitions);
		rows = rows.cache();
		return rows;
	}

	public  void write(ZFrame<D,R,C> toWriteOrig, Arguments args, 
		Pipe<D,R,C>... pipes) throws ZinggClientException {
		try {
			for (Pipe<D,R,C> p: pipes) {
			//Dataset<Row> toWrite = toWriteOrig.df();
			//DataFrameWriter writer = toWrite.write();
			DFWriter writer = getWriter(toWriteOrig);
		
			LOG.warn("Writing output " + p);
			
			if (p.getFormat() == Pipe.FORMAT_INMEMORY) {
 				p.setDataset(toWriteOrig);
				return;
			}
			//SparkPipe sPipe = (SparkPipe) p;
			if (p.getMode() != null) {
				writer.setMode(p.getMode()); //SaveMode.valueOf(p.getMode()));
			}
			else {
				writer.setMode("Append"); //SaveMode.valueOf("Append"));
			}
			/* 
			if (p.getFormat().equals(Pipe.FORMAT_ELASTIC)) {
				ctx.getConf().set(ElasticPipe.NODE, p.getProps().get(ElasticPipe.NODE));
				ctx.getConf().set(ElasticPipe.PORT, p.getProps().get(ElasticPipe.PORT));
				ctx.getConf().set(ElasticPipe.ID, ColName.ID_COL);
				ctx.getConf().set(ElasticPipe.RESOURCE, p.getName());
			}
			*/
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
				writer = getWriter(toWriteOrig);
				writer = writer.format(p.getFormat());				

				//SparkPipe sPipe = (SparkPipe) p;
				if (p.getMode() != null) {
					writer.setMode(p.getMode()); //SaveMode.valueOf(p.getMode()));
				}
				else {
					writer.setMode("Append") ;//SaveMode.valueOf("Append"));
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

	public Pipe<D,R,C> getTrainingDataUnmarkedPipe(Arguments args) {
		Pipe<D,R,C> p = new Pipe<D,R,C>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataUnmarkedDir());
		return p;
	}

	public  Pipe<D,R,C> getTrainingDataMarkedPipe(Arguments args) {
		Pipe<D,R,C> p = new Pipe<D,R,C>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getZinggTrainingDataMarkedDir());
		return p;
	}
	
	public  Pipe<D,R,C> getModelDocumentationPipe(Arguments args) {
		Pipe<D,R,C> p = new Pipe<D,R,C>();
		p.setFormat(Pipe.FORMAT_TEXT);
		p.setProp(FilePipe.LOCATION, args.getZinggModelDocFile());
		return p;
	}

	
	
	
	public Pipe<D,R,C> getStopWordsPipe(Arguments args, String fileName) {
		Pipe p = new Pipe<D,R,C>();
		p.setFormat(Pipe.FORMAT_CSV);
		p.setProp(FilePipe.HEADER, "true");
		p.setProp(FilePipe.LOCATION, fileName);
		//p.setMode(SaveMode.Overwrite.toString());
		p = setOverwriteMode(p);
		return p;
	}

	public  Pipe<D,R,C> getBlockingTreePipe(Arguments args) {
		Pipe p = new Pipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, args.getBlockFile());
		//p.setMode(SaveMode.Overwrite.toString());
		
		p = setOverwriteMode(p);
		return p;
	}

	
	@Override
	public String getPipesAsString(Pipe<D,R,C>[] pipes) {
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