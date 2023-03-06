package zingg.spark.preprocess;

import org.apache.spark.sql.api.java.UDF2;

import zingg.preprocess.RemoveStopWords;

public class RemoveStopWordsUDF extends RemoveStopWords implements UDF2<String,String,String>{
	
	private static final long serialVersionUID = 1L;
	
	public RemoveStopWordsUDF() {
		super();
	}
	@Override
	public String call(String s,String stopWordsRegexString) throws Exception {
		return removeStopWordsUsingRegex(s,stopWordsRegexString);
	}
	
}
