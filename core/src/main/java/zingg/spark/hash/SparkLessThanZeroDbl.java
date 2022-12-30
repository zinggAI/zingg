package zingg.spark.hash;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.client.ZFrame;
import zingg.hash.LessThanZeroDbl;

public class SparkLessThanZeroDbl extends LessThanZeroDbl<Dataset<Row>,Row,Column,DataType>  implements UDF1<Double, Boolean>{
	
    @Override
    public ZFrame<Dataset<Row>, Row, Column> apply(ZFrame<Dataset<Row>, Row, Column> ds, String column,
            String newColumn) {
        return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
    }

    @Override
    public Object getAs(Row r, String column) {
        return (Double) r.getAs(column);
    }

    @Override
    public Object getAs(Dataset<Row> df, Row r, String column) {
        throw new UnsupportedOperationException("not supported for Spark");
    }


    @Override
    public Object apply(Row r, String column) {
        return call((Double) getAs(r, column));
   }


    @Override
    public Object apply(Dataset<Row> df, Row r, String column) {
        throw new UnsupportedOperationException("not supported for Spark");
    }

}
