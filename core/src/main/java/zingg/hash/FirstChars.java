package zingg.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

import zingg.block.Canopy;


public abstract class FirstChars<D,R,C,T,T1> extends HashFunction<D,R,C,T,T1>{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	int endIndex;
	
	public FirstChars(int endIndex) {
		super("first" + endIndex + "Chars");
		//TODO, DataTypes.StringType, DataTypes.StringType);
		this.endIndex = endIndex;
	}
	
	 
	 public String call(String field) {
		 
		 String r = null;
			if (field == null ) {
				r = field;
			}
			else{
				field = field.trim().toLowerCase();
				if (field.length() <= (endIndex)) {
			
				r = field; 
			}
			else {
				r = field.trim().substring(0, endIndex);
			}
			}
			LOG.debug("Applying " + this.name + " on " + field + " and returning " + r);
			return r;
	 }

	public Object apply(R ds, String column) {
		 return call((String) getAs(ds, column));
	}

	

	

}
