package zingg.common.client.util;

import java.util.HashMap;
import java.util.Map;

public class StringRedactor {

    protected String REDACT_PATTERN = "(?i)secret|password|token|access[.]key";
    protected String REDACT_VALUE = "********(redacted)";

    public String redact(Map<String, String> values){
        
        if (values != null){
            Map<String, String> valuesClone = new HashMap<String, String>();
            valuesClone.putAll(values);
            System.out.println("values clone is " + valuesClone);
            for (String key: valuesClone.keySet()){
                if (key.matches(REDACT_PATTERN)){
                    valuesClone.put(key, REDACT_VALUE);
                }
            }
            return valuesClone.toString();
        }
        else {
            return null;
        }

    }
    
}
