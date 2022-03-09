package zingg.hash;

import java.io.Serializable;

import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;
import com.snowflake.snowpark_java.types.DataType;

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

		
		public DataFrame apply(DataFrame ds, String column, String newColumn) {
			return ds.withColumn(newColumn, Functions.callUDF(this.name, ds.col(column)));
		}
		
		public abstract Object apply(Row ds, String column);
}

