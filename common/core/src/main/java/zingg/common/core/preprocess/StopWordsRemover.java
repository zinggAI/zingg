package zingg.common.core.preprocess;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.context.IContext;

public abstract class StopWordsRemover<S,D,R,C,T> implements Serializable{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.preprocess.StopWordsRemover";
	public static final Log LOG = LogFactory.getLog(StopWordsRemover.class);
	protected static final int COLUMN_INDEX_DEFAULT = 0;
	
	protected IContext<S,D,R,C,T> context;
	protected IArguments args;

	public StopWordsRemover(IContext<S, D, R, C, T> context,IArguments args) {
		super();
		this.context = context;
		this.args = args;
	}

	public ZFrame<D, R, C> preprocessForStopWords(ZFrame<D, R, C> ds) throws ZinggClientException {
		for (FieldDefinition def : getArgs().getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				ZFrame<D, R, C> stopWords = getStopWords(def);
				String stopWordColumn = getStopWordColumnName(stopWords);
				List<String> wordList = getWordList(stopWords,stopWordColumn);
				String pattern = getPattern(wordList);
				ds = removeStopWordsFromDF(ds, def.getFieldName(), pattern);
			}
		}
		return ds;
	}

	protected ZFrame<D,R,C> getStopWords(FieldDefinition def) throws ZinggClientException {
		PipeUtilBase<S,D,R,C> pipeUtil = getContext().getPipeUtil();
		ZFrame<D,R,C> stopWords = pipeUtil.read(false, false, getContext().getModelHelper().getStopWordsPipe(def.getStopWords()));
		return stopWords;
	}

	/**
	 * Return the 0th column name if the column ColName.COL_WORD is not in the list of the columns of the stopwords DF
	 * @return
	 */
	protected String getStopWordColumnName(ZFrame<D,R,C> stopWords) {
		String[] fieldNames = stopWords.fieldNames();
		if (!Arrays.asList(fieldNames).contains(ColName.COL_WORD)) {
			return stopWords.columns()[getColumnIndexDefault()];
		} else {
			return ColName.COL_WORD;
		}
	}
	
	protected List<String> getWordList(ZFrame<D,R,C> stopWords, String stopWordColumn) {
		return stopWords.select(stopWordColumn).collectFirstColumn();
	}
	
	/**
	 * Regex to remove the stop words
	 * @param wordList
	 * @return
	 */
	protected String getPattern(List<String> wordList) {
		String pattern = wordList.stream().collect(Collectors.joining("|", "\\b(", ")\\b\\s?"));
		String lowerCasePattern = pattern.toLowerCase();
		return lowerCasePattern;
	}
    
	// implementation specific as may require UDF
	public abstract ZFrame<D,R,C> removeStopWordsFromDF(ZFrame<D,R,C> ds,String fieldName, String pattern);
	
	public IContext<S, D, R, C, T> getContext() {
		return context;
	}

	public void setContext(IContext<S, D, R, C, T> context) {
		this.context = context;
	}

	public IArguments getArgs() {
		return args;
	}

	public void setArgs(IArguments args) {
		this.args = args;
	}


	public static int getColumnIndexDefault() {
		return COLUMN_INDEX_DEFAULT;
	}
	
	
}
