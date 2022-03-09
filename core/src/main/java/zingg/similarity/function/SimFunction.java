package zingg.similarity.function;

import com.snowflake.snowpark_java.udf.JavaUDF2;

public interface SimFunction<T> extends JavaUDF2<T,T, Double>{
	
	
	
	public int getNumFeatures();
	
	
	/*public String getInputColumn() ;

	public void setInputColumn(String inputColumn);

	public String getOutputColumn();

	public void setOutputColumn(String outputColumn);
	*/
}
