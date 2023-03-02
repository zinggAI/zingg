package zingg.common.core.hash;


public class IsNullOrEmpty extends BaseHash<String,Boolean>{
	
	public IsNullOrEmpty() {
	    setName("isNullOrEmpty");
	}


	public Boolean call(String field) {
		 return (field == null || ((String ) field).trim().length() == 0);
	}

}
