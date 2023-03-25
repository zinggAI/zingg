package zingg.common.core.hash;

public class LessThanZeroFloat extends BaseHash<Float,Boolean>{

	private static final long serialVersionUID = 1L;


	public LessThanZeroFloat() {
	    setName("lessThanZeroFloat");
	}

	
	public Boolean call(Float field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
