package zingg.common.client.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class StructTypeFromPojoClass<ST, SF, T> {

    public abstract ST getStructType(Class<?> objClass) throws Exception;

    public List<SF> getFields(Class<?> objClass) {
        List<SF> structFields = new ArrayList<SF>();
        if (objClass.getSuperclass() != null) {
            Field[] fieldsSuper = objClass.getSuperclass().getDeclaredFields();
            for (Field f : fieldsSuper) {
                structFields.add(getStructField(f));
            }
        }
        Field[] fields = objClass.getDeclaredFields();
        for (Field f : fields) {
            structFields.add(getStructField(f));
        }
        return structFields;
    }

    public abstract SF getStructField(Field field);

    public abstract T getSFType(Class<?> t);

}
