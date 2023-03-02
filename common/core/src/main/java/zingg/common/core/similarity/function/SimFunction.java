package zingg.common.core.similarity.function;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class SimFunction<T> implements Serializable{ //extends UDF2<T,T, Double>{
	
	public static final Log LOG = LogFactory.getLog(SimFunction.class);
	protected String name;
	
	public int getNumFeatures() {
		return 1;
	}

	public abstract Double call(T t1, T t2) throws Exception;

	
	
	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}
	
    
	
	public SimFunction() {
		
	}

	public SimFunction(String name) {
		this.name = name;
	}
	

	
	
	/*public String getInputColumn() ;

	public void setInputColumn(String inputColumn);

	public String getOutputColumn();

	public void setOutputColumn(String outputColumn);
	*/
}
