package zingg.spark.core;

import org.apache.spark.sql.api.java.UDF2;

public class TestUDFDoubleObj implements UDF2<Object,Object, Double>{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Double call(Object t1, Object t2) throws Exception {
		
		System.out.println("TestUDFDoubleObj class" +t1.getClass());
		return 0.3;
	}
	
}
