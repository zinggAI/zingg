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

}
