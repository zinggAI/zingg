package zingg.hash;


public abstract class LastWord<D,R,C,T> extends HashFunction<D,R,C,T>{
	public LastWord() {
		super("lastWord");		
	}

			
	public String call(String field) {
		String r = null;
		if (field == null ) {
			r = field;
		}
		else {
			String[] v= field.trim().toLowerCase().split(" ");
			return v[v.length-1];
		}
		return r;
	}

	@Override
	public Object getAs(D df, R r, String column) {
		return null;
	}

	@Override
	public Object apply(D df, R r, String column) {
		return null;
	}

	@Override
	public Object apply(R r, String column) {
		return null;
	}

	
}
