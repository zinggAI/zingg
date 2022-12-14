package zingg.hash;


public abstract class IdentityInteger<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public IdentityInteger() {
		super("identityInteger");
		//, DataTypes.IntegerType, DataTypes.IntegerType);
	}

	public Integer call(Integer field) {
		 return field;
	 }

	@Override
	public Object apply(R ds, String column) {
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

	@Override
	public Object getAs(R r, String column) {
		return null;
	}


}
