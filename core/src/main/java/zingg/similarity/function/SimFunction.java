package zingg.similarity.function;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF2;

public interface SimFunction<T> { //extends UDF2<T,T, Double>{
	
	
	
	public int getNumFeatures();
	Double call(T t1, T t2) throws Exception;
	
	
	/*public String getInputColumn() ;

	public void setInputColumn(String inputColumn);

	public String getOutputColumn();

	public void setOutputColumn(String outputColumn);
	*/
}
