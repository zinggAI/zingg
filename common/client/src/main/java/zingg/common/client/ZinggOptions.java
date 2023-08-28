package zingg.common.client;

import java.util.HashMap;
import java.util.Map;

import zingg.common.client.util.Util;

public class ZinggOptions {

    public final static ZinggOptions TRAIN = new ZinggOptions("train");
    public final static ZinggOptions MATCH = new ZinggOptions("match");
    public final static ZinggOptions TRAIN_MATCH = new ZinggOptions("trainMatch");
    public final static ZinggOptions FIND_TRAINING_DATA = new ZinggOptions("findTrainingData");
    public final static ZinggOptions LABEL = new ZinggOptions("label");
    public final static ZinggOptions LINK = new ZinggOptions("link");
    public final static ZinggOptions GENERATE_DOCS = new ZinggOptions("generateDocs");
    public final static ZinggOptions RECOMMEND = new ZinggOptions("recommend");
    public final static ZinggOptions UPDATE_LABEL = new ZinggOptions("updateLabel");
    public final static ZinggOptions FIND_AND_LABEL = new ZinggOptions("findAndLabel");
    public final static ZinggOptions ASSESS_MODEL = new ZinggOptions("assessModel");
    public final static ZinggOptions PEEK_MODEL = new ZinggOptions("peekModel");
    public final static ZinggOptions EXPORT_MODEL = new ZinggOptions("exportModel");

    public static Map<String, ZinggOptions> allZinggOptions = new HashMap<String, ZinggOptions>();

    String name;

    public ZinggOptions(String name) {
        this.name = name;
        allZinggOptions.put(name, this);
    }
    
    
    
    public static String[] getAllZinggOptions() {
        ZinggOptions[] zo = allZinggOptions.values().toArray(new ZinggOptions[allZinggOptions.size()]);
        int i = 0;
        String[] s = new String[zo.length];
        for (ZinggOptions z: zo) {
            s[i++] = z.getName();
        }
        return s;
    }

    public String getName() { 
        return name;
    }

    public static final ZinggOptions getByValue(String value){
        for (ZinggOptions zo: ZinggOptions.allZinggOptions.values()) {
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