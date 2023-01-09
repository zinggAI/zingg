package zingg.hash;


public class IdentityInteger extends BaseHash<Integer,Integer>{
	
	public IdentityInteger() {
	    setName("identityInteger");
	}

	public Integer call(Integer field) {
		 return field;
	 }

}
