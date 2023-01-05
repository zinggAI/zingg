package zingg.hash;

public class RangeInt extends BaseHash<Integer,Integer>{
	private int lowerLimit;
	private int upperLimit;

	public RangeInt(int lower, int upper) {
	    setName("rangeBetween" + lower + "And" + upper + "Int");//, DataTypes.IntegerType, DataTypes.IntegerType, true);
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


    public int getLowerLimit() {
        return lowerLimit;
    }


    public int getUpperLimit() {
        return upperLimit;
    }

}
