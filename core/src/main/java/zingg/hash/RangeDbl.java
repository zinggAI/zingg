package zingg.hash;

public abstract class RangeDbl<D,R,C,T> extends HashFunction<D,R,C,T> {
	int lowerLimit;
	int upperLimit;

	public RangeDbl(int lower, int upper) {
		super("rangeBetween" + lower + "And" + upper + "Dbl");//, DataTypes.DoubleType, DataTypes.IntegerType, true);
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Integer call(Double field) {
		int withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}

}
