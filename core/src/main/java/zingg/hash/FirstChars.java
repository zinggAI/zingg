package zingg.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class FirstChars<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	int endIndex;
	
	public FirstChars(int endIndex) {
		super("first" + endIndex + "Chars");
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
	
	 @Override
	 public Object apply(R r, String column) {
		 return null;
	 }
 
 
	 @Override
	 public Object apply(D df, R r, String column) {
		 return null;
	 }
 

	@Override
	public Object getAs(R r, String column) {
		return null;
	}

}
