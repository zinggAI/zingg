package zingg.common.infra.util;

import java.lang.reflect.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class PojoToArrayConverter {
    
    public static <T> Object[] getObjectArray(T person) throws NoSuchAlgorithmException, IllegalArgumentException, IllegalAccessException {
        
        List<Object> values = new ArrayList<Object>();
        if (person.getClass().getSuperclass() != null) {
            Field[] fieldsSuper =  person.getClass().getSuperclass().getDeclaredFields();
            if (fieldsSuper != null){
                for (Field f: fieldsSuper) {
                    f.setAccessible(true);
                    values.add(f.get(person));
                  }

            }
        }
        Field[] fields = person.getClass().getDeclaredFields();
        
        for (Field field: fields) {
          field.setAccessible(true);
          values.add(field.get(person));
        }
     
        return values.toArray(new Object[values.size()]);
     }
}
