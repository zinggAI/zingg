package zingg.spark.client.util;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import zingg.common.client.util.StructTypeFromPojoClass;

public class SparkStructTypeFromPojoClass extends StructTypeFromPojoClass<StructType, StructField, DataType> {

    public StructType getStructType(Class<?> objClass)
            throws NoSuchAlgorithmException, IllegalArgumentException, IllegalAccessException {
        List<StructField> structFields = getFields(objClass);
        return new StructType(structFields.toArray(new StructField[structFields.size()]));
    }

    public StructField getStructField(Field field) {
        field.setAccessible(true);
        return new StructField(field.getName(), getSFType(field.getType()), true, Metadata.empty());
    }

    public DataType getSFType(Class<?> t) {
        if (t.getCanonicalName().contains("String")) {
            return DataTypes.StringType;
        } else if (t.getCanonicalName().contains("Integer")) {
            return DataTypes.IntegerType;
        } else if (t.getCanonicalName().contains("Long")) {
            return DataTypes.LongType;
        } else if (t.getCanonicalName().contains("Float")) {
            return DataTypes.FloatType;
        } else if (t.getCanonicalName().contains("Double")) {
            return DataTypes.DoubleType;
        } else if (t.getCanonicalName().contains("Date")) {
            return DataTypes.DateType;
        } else if (t.getCanonicalName().contains("Timestamp")) {
            return DataTypes.TimestampType;
        }

        return null;
    }

}