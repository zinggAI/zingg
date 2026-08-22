package zingg.spark.core.hash;

import org.apache.spark.sql.types.DataTypes;
import zingg.common.core.hash.SameMonthYear;

import java.util.Date;

public class SparkSameMonthYear extends SparkHashFunction<Date, Integer> {
    public SparkSameMonthYear() {
        setBaseHash(new SameMonthYear());
        setDataType(DataTypes.DateType);
        setReturnType(DataTypes.IntegerType);
    }
}

