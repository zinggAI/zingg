package zingg.spark.core.preprocess;

import java.util.HashMap;

import zingg.common.core.preprocess.IPreProc;
import zingg.common.core.preprocess.PreprocFactory;

public class SparkPreprocFactory extends PreprocFactory {
	
	//TODO move to a constant class
	protected static final String STOP_WORDS_PREPROC = "stopWords";

	public SparkPreprocFactory() {
		this.preprocMap = new HashMap<String, Class<? extends IPreProc>>();
		preprocMap.put(STOP_WORDS_PREPROC, SparkStopWordsRemover.class);
	}

}
