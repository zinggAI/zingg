package zingg.hash;



public abstract class Round<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public Round() {
		super("round");
		//, DataTypes.DoubleType, DataTypes.LongType);
	}
	

	//  @Override
	 public Long call(Double field) {
		 return field == null ? null : Math.round(field);
	 }

	 @Override
	 public Object apply(R ds, String column) {
		 return call((Double) getAs(ds, column));
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


