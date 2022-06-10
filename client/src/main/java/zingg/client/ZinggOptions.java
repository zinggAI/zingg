package zingg.client;

import java.util.Arrays;

import zingg.client.util.Util;

public enum ZinggOptions {
    
    TRAIN("train"), 
    MATCH("match"), 
    TRAIN_MATCH("trainMatch"), 
    FIND_TRAINING_DATA("findTrainingData"), 
    LABEL("label"),
    LINK("link"),
    GENERATE_DOCS("generateDocs"),
    UPDATE_LABEL("updateLabel"),
    FIND_AND_LABEL("findAndLabel"),
    ASSESS_MODEL("assessModel"),
    PEEK_MODEL("peekModel"),
    EXPORT_MODEL("exportModel");

    private String value;

    ZinggOptions(String s) {
        this.value = s;
    }

    public static String[] getAllZinggOptions() {
        ZinggOptions[] zo = ZinggOptions.values();
        int i = 0;
        String[] s = new String[zo.length];
        for (ZinggOptions z: zo) {
            s[i++] = z.getValue();
        }
        return s;
    }

    public String getValue() { 
        return value;
    }

    public static final ZinggOptions getByValue(String value){
        for (ZinggOptions zo: ZinggOptions.values()) {
            if (zo.value.equals(value)) return zo;
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