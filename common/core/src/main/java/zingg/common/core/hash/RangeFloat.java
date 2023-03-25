package zingg.common.core.hash;

public class RangeFloat extends BaseHash<Float,Integer>{
	private static final long serialVersionUID = 1L;
	private int lowerLimit;
	private int upperLimit;

	public RangeFloat(int lower, int upper) {
	    setName("rangeBetween" + lower + "And" + upper + "Float");
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Integer call(Float field) {
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
