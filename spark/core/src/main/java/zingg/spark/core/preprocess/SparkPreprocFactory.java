package zingg.spark.core.preprocess;

import java.util.HashMap;

import zingg.common.core.preprocess.IPreProc;
import zingg.common.core.preprocess.PreprocConstants;
import zingg.common.core.preprocess.PreprocFactory;

public class SparkPreprocFactory extends PreprocFactory {
	
	public SparkPreprocFactory() {
		this.preprocMap = new HashMap<String, Class<? extends IPreProc>>();
		preprocMap.put(PreprocConstants.STOP_WORDS_PREPROC, SparkStopWordsRemover.class);
	}

}
