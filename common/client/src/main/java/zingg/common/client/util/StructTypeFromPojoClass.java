package zingg.common.client.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class StructTypeFromPojoClass<ST, SF, T> {

    public abstract ST getStructType(Class<?> objClass) throws Exception;

    public List<SF> getFields(Class<?> objClass) {
        List<SF> structFields = new ArrayList<SF>();
        Field[] fields = objClass.getDeclaredFields();

        //add child class fields in struct
        for (Field f : fields) {
            structFields.add(getStructField(f));
        }

        //add parent class fields in struct
        if (objClass.getSuperclass() != null) {
            Field[] fieldsSuper = objClass.getSuperclass().getDeclaredFields();
            for (Field f : fieldsSuper) {
                structFields.add(getStructField(f));
            }
        }
        return structFields;
    }

    public abstract SF getStructField(Field field);

    public abstract T getSFType(Class<?> t);

}
