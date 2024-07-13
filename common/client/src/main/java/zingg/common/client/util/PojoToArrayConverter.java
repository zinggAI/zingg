package zingg.common.client.util;

import java.lang.reflect.Field;

public class PojoToArrayConverter {

    public static Object[] getObjectArray(Object object) throws IllegalAccessException {
        Field[] fieldsInChildClass = object.getClass().getDeclaredFields();
        Field[] fieldsInParentClass = null;

        int fieldCountInChildClass = fieldsInChildClass.length;
        int fieldCount = fieldCountInChildClass;

        if (object.getClass().getSuperclass() != null) {
            fieldCount += object.getClass().getSuperclass().getDeclaredFields().length;
            fieldsInParentClass = object.getClass().getSuperclass().getDeclaredFields();
        }

        //fieldCount = fieldCountChild + fieldCountParent
        Object[] objArr = new Object[fieldCount];

        int idx = 0;

        //iterate through child class fields
        for (; idx < fieldCountInChildClass; idx++) {
            Field field = fieldsInChildClass[idx];
            field.setAccessible(true);
            objArr[idx] = field.get(object);
        }

        //iterate through super class fields
        for (; idx < fieldCount; idx++) {
            Field field = fieldsInParentClass[idx - fieldCountInChildClass];
            field.setAccessible(true);
            objArr[idx] = field.get(object);
        }

        return objArr;
    }
}
