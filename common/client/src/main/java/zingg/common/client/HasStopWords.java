package zingg.common.client;

import scala.Serializable;

public class HasStopWords implements Serializable {

    public static boolean isStopwordField(FieldDefinition f){
        return (!(f.getStopWords() == null || f.getStopWords() == ""));
    }
    
}
