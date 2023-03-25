package zingg.common.core.hash;

public class RangeLong extends BaseHash<Long,Long>{
	private static final long serialVersionUID = 1L;
	private long lowerLimit;
	private long upperLimit;

	public RangeLong(long lower, long upper) {
	    setName("rangeBetween" + lower + "And" + upper + "Long");
		this.lowerLimit = lower;
		this.upperLimit = upper;
	}

	
	public Long call(Long field) {
		long withinRange = 0;
		if (field != null && field >= lowerLimit && field < upperLimit) {
			withinRange = 1;
		}
		return withinRange;
	}


    public long getLowerLimit() {
        return lowerLimit;
    }


    public long getUpperLimit() {
        return upperLimit;
    }

}
