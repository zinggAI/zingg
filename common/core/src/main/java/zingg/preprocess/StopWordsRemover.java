package zingg.preprocess;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.FieldDefinition;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.common.Context;
import zingg.util.PipeUtilBase;

public abstract class StopWordsRemover<S,D,R,C,T> implements Serializable{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.preprocess.StopWordsRemover";
	public static final Log LOG = LogFactory.getLog(StopWordsRemover.class);
	protected String stopWordColumn = ColName.COL_WORD;
	protected static final int COLUMN_INDEX_DEFAULT = 0;
	
	protected Context<S,D,R,C,T> context;
	protected Arguments args;

	public StopWordsRemover(Context<S, D, R, C, T> context,Arguments args) {
		super();
		this.context = context;
		this.args = args;
	}

	public ZFrame<D, R, C> preprocessForStopWords(ZFrame<D, R, C> ds) throws ZinggClientException {
		List<String> wordList = new ArrayList<String>();
		for (FieldDefinition def : getArgs().getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				ZFrame<D, R, C> stopWords = getStopWords(def);
				resetStopWordColumn(stopWords);
				wordList = getWordList(stopWords);
				String pattern = getPattern(wordList);
				ds = removeStopWordsFromDF(ds, def.getFieldName(), pattern);
			}
		}
		return ds;
	}

	protected ZFrame<D,R,C> getStopWords(FieldDefinition def) throws ZinggClientException {
		PipeUtilBase<S,D,R,C> pipeUtil = getContext().getPipeUtil();
		ZFrame<D,R,C> stopWords = pipeUtil.read(false, false, pipeUtil.getStopWordsPipe(getArgs(), def.getStopWords()));
		return stopWords;
	}

	protected void resetStopWordColumn(ZFrame<D,R,C> stopWords) {
		String[] fieldNames = stopWords.fieldNames();
		if (!Arrays.asList(fieldNames).contains(getStopWordColumn())) {
			setStopWordColumn(stopWords.columns()[getColumnIndexDefault()]);
		}
	}
	
	protected List<String> getWordList(ZFrame<D,R,C> stopWords) {
		return stopWords.select(getStopWordColumn()).collectAsListOfStrings();
	}
	
	/**
	 * Regex to remove the stop words
	 * @param wordList
	 * @return
	 */
	protected String getPattern(List<String> wordList) {
		return wordList.stream().collect(Collectors.joining("|", "\\b(", ")\\b\\s?"));
	}
    
	// implementation specific as may require UDF
	protected abstract ZFrame<D,R,C> removeStopWordsFromDF(ZFrame<D,R,C> ds,String fieldName, String pattern);
	
	public  String getStopWordColumn() {
		return stopWordColumn;
	}

	public  void setStopWordColumn(String stopWordColumn) {
		this.stopWordColumn = stopWordColumn;
	}

	public Context<S, D, R, C, T> getContext() {
		return context;
	}

	public void setContext(Context<S, D, R, C, T> context) {
		this.context = context;
	}

	public Arguments getArgs() {
		return args;
	}

	public void setArgs(Arguments args) {
		this.args = args;
	}


	public static int getColumnIndexDefault() {
		return COLUMN_INDEX_DEFAULT;
	}
	
	
}