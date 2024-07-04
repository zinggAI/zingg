package zingg.client.helper;

import org.apache.spark.sql.*;
import org.apache.spark.sql.types.StructType;
import zingg.common.client.ZFrame;
import zingg.spark.client.SparkFrame;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SparkDFObjectUtil extends DFObjectUtil<SparkSession, Dataset<Row>, Row, Column> {

    public SparkDFObjectUtil(SparkSession s) {
        super(s);
    }

    public static <T> StructType getStructSchema(Class<T> objClass) {
        StructType schema = new StructType();
        for (Field field : objClass.getDeclaredFields()) {
            field.setAccessible(true);
            schema = schema.add(field.getName(), JavaToSparkSchema.getDataType(String.valueOf(field.getType())));
        }
        return schema;
    }

    @Override
    public ZFrame<Dataset<Row>, Row, Column> getDFFromObjectList(List objList, Class objClass) throws Exception {
        if (objList == null || objClass == null) return null;
        List<Row> rows = new ArrayList<>();
        for (Object obj : objList) {
            rows.add(RowFactory.create(getObjectArray(obj)));
        }
        SparkSession sparkSession = getSession();
        StructType structType = JavaToSparkSchema.getStructSchema(objClass);
        return new SparkFrame(sparkSession.createDataFrame(rows, structType));
    }

    private Object[] getObjectArray(Object person) throws IllegalAccessException {
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