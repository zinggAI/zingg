package zingg.hash;


public abstract class LastChars<D,R,C,T> extends HashFunction<D,R,C,T>{
	int numChars;
	
	public LastChars(int endIndex) {
		super("last" + endIndex + "Chars");
		// DataTypes.StringType, DataTypes.StringType, true);
		this.numChars = endIndex;
	} 
	
	public String call(String field) {
		String r = null;
		if (field == null ) {
			r = field;
		}
		else {
			field = field.trim().toLowerCase();
			r= field.trim().toLowerCase().substring(Math.max(field.length() - numChars, 0));
		}
		return r;
		}


}
