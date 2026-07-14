package zingg.common.client.util;

import java.util.HashMap;
import java.util.Map;

public class StringRedactor {

    protected String REDACT_PATTERN;
    protected String REDACT_VALUE;

    public StringRedactor(){
        REDACT_PATTERN = "(?i)secret|password|token|access?key|key|sfPassword";
        REDACT_VALUE = "********(redacted)";
    }

    public StringRedactor(String pattern, String value){
        REDACT_PATTERN = pattern;
        REDACT_VALUE = value;
    }

    public Map<String, String> replace(Map<String, String> values){
        if (values != null){
            Map<String, String> valuesClone = new HashMap<String, String>();
            valuesClone.putAll(values);
            for (String key: valuesClone.keySet()){
                if (key.matches(REDACT_PATTERN)){
                    valuesClone.put(key, REDACT_VALUE);
                }
            }
            return valuesClone;
        }
        else {
            return values;
        }
    }

    public String redact(Map<String, String> values){
        
        if (values != null){
            Map<String, String> valuesClone = replace(values);
            return valuesClone.toString();
        }
        else {
            return "{}";
        }

    }
    
}
