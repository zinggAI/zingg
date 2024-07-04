package zingg.client.helper;

import java.lang.reflect.Field;

import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

public class JavaToSparkSchema  {

    public static <T> StructType  getStructSchema  (Class<T> objClass) {
       StructType schema = new StructType();
       for(Field field: objClass.getDeclaredFields()) {
           field.setAccessible(true);
           schema = schema.add(field.getName(),getDataType(String.valueOf(field.getType())));
       }
        return schema;
    }

    public static DataType getDataType(String type) {

        String simpleName = type.split(" ")[type.split(" ").length - 1];

        switch (simpleName) {
            case "int" :
            case "java.lang.Integer" : return DataTypes.IntegerType;

            case "short":
            case "java.lang.Short": return  DataTypes.ShortType;

            case "byte":
            case "java.lang.Byte":
                return DataTypes.ByteType;

            case "long":
            case "java.lang.Long":
                return DataTypes.LongType;

            case "float":
            case "java.lang.Float":
                return DataTypes.FloatType;

            case "double":
            case "java.lang.Double":
                return DataTypes.DoubleType;

            case "boolean":
            case "java.lang.Boolean":
                return DataTypes.BooleanType;

            case "Date":
            case "java.sql.Date":
                return DataTypes.DateType;

            case "Timestamp":
            case "java.sql.Timestamp":
                return DataTypes.TimestampType;

            case "null":
            case "char":
            case "java.lang.Character":
            case "java.lang.String":
            default: return DataTypes.StringType;
        }
    }
}