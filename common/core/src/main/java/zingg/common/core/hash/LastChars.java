package zingg.common.core.hash;


public class LastChars extends BaseHash<String,String>{
	private int numChars;
	
	public LastChars(int endIndex) {
	    setName("last" + endIndex + "Chars");
		this.numChars = endIndex;
	} 
	
	public String call(String field) {
		String r = null;
		if (field == null ) {
			r = field;
		}
		else {
			field = field.trim().toLowerCase();
			//r= field.trim().toLowerCase().substring(Math.max(field.length() - numChars, 0));
			r= field.trim().substring(Math.max(field.length() - numChars, 0));
		}
		return r;
	}

    public int getNumChars() {
        return numChars;
    }

}
