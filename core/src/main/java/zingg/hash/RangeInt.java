package zingg.hash;

public abstract class RangeInt<D,R,C,T> extends HashFunction<D,R,C,T>{
	int lowerLimit;
	int upperLimit;

	public RangeInt(int lower, int upper) {
		super("rangeBetween" + lower + "And" + upper + "Int");//, DataTypes.IntegerType, DataTypes.IntegerType, true);
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Integer call(Integer field) {
		int withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}

}
