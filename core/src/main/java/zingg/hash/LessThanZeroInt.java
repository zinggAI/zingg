package zingg.hash;

public abstract class LessThanZeroInt<D,R,C,T> extends HashFunction<D,R,C,T> {
	public LessThanZeroInt() {
		super("lessThanZeroInt");//, DataTypes.IntegerType, DataTypes.BooleanType, true);
	}
	
	public Boolean call(Integer field) {
		Boolean r = false;
		if (field != null) {
			r = field < 0 ? true : false;
		}
		return r;
	}

}
