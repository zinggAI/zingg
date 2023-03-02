package zingg.common.core.hash;

public class RangeDbl extends BaseHash<Double,Integer>{
	private int lowerLimit;
	private int upperLimit;

	public RangeDbl(int lower, int upper) {
	    setName("rangeBetween" + lower + "And" + upper + "Dbl");
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


    public int getLowerLimit() {
        return lowerLimit;
    }


    public int getUpperLimit() {
        return upperLimit;
    }	

}
