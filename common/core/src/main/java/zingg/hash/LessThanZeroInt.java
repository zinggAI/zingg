package zingg.hash;

public class LessThanZeroInt extends BaseHash<Integer,Boolean>{
	public LessThanZeroInt() {
	    setName("lessThanZeroInt");
	}
	
	public Boolean call(Integer field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
