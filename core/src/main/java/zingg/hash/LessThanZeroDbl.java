package zingg.hash;

public abstract class LessThanZeroDbl<D,R,C,T> extends HashFunction<D,R,C,T> {

	public LessThanZeroDbl() {
		super("lessThanZeroDbl");
	}

	
	public Boolean call(Double field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
