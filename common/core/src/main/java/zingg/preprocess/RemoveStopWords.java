package zingg.preprocess;

import java.io.Serializable;

public class RemoveStopWords implements Serializable {
	private static final long serialVersionUID = 1L;
	private String stopWordsRegexString;
	private String name = "removeStopWordsUDF";
	
	public RemoveStopWords(String stopWordsRegexString) {
		super();
		this.stopWordsRegexString = stopWordsRegexString;
	}

	protected String removeStopWordsUsingRegex(String s) {
		if (s == null) return null;
		return s.toLowerCase().replaceAll(stopWordsRegexString, "");
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
