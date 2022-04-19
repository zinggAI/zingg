package zingg.preprocess;

import static org.apache.spark.sql.functions.udf;
import static org.apache.spark.sql.functions.col;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;

import scala.collection.JavaConverters;
import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.util.ColName;
import zingg.util.PipeUtil;

public class StopWords {

	protected static String name = "zingg.preprocess.StopWords";
	public static final Log LOG = LogFactory.getLog(StopWords.class); 

    public static Dataset<Row> preprocessForStopWords(SparkSession spark, Arguments args, Dataset<Row> ds) {

		List<String> wordList = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				Dataset<Row> stopWords = PipeUtil.read(spark, false, false, PipeUtil.getStopWordsPipe(args, def.getStopWords()));
				wordList = stopWords.select("_c0").collectAsList().stream().map(f -> f.getString(0)).collect(Collectors.toList());
				ds = ds.withColumn(def.getFieldName(), removeStopWords(wordList).apply(ds.col(def.getFieldName())));
			}
		}

		return ds;
	}

	public static UserDefinedFunction removeStopWords(List<String> stopWords) {
		return udf((String s) -> { 
			if (s == null) return null;
			ArrayList<String> allWords = Stream.of(s.split(" "))
						.collect(Collectors.toCollection(ArrayList<String>::new));
						allWords.removeAll(stopWords);
			return allWords.stream().collect(Collectors.joining(" "));
			}, DataTypes.StringType);
	}

    public static Dataset<Row> postprocess(Dataset<Row> actual, Dataset<Row> orig) {
		List<Column> cols = new ArrayList<Column>();	
		cols.add(actual.col(ColName.CLUSTER_COLUMN));
		cols.add(actual.col(ColName.ID_COL));
		cols.add(actual.col(ColName.PREDICTION_COL));
		cols.add(actual.col(ColName.SCORE_COL));
		cols.add(col(ColName.MATCH_FLAG_COL));

		Dataset<Row> zFieldsFromActual = actual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
		
		Dataset<Row> joined = zFieldsFromActual.join(orig, ColName.ID_COL);

		return joined;
	}

    public static Dataset<Row> postprocessLinked(Dataset<Row> actual, Dataset<Row> orig) {
		List<Column> cols = new ArrayList<Column>();
        cols.add(actual.col(ColName.CLUSTER_COLUMN));	
		cols.add(actual.col(ColName.ID_COL));
 		cols.add(actual.col(ColName.SCORE_COL));
 		cols.add(actual.col(ColName.SOURCE_COL));	

		Dataset<Row> zFieldsFromActual = actual.select(JavaConverters.asScalaIteratorConverter(cols.iterator()).asScala().toSeq());
		Dataset<Row> joined = zFieldsFromActual.join(orig,
				zFieldsFromActual.col(ColName.ID_COL).equalTo(orig.col(ColName.ID_COL))
						.and(zFieldsFromActual.col(ColName.SOURCE_COL).equalTo(orig.col(ColName.SOURCE_COL))))
						.drop(zFieldsFromActual.col(ColName.SOURCE_COL))
						.drop(zFieldsFromActual.col(ColName.ID_COL))
						.drop(orig.col(ColName.ID_COL));

		return joined;
	}
}