package zingg.sink;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import zingg.client.Arguments;
import zingg.client.pipe.Pipe;
import zingg.client.util.Util;
import scala.Tuple2;

import com.google.common.collect.ImmutableMap;

public class Sinker {
	
	public static final Log LOG = LogFactory.getLog(Sinker.class);
	

	
	public static void write(Pipe sink, JavaPairRDD<Long, Tuple2<Long, String>> records, Arguments args) {
		/*if (sink.getSinkType().equals("elastic")) {
			writeToES(sink, records, args);
		}
		else if (sink.getSinkType().equals("jdbc")) {
			writeToJDBC(sink, records, args);
		}*/		
	}
	
	public static void writeToES(Pipe sink, JavaPairRDD<Long, Tuple2<Long, String>> records, Arguments args) {
		
		/*
		 * //turn records into json
		 
				final String header = sink.getProps().get("header");
				LOG.info("header is " + header);
				final String idField = sink.getProps().get("id");
				LOG.info("id is " + idField);
				JavaRDD<ImmutableMap<String, String>> pretty = records.map(new Function<Tuple2<Long, Tuple2<Long, String>>, ImmutableMap<String, String>>() {
					@Override
					public ImmutableMap<String, String> call(
							Tuple2<Long, Tuple2<Long, String>> t) {
						return Sinker.prettyPrintTupleToJson(idField, header, t._2()._1(), t._2()._2(), args);
					}
				});
				//call es
				 * 
				 */
		
	}
	
	
	public static void writeToJDBC(Pipe sink, JavaPairRDD<Long, Tuple2<Long, String>> records, Arguments args) {
		/*
				//turn records into json
				final String header = sink.getProps().get("header");
				LOG.info("header is " + header);
				final String idField = sink.getProps().get("id");
				LOG.info("id is " + idField);
				JavaRDD<TableOutput> pretty = records.map(new Function<Tuple2<Long, Tuple2<Long, String>>, TableOutput>() {
					@Override
					public TableOutput call(
							Tuple2<Long, Tuple2<Long, String>> t) {
						return new TableOutput(0, System.currentTimeMillis(), t._2()._1, t._2()._2());
					}
				});
				//Dataset<Row> rows = ((SparkContext) pretty.context()).createDataFrame(pretty, TableOutput.class);
			*/	
				
		
	}
	
	public static ImmutableMap<String, String> prettyPrintTupleToJson(String idField, String header, Long clusterId, String tuple, Arguments args) {
		String[] head = header.split(",");
		List<String> values = Util.parse(tuple,",");
		
		ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<String, String>();
		
		//ImmutableMap<String, String> map = new ImmutableMap.Builder<String, String>().build();
		builder.put("CLUSTER_ID", clusterId.toString());
		
		int col = 0;
		for (String h: head) {
			builder.put(h, values.get(col++));
		}
		return builder.build();
		
	}

	
}
