package zingg.preprocess;

import static org.apache.spark.sql.functions.udf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.UserDefinedFunction;
import org.apache.spark.sql.types.DataTypes;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.util.PipeUtil;

public class StopWords {

	protected static String name = "zingg.preprocess.StopWords";
	public static final Log LOG = LogFactory.getLog(StopWords.class);
	protected static String stopWordColumn = ColName.COL_WORD;
	protected static final int COLUMN_INDEX_DEFAULT = 0;

    public static Dataset<Row> preprocessForStopWords(SparkSession spark, Arguments args, Dataset<Row> ds) throws ZinggClientException {

		List<String> wordList = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				Dataset<Row> stopWords = PipeUtil.read(spark, false, false, PipeUtil.getStopWordsPipe(args, def.getStopWords()));
				if (!Arrays.asList(stopWords.schema().fieldNames()).contains(stopWordColumn)) {
					stopWordColumn = stopWords.columns()[COLUMN_INDEX_DEFAULT];
				}
				wordList = stopWords.select(stopWordColumn).as(Encoders.STRING()).collectAsList();
				String pattern = wordList.stream().collect(Collectors.joining("|", "\\b(", ")\\b\\s?"));
				ds = ds.withColumn(def.getFieldName(), removeStopWords(pattern.toLowerCase()).apply(ds.col(def.getFieldName())));
			}
		}

		return ds;
	}
	
	public static UserDefinedFunction removeStopWords(String stopWordsRegexString) {
		return udf((String s) -> {
				if (s == null) return null;
				return s.toLowerCase().replaceAll(stopWordsRegexString, "");
			}, DataTypes.StringType);
	}
}