package zingg.hash;


public abstract class IdentityString<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public IdentityString() {
		super("identityString");
		//, DataTypes.StringType, DataTypes.StringType);
	}

	 public String call(String field) {
		 if (field == null) return field;
		 field = field.trim().toLowerCase();
		 return field;
	 }

	@Override
	public Object apply(R ds, String column) {
		 return call((String) getAs(ds, column));
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
	public Object getAs(R r, String column) {
		return null;
	}
}
