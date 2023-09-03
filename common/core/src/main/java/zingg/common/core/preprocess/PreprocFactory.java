package zingg.common.core.preprocess;

import java.util.HashMap;

public class PreprocFactory {
	
    public PreprocFactory() {}

    protected static HashMap<String, Class> preprocMap = new  HashMap<String, Class>();

    static {
    	//TODO a place holder for now [class as well as move to constants]
    	preprocMap.put("stopWords", StopWords.class);
    }

    public IPreProc get(String preproc) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (IPreProc) preprocMap.get(preproc).newInstance();
    }

}
