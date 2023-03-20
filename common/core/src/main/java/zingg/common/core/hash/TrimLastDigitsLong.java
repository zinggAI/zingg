package zingg.common.core.hash;

/**
 * Base class for hash functions related to trimming of longs
 *
 */
public class TrimLastDigitsLong extends BaseHash<Long,Long>{
	private static final long serialVersionUID = 1L;
	private int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsLong(int count) {
	    setName("trimLast" + count + "DigitsLong");
		this.numDigits = count;
	}

	public Long call(Long field) {
		Long r = null;
		if (field == null) {
			r = field;
		} else {
			r = field / POWERS_OF_10[numDigits];
		}
		return r;
	}

    public int getNumDigits() {
        return numDigits;
    }
	
}
