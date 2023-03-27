package zingg.common.core.hash;

public class LessThanZeroLong extends BaseHash<Long,Boolean>{
	private static final long serialVersionUID = 1L;

	public LessThanZeroLong() {
	    setName("lessThanZeroLong");
	}
	
	public Boolean call(Long field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
