package zingg.common.core.hash;


public class IdentityString extends BaseHash<String,String>{
	
	public IdentityString() {
	    setName("identityString");
	}

	public String call(String field) {
		 if (field == null) return field;
		 field = field.trim(); //.toLowerCase();
		 return field;
	}

}
