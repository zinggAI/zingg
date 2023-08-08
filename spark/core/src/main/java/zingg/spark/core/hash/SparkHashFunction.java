/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.spark.core.hash;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.ZFrame;
import zingg.common.core.hash.BaseHash;
import zingg.common.core.hash.HashFunction;


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

    /* 
    @Override
    public void writeCustomObject(ObjectOutputStream out) throws IOException {
        out.writeObject(getBaseHash());
        out.writeObject(getDataType());
        out.writeObject(getReturnType());
    }
    
    @Override
	public void readCustomObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
        setBaseHash((BaseHash) ois.readObject());
        setDataType((DataType)ois.readObject());
        setReturnType((DataType)ois.readObject());
    }*/
		

}
