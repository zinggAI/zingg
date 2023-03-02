package zingg.common.core.hash;

public class LessThanZeroDbl extends BaseHash<Double,Boolean>{

	public LessThanZeroDbl() {
	    setName("lessThanZeroDbl");
	}

	
	public Boolean call(Double field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
