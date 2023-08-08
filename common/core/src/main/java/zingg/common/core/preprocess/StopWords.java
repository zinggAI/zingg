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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.Arguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.util.PipeUtilBase;

public class StopWords<S,D,R,C,T> {

	protected static String name = "zingg.preprocess.StopWords";
	public static final Log LOG = LogFactory.getLog(StopWords.class);
	protected static String stopWordColumn = ColName.COL_WORD;
	protected static final int COLUMN_INDEX_DEFAULT = 0;
	protected PipeUtilBase<S,D,R,C> pipeUtil;

	public PipeUtilBase<S, D, R, C> getPipeUtil() {
		return pipeUtil;
	}

	public void setPipeUtil(PipeUtilBase<S, D, R, C> pipeUtil) {
		this.pipeUtil = pipeUtil;
	}

	

    public ZFrame<D,R,C> preprocessForStopWords(S session, Arguments args, ZFrame<D,R,C> ds) throws ZinggClientException {
		/* 
		List<String> wordList = new ArrayList<String>();
		for (FieldDefinition def : args.getFieldDefinition()) {
			if (!(def.getStopWords() == null || def.getStopWords() == "")) {
				ZFrame<D,R,C> stopWords = getPipeUtil().read(false, false, getPipeUtil().getStopWordsPipe(args, def.getStopWords()));
				//if (!Arrays.asList(stopWords.schema().fieldNames()).contains(stopWordColumn)) {
					stopWordColumn = stopWords.columns()[COLUMN_INDEX_DEFAULT];
				//}
				wordList = stopWords.select(stopWordColumn).as(Encoders.STRING()).collectAsList();
				String pattern = wordList.stream().collect(Collectors.joining("|", "\\b(", ")\\b\\s?"));
				ds = ds.withColumn(def.getFieldName(), removeStopWords(pattern.toLowerCase()).apply(ds.col(def.getFieldName())));
			}
		}

		return ds;
		*/
		return ds;
	}
	
	/* 
	public static UserDefinedFunction removeStopWords(String stopWordsRegexString) {
		return udf((String s) -> {
				if (s == null) return null;
				return s.toLowerCase().replaceAll(stopWordsRegexString, "");
			}, DataTypes.StringType);
	}
	*/
}