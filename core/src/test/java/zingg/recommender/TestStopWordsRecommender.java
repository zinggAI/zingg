package zingg.recommender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import zingg.ZinggSparkTester;
import zingg.client.ZinggClientException;

public class TestStopWordsRecommender extends ZinggSparkTester {

	private static final int NO_OF_RECORDS = 5;
	private static final String COL_STOPWORDS = "stopwords";
	public static final Log LOG = LogFactory.getLog(TestStopWordsRecommender.class);

	StopWordsRecommender recommender = new StopWordsRecommender(spark, ctx, args);
	Dataset<Row> dataset = createDFWithGivenStopWords();
	List<Row> stopwordRow= new ArrayList<>();
	List<String> stopwordList = new ArrayList<String>();
	Dataset<Row> stopWords;

	@Test
    public void testWithNegativefCuttoff() throws Throwable{
        try {
			LOG.info("Test with stopCutoff = -1");
			stopwordList = getStopWordList(-1.0f);
            fail("Exception should not have been thrown when stopCutoff is negative");
        }
        catch(ZinggClientException e) {
        }

    }

	@Test
	public void testWithCuttoffOne() throws Throwable {
		LOG.info("Test with stopCutoff = 1");
		stopwordList = getStopWordList(1.0f);
		// cutoff 1 implies that whole dataset contain only one word, hence expected no. of stopword should be 0
		assertEquals(0, stopwordList.size());
	}

