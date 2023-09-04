package zingg.common.core.preprocess;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.util.PipeUtilBase;

public abstract class StopWordsRemover<S,D,R,C,T> implements Serializable, IPreProc<S,D,R,C,T> {

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.preprocess.StopWordsRemover";
	public static final Log LOG = LogFactory.getLog(StopWordsRemover.class);
	protected static final int COLUMN_INDEX_DEFAULT = 0;
	
	@Override
	public ZFrame<D, R, C> preprocess(S session, PipeUtilBase<S, D, R, C> pipeUtil, Arguments args, ZFrame<D, R, C> ds)
			throws ZinggClientException {
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				ZFrame<D, R, C> stopWords = getStopWords(pipeUtil,args,def);
				String stopWordColumn = getStopWordColumnName(stopWords);
				List<String> wordList = getWordList(stopWords,stopWordColumn);
				String pattern = getPattern(wordList);
				ds = removeStopWordsFromDF(session, ds, def.getFieldName(), pattern);
			}
		}
		return ds;
	}

	protected ZFrame<D,R,C> getStopWords(PipeUtilBase<S, D, R, C> pipeUtil, Arguments args, FieldDefinition def) throws ZinggClientException {
		ZFrame<D,R,C> stopWords = pipeUtil.read(false, false, pipeUtil.getStopWordsPipe(args, def.getStopWords()));
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
		return stopWords.select(stopWordColumn).collectAsListOfStrings();
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
	protected abstract ZFrame<D,R,C> removeStopWordsFromDF(S session, ZFrame<D,R,C> ds,String fieldName, String pattern);
	
	public static int getColumnIndexDefault() {
		return COLUMN_INDEX_DEFAULT;
	}
	
	
}
