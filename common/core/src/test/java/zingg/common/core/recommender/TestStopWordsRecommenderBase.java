package zingg.common.core.recommender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;
import zingg.common.core.recommender.model.Records;
import zingg.common.core.recommender.model.WordByCount;

public abstract class TestStopWordsRecommenderBase<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TestStopWordsRecommenderBase.class);
    protected final Context<S, D, R, C, T> context;
    protected final DFObjectUtil<S, D, R, C> dfObjectUtil;
    protected final IArguments arguments = new Arguments();
    protected static final int NO_OF_RECORDS = 5;
	protected List<R> stopwordRow = new ArrayList<R>();
	protected List<String> stopwordList = new ArrayList<String>();
	protected static final String COL_STOPWORDS = "stopwords";
    private final StopWordsRecommender<S,D,R,C,T> recommender;

    public TestStopWordsRecommenderBase(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S,D,R,C,T> context){
	    this.dfObjectUtil = dfObjectUtil;
        this.context = context;
        this.recommender = getRecommender(context,arguments);
    }

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
		List<String> arr = new ArrayList<String>();
		arr.add("the");
		arr.add("of");
		assertTrue(validTestWithStopWords(stopwordList, arr));
	}

	@Test
	public void testWithOneTenthCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0.1");
		stopwordList = getStopWordList(0.1f);
		// for cutoff = 0.4, expected no. of stopword should have frequecy greater than or equal to
		// totalwords*cutoff(here, 108*0.4 = 21.6) which should be 4 ("the","44"), ("of","27"), ("in","11") and ("was","11")
		assertEquals(4, stopwordList.size());
		List<String> arr = new ArrayList<String>();
		arr.add("the");
		arr.add("of");
		arr.add("in");
		arr.add("was");
		assertTrue(validTestWithStopWords(stopwordList, arr));
	}

	@Test
	public void testWithZeroCuttoff() throws Throwable {
		LOG.info("Test with stopCutoff = 0");
		stopwordList = getStopWordList(0.0f);
		// zero cutoff selects all words as stopwords, hence hence expected no. of stopword should be 9
		assertEquals(9, stopwordList.size());
	}

    public abstract StopWordsRecommender<S,D,R,C,T> getRecommender(IContext<S, D, R, C, T> context, IArguments args);

    public abstract ZFrame<D,R,C> getStopWordsDataset(ZFrame<D,R,C> dataset);

    public abstract String getStopWordColName();

    /* join list elements */
	public static String getStringFromList(List<String> strs) {
		return strs.stream().reduce((p1, p2) -> p1 + " " + p2)
				.map(Object::toString)
				.orElse("");
	}

    /* method to check if words in List of String arr are recommended or not */
	public Boolean validTestWithStopWords(List<String> stopwordList, List<String> arr){
		for(String r: stopwordList){
			if(!arr.contains(r)){
				return false;
			}
		}
		return true;
	}

    /* Breaks WordByCount object's 'count' Variable into 'm' random numbers such that sum(arr[m]) = count */
	public static int[][] randomDistributionList(int m, List<WordByCount> wbc) {
		int[][] arr=new int[m][wbc.size()];
		for (WordByCount c:wbc) {
            for(int i = 0; i < c.z_count; i++){
				arr[(int) (Math.random() * m)][wbc.indexOf(c)]++;
            }
		}
		return arr;
	}

	/* creates list of string countaining recommended stopwords based on stopWordsCutoff */
	public List<String> getStopWordList(float cutoff) throws Throwable {

        ZFrame<D,R,C> dataset = createDFWithGivenStopWords();

		//create stopwords dataset and display according to stopWordsCutoff
		arguments.setStopWordsCutoff(cutoff);
		ZFrame<D,R,C> stopWords = recommender.findStopWords(getStopWordsDataset(dataset), COL_STOPWORDS);
		//stopWords.show();

		//convert spark Dataset<Row> into java List<Row>
		stopwordRow = stopWords.collectAsList();

		//convert list of Row containing word,count to list of String containing only stopword
		List<String> stopwordsList = new ArrayList<String>();
		for(R r: stopwordRow){
            String s = stopWords.getAsString(r,getStopWordColName());
			stopwordsList.add(s);
		}
		return stopwordsList;
	}


	/* creates a dataframe for given words and their frequency*/
	public ZFrame<D,R,C> createDFWithGivenStopWords() throws Exception {
		//Initialize WordByCount list and add all the (word,count) one by one.
		List<WordByCount> wordCount= new ArrayList<WordByCount>();
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
		int[][] wordDistribution = randomDistributionList(NO_OF_RECORDS,wordCount);

		//add stopwords as per calulcated random distribution to each record
		List<Records> records = new ArrayList<Records>();
		for (int index = 0; index < NO_OF_RECORDS; index++) {
			List<String> strList = new ArrayList<String>();
			for(WordByCount wc: wordCount){
				strList.addAll(Collections.nCopies(wordDistribution[index][wordCount.indexOf(wc)],wc.z_word));
			}
			records.add(new Records(getStringFromList(strList)));
		}
		
		//create dataframe with given records
		ZFrame<D,R,C> recordDF = dfObjectUtil.getDFFromObjectList(records, Records.class);
		return recordDF;
	}
	
	
    
}
