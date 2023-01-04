package zingg.hash;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class FirstChars implements Serializable{
	
	public static final Log LOG = LogFactory.getLog(FirstChars.class);

	private int endIndex;
	
	private String name;
	
	public FirstChars(int endIndex) {
	    this.name = "first" + endIndex + "Chars";
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


    public int getEndIndex() {
        return endIndex;
    }


    public String getName() {
        return name;
    }

}
