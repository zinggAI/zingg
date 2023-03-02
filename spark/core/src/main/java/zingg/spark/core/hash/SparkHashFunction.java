package zingg.spark.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZFrame;
import zingg.hash.BaseHash;
import zingg.hash.HashFunction;


public abstract class SparkHashFunction<T1, R> extends HashFunction<Dataset<Row>,Row,Column,DataType> implements UDF1<T1, R>{
	
	public static final Log LOG = LogFactory.getLog(SparkHashFunction.class);
	
	private BaseHash<T1, R> baseHash;

    public BaseHash<T1, R> getBaseHash() {
        return baseHash;
    }

    public void setBaseHash(BaseHash<T1, R> baseHash) {
        this.baseHash = baseHash;
        this.setName(baseHash.getName());
    }
	
	@Override
	public R call(T1 t1) {
	    return getBaseHash().call(t1);
	}
	

    @Override
    public ZFrame<Dataset<Row>, Row, Column> apply(ZFrame<Dataset<Row>, Row, Column> ds, String column,
            String newColumn) {
        return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
    }

    @Override
    public Object getAs(Row r, String column) {
        return r.getAs(column);
    }

    @Override
    public Object getAs(Dataset<Row> df, Row r, String column) {
        throw new UnsupportedOperationException("not supported for Spark");
    }


    @Override
    public Object apply(Row r, String column) {
        return call((T1)getAs(r, column));
   }


    @Override
    public Object apply(Dataset<Row> df, Row r, String column) {
        throw new UnsupportedOperationException("not supported for Spark");
    }

}
