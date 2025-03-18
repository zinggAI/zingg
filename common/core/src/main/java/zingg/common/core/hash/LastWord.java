package zingg.common.core.hash;


public class LastWord extends BaseHash<String,String>{

	public LastWord() {
	    setName("lastWord");		
	}
			
	public String call(String field) {
		String r = null;
		if (field == null || field.isEmpty() ) {
			r = field;
		}
		else {
			String[] v = field.split(" ");
			if(v.length != 0){
				return v[v.length-1];
			}
		}
		return r;
	}
	
}
