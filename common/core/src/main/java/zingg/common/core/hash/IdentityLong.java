package zingg.common.core.hash;


public class IdentityLong extends BaseHash<Long,Long>{
	
	private static final long serialVersionUID = 1L;

	public IdentityLong() {
	    setName("identityLong");
	}

	public Long call(Long field) {
		 return field;
	 }

}
