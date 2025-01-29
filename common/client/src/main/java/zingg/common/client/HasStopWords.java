package zingg.common.client;

import scala.Serializable;

public class HasStopWords implements Serializable {

    public static boolean isStopwordField(FieldDefinition f){
        if(!(f.getStopWords() == null || f.getStopWords() == "")){
            return true;
        }
        else {
            return false;
        }
    }
    
}
