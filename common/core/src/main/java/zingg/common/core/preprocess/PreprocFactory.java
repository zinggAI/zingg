package zingg.common.core.preprocess;

import java.util.HashMap;

public abstract class PreprocFactory {
	
    public PreprocFactory() {}

	/**
	 * Need to be populated by concrete sub class
	 */
    protected HashMap<String, Class<? extends IPreProc>> preprocMap;


    public IPreProc get(String preproc) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (IPreProc) preprocMap.get(preproc).newInstance();
    }

}
