package zingg.hash;


public abstract class IdentityInteger<D,R,C,T> extends HashFunction<D,R,C,T>{
	
	public IdentityInteger() {
		super("identityInteger");
		//, DataTypes.IntegerType, DataTypes.IntegerType);
	}

	public Integer call(Integer field) {
		 return field;
	 }

}
