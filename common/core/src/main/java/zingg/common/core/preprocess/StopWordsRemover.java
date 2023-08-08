/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
import zingg.common.core.Context;
import zingg.common.core.util.PipeUtilBase;

public abstract class StopWordsRemover<S,D,R,C,T> implements Serializable{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.preprocess.StopWordsRemover";
	public static final Log LOG = LogFactory.getLog(StopWordsRemover.class);
	protected static final int COLUMN_INDEX_DEFAULT = 0;
	
	protected Context<S,D,R,C,T> context;
	protected Arguments args;

	public StopWordsRemover(Context<S, D, R, C, T> context,Arguments args) {
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
		ZFrame<D,R,C> stopWords = pipeUtil.read(false, false, pipeUtil.getStopWordsPipe(getArgs(), def.getStopWords()));
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
	protected abstract ZFrame<D,R,C> removeStopWordsFromDF(ZFrame<D,R,C> ds,String fieldName, String pattern);
	
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
