package zingg.spark.preprocess;

import org.apache.spark.sql.api.java.UDF1;

import zingg.preprocess.RemoveStopWords;

public class RemoveStopWordsUDF extends RemoveStopWords implements UDF1<String,String>{
	
	private static final long serialVersionUID = 1L;
	
	public RemoveStopWordsUDF(String stopWordsRegexString) {
		super(stopWordsRegexString);
	}
	@Override
	public String call(String s) throws Exception {
		return removeStopWordsUsingRegex(s);
	}
	
}
