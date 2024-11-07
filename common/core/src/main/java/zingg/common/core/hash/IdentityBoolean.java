package zingg.common.core.hash;

public class IdentityBoolean extends BaseHash<Boolean, Boolean>{
	
	private static final long serialVersionUID = 1L;

	public IdentityBoolean() {
	    setName("identityBoolean");
	}

	public Boolean call(Boolean field) {
		 return field;
	 }
    
}
