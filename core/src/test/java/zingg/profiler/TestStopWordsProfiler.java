package zingg.profiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import zingg.ZinggSparkTester;

public class TestStopWordsProfiler extends ZinggSparkTester {
	private static final int NO_OF_RECORDS = 5;
	private static final String COL_STOPWORDS = "stopwords";
	public static final Log LOG = LogFactory.getLog(TestStopWordsProfiler.class);
	Dataset<Row> dataset;

	@Test
	public void testGeneratedStopWordsAndTheirCount() throws Throwable {
		StopWordsProfiler profile = new StopWordsProfiler(spark, args);

		Dataset<Row> dataset = createDFWithGivenStopWords();

		args.setStopWordsCutoff(0.1f);
		Dataset<Row> stopWords = profile.findStopWords(dataset, COL_STOPWORDS);
		stopWords.show();
	}
	/* creates a dataframe for given words and their frequency*/
	public Dataset<Row> createDFWithGivenStopWords() {
		Map<String, Integer> map = Stream.of(new Object[][] {
				{ "the", 44 },
				{ "of", 27 },
				{ "was", 11 },
				{ "in", 11 },
				{ "to", 9 },
				{ "passengers", 2 },
				{ "carried", 2 },
				{ "people", 1 },
				{ "iceberg", 1 }
		}).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));

		//create a list of list. Each record shall contain a list of stopwords
		ArrayList<List<String>> strList = new ArrayList<>();
		for (int index = 0; index < NO_OF_RECORDS; index++) {
			strList.add(new ArrayList<String>());
		}
		//add stopwords as per calulcated random distribution to each record
		for (String key : map.keySet()) {
			int arr[] = randomDistributionList(NO_OF_RECORDS, map.get(key));
			for (int index = 0; index < NO_OF_RECORDS; index++) {
				List<String> str = new ArrayList<String>();
				str.addAll(Collections.nCopies(arr[index], key));
				strList.get(index).addAll(str);
			}
		}
		List<Row> records = new ArrayList<Row>();
		for (int index = 0; index < NO_OF_RECORDS; index++) {
			records.add(RowFactory.create(getStringFromList(strList.get(index))));
		}
		//schema definition
		StructType structType = new StructType();
		structType = structType.add(DataTypes.createStructField(COL_STOPWORDS, DataTypes.StringType, false));
		//create dataframe with given records and schema
		Dataset<Row> doublesDF = spark.createDataFrame(records, structType);
		return doublesDF;
	}
	/* join list elements */
	public static String getStringFromList(List<String> strs) {
		return strs.stream().reduce((p1, p2) -> p1 + " " + p2)
				.map(Object::toString)
				.orElse("");
	}
	/* Breaks 'n' into 'm' random numbers such that sum(arr[m]) = n */
	int[] randomDistributionList(int m, int n) {
		int arr[] = new int[m];
		for (int i = 0; i < n; i++) {
			arr[(int) (Math.random() * m)]++;
		}
		return arr;
	}
}