	@Test
	public void testWithHalfCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.5");
		stopwordList = getStopWordList(0.5f);
		// for cutoff = 0.5, expected no. of stopword should have frequecy greater than 
		// or equal to half of the total words(here, 108/2 = 54) which should be 0
		assertEquals(0, stopwordList.size());
	}

	@Test
	public void testWithPointFourCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.4");
		stopwordList = getStopWordList(0.4f);
		// for cutoff = 0.4, expected no. of stopword should have frequecy greater than 
		// or equal to totalwords*cutoff(here, 108*0.4 = 43.2) which should be 1 ("the","44")
		assertEquals(1, stopwordList.size());
		assertEquals("the", stopwordList.get(0));
	}
	
	@Test
	public void testWithPointThreeCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.3");
		stopwordList = getStopWordList(0.3f);
		// for cutoff = 0.3, expected no. of stopword should have frequecy greater than 
		// or equal to totalwords*cutoff(here, 108*0.3 = 32.4) which should be 1 ("the","44")
		assertEquals(1, stopwordList.size());
		assertEquals("the", stopwordList.get(0));
	}

	@Test
	public void testWithPointTwoCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.2");
		stopwordList = getStopWordList(0.2f);
		// for cutoff = 0.4, expected no. of stopword should have frequecy greater than 
		// or equal to totalwords*cutoff(here, 108*0.4 = 21.6) which should be 2 ("the","44") and ("of","27")
		assertEquals(2, stopwordList.size());
		assertTrue(validTestWithPointTwoCuttoff(stopwordList));
	}

	@Test
	public void testWithOneTenthCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.1");
		stopwordList = getStopWordList(0.1f);
		// for cutoff = 0.4, expected no. of stopword should have frequecy greater than or equal to
		// totalwords*cutoff(here, 108*0.4 = 21.6) which should be 4 ("the","44"), ("of","27"), ("in","11") and ("was","11")
		assertEquals(4, stopwordList.size());
		assertTrue(validTestWithOneTenthCuttoff(stopwordList));
	}

	@Test
	public void testWithZeroCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0");
		stopwordList = getStopWordList(0.0f);
		// zero cutoff selects all words as stopwords, hence hence expected no. of stopword should be 9
		assertEquals(9, stopwordList.size());
	}

	/* creates list of string countaining recommended stopwords based on stopWordsCutoff */
	public List<String> getStopWordList(float cutoff ) throws Throwable {
		//create stopwords dataset and display according to stopWordsCutoff
		args.setStopWordsCutoff(cutoff);
		stopWords = recommender.findStopWords(dataset, COL_STOPWORDS);
		stopWords.show();

		//convert spark Dataset<Row> into java List<Row>
		stopwordRow = stopWords.collectAsList();

		//convert list of Row containing word,count to list of String containing only stopword
		List<String> stopwordsList = new ArrayList<String>();
		for(Row r: stopwordRow){
			stopwordsList.add(r.getString(0));
		}
		return stopwordsList;
	}

	/* method to check if "the" and "of" are recommended for stopWordsCutoff = 0.2 */
	public Boolean validTestWithPointTwoCuttoff(List<String> stopwordList){
		List<String> arr = new ArrayList<String>();
		arr.add("the");
		arr.add("of");
		for(String r: stopwordList){
			if(!arr.contains(r)){
				return false;
			}
		}
		return true;
	}
	/* method to check if "in","the","was" and "of" are recommended for stopWordsCutoff = 0.1 */
	public Boolean validTestWithOneTenthCuttoff(List<String> stopwordList){
		List<String> arr = new ArrayList<String>();
		arr.add("the");
		arr.add("of");
		arr.add("in");
		arr.add("was");
		for(String r: stopwordList){
			if(!arr.contains(r)){
				return false;
			}
		}
		return true;
	}

	/* creates a dataframe for given words and their frequency*/
	public Dataset<Row> createDFWithGivenStopWords() {
		//Initialize WordByCount list and add all the (word,count) one by one.
		List<WordByCount> wordCount=new ArrayList<WordByCount>();
		wordCount.add(new WordByCount("the", 44));
		wordCount.add(new WordByCount("of", 27));
		wordCount.add(new WordByCount("was", 11));
		wordCount.add(new WordByCount("in", 11));
		wordCount.add(new WordByCount("to", 9));
		wordCount.add(new WordByCount("passengers", 2));
		wordCount.add(new WordByCount("carried", 2));
		wordCount.add(new WordByCount("people", 1));
		wordCount.add(new WordByCount("iceberg", 1));

		//fill wordDistribution per word in the list of words where row is NO_OF_RECORDS, each column represents a word 
		int[][] wordDistribution=randomDistributionList(NO_OF_RECORDS,wordCount);

		//add stopwords as per calulcated random distribution to each record
		List<Row> records = new ArrayList<Row>();
		for (int index = 0; index < NO_OF_RECORDS; index++) {
			List<String> strList = new ArrayList<String>();
			for(WordByCount wc: wordCount){
				strList.addAll(Collections.nCopies(wordDistribution[index][wordCount.indexOf(wc)],wc.word));
			}
			records.add(RowFactory.create(getStringFromList(strList)));
		}
		//schema definition
		StructType structType = new StructType();
		structType = structType.add(DataTypes.createStructField(COL_STOPWORDS, DataTypes.StringType, false));
		//create dataframe with given records and schema
		Dataset<Row> recordDF = spark.createDataFrame(records, structType);
		return recordDF;
	}
	/* WordByCount class containing word,count variable*/
	class WordByCount{
		String word;
		Integer count;
		public WordByCount(String word, Integer count){
			this.word = word;
			this.count = count;
		}
	}

	/* join list elements */
	public static String getStringFromList(List<String> strs) {
		return strs.stream().reduce((p1, p2) -> p1 + " " + p2)
				.map(Object::toString)
				.orElse("");
	}
	/* Breaks WordByCount object's 'count' Variable into 'm' random numbers such that sum(arr[m]) = count */
	public static int[][] randomDistributionList(int m, List<WordByCount> wbc) {
		int[][] arr=new int[m][wbc.size()];
		for (WordByCount c:wbc) {
            for(int i = 0; i < c.count; i++){
				arr[(int) (Math.random() * m)][wbc.indexOf(c)]++;
            }
		}
		return arr;
	}
} 
