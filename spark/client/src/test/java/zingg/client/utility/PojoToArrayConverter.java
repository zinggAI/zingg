package zingg.client.utility;

import java.lang.reflect.Field;

public class PojoToArrayConverter {

    public static Object[] getObjectArray(Object person) throws IllegalAccessException {
        Field[] fields = person.getClass().getDeclaredFields();
        int fieldCount = fields.length;
        Object[] objArr = new Object[fieldCount];

        for (int i = 0; i < objArr.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);

            objArr[i] = field.get(person);
        }

        return objArr;
    }
}
