package zingg.hash;


public abstract class IsNullOrEmpty<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public IsNullOrEmpty() {
		super("isNullOrEmpty");
		//, DataTypes.StringType, DataTypes.BooleanType);
	}

	//  @Override
	 public Boolean call(String field) {
		 return (field == null || ((String ) field).trim().length() == 0);
	 }

	public Object apply(R ds, String column) {
		 return call((String) getAs(ds, column));
	}

	@Override
	public Object getAs(R r, String column) {
		return null;
	}

	@Override
	public Object apply(D df, R r, String column) {
		return null;
	}

	@Override
	public Object getAs(D df, R r, String column) {
		return null;
	}
}
