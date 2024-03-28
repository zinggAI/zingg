package zingg.common.core.hash;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FirstChars extends BaseHash<String,String>{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	private int endIndex;

	public FirstChars(int endIndex) {
	    setName("first" + endIndex + "Chars");
		this.endIndex = endIndex;
	}
	
	 
	 public String call(String field) {
		 
		 String r = null;
			if (field == null ) {
				r = field;
			}
			else{
				field = field.trim(); //.toLowerCase();
				if (field.length() <= (endIndex)) {
			
				r = field; 
			}
			else {
				r = field.trim().substring(0, endIndex);
			}
			}
			//LOG.debug("Applying " + this.getName() + " on " + field + " and returning " + r);
			return r;
	 }


    public int getEndIndex() {
        return endIndex;
    }

}
