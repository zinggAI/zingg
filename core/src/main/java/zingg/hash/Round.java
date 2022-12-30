package zingg.hash;



public abstract class Round<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public Round() {
		super("round");
		//, DataTypes.DoubleType, DataTypes.LongType);
	}
	

	
	 public Long call(Double field) {
		 return field == null ? null : Math.round(field);
	 }

}


