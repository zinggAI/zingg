package zingg.common.core.preprocess.stopwords;

import java.io.Serializable;

public class RemoveStopWords implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String name = "removeStopWordsUDF";
	
	public RemoveStopWords() {
		super();
	}

	protected String removeStopWordsUsingRegex(String s,String stopWordsRegexString) {
		if (s == null || stopWordsRegexString==null) return null;
		return s.toLowerCase().replaceAll(stopWordsRegexString, "");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
