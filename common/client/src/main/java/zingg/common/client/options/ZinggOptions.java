package zingg.common.client.options;

import java.util.HashMap;
import java.util.Map;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.Util;

public class ZinggOptions {

    public final static ZinggOption TRAIN = new ZinggOption("train");
    public final static ZinggOption MATCH = new ZinggOption("match");
    public final static ZinggOption TRAIN_MATCH = new ZinggOption("trainMatch");
    public final static ZinggOption FIND_TRAINING_DATA = new ZinggOption("findTrainingData");
    public final static ZinggOption LABEL = new ZinggOption("label");
    public final static ZinggOption VERIFY_BLOCKING = new ZinggOption("verifyBlocking");
    public final static ZinggOption LINK = new ZinggOption("link");
    public final static ZinggOption GENERATE_DOCS = new ZinggOption("generateDocs");
    public final static ZinggOption RECOMMEND = new ZinggOption("recommend");
    public final static ZinggOption UPDATE_LABEL = new ZinggOption("updateLabel");
    public final static ZinggOption FIND_AND_LABEL = new ZinggOption("findAndLabel");
    public final static ZinggOption ASSESS_MODEL = new ZinggOption("assessModel");
    public final static ZinggOption PEEK_MODEL = new ZinggOption("peekModel");
    public final static ZinggOption EXPORT_MODEL = new ZinggOption("exportModel");
    

    public static Map<String, ZinggOption> allZinggOptions;// = new HashMap<String, ZinggOption>();

    

    protected ZinggOptions() {
    }

    public static final void put(ZinggOption o) {
        if (allZinggOptions == null) {
            allZinggOptions = new HashMap<String, ZinggOption>();
        }
        allZinggOptions.put(o.getName(), o);
    }
    
    
    
    public static String[] getAllZinggOptions() {
        ZinggOption[] zo = allZinggOptions.values().toArray(new ZinggOption[allZinggOptions.size()]);
        int i = 0;
        String[] s = new String[zo.length];
        for (ZinggOption z: zo) {
            s[i++] = z.getName();
        }
        return s;
    }

   
    public static final ZinggOption getByValue(String value){
        for (ZinggOption zo: ZinggOptions.allZinggOptions.values()) {
            if (zo.name.equals(value)) return zo;
        }
        return null;
    }

	public static void verifyPhase(String phase) throws ZinggClientException {
		if (getByValue(phase) == null) {	
			String message = "'" + phase + "' is not a valid phase. "
			               + "Valid phases are: " + Util.join(getAllZinggOptions(), "|");
			throw new ZinggClientException(message);
		}
	}
}