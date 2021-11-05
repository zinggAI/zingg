package zingg.hash;

import java.io.Serializable;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataType;

public abstract class HashFunction implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected DataType dataType;
		protected String name;
		protected boolean isUdf = true;
		protected DataType returnType;
		
		public HashFunction(String name, DataType cl, DataType returnType) {
			this.name = name;
			this.dataType = cl;
			this.returnType = returnType;
		}
	
		public HashFunction(String name, DataType cl, DataType returnType, boolean isUdf) {
			this(name, cl, returnType);
			this.isUdf = isUdf;
		}
		
		public DataType getDataType() {
			return dataType;
		}
		
		
	
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public boolean isUdf() {
			return isUdf;
		}

		public void setUdf(boolean isUdf) {
			this.isUdf = isUdf;
		}

		public DataType getReturnType() {
			return returnType;
		}

		public void setReturnType(DataType returnType) {
			this.returnType = returnType;
		}

		public void setDataType(DataType dataType) {
			this.dataType = dataType;
		}

		
		public Dataset<Row> apply(Dataset<Row> ds, String column, String newColumn) {
			return ds.withColumn(newColumn, functions.callUDF(this.name, ds.col(column)));
		}
		
		public abstract Object apply(Row ds, String column);


	@Override
	public String toString() {
		return "{" +
			" dataType='" + getDataType() + "'" +
			", name='" + getName() + "'" +
			", isUdf='" + isUdf + "'" +
			", returnType='" + getReturnType() + "'" +
			"}";
	}

}

