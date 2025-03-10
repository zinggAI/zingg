package zingg.common.core.hash;


public class IdentityString extends BaseHash<String,String>{
	
	public IdentityString() {
	    setName("identityString");
	}

	public String call(String field) {
		return field;
	}

}
