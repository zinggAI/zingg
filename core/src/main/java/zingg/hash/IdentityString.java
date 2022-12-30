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

}
