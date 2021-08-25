package zingg.client;

import java.util.Arrays;

public enum ZinggOptions {
    
    TRAIN("train"), 
    MATCH("match"), 
    TRAIN_MATCH("trainMatch"), 
    FIND_TRAINING_DATA("findTrainingData"), 
    LABEL("label"),
    LINK("link");

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


}