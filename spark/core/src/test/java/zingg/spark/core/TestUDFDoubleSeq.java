package zingg.spark.core;

import org.apache.spark.sql.api.java.UDF2;

import scala.collection.immutable.Seq;

public class TestUDFDoubleSeq implements UDF2<Seq<Double>,Seq<Double>, Double>{
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Double call(Seq<Double> t1, Seq<Double> t2) throws Exception {
		return 0.1;
	}
	
}
