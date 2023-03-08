package zingg.common.core.hash;

/**
 * Base class for hash functions related to trimming of doubles
 *
 */
public class TrimLastDigitsDbl extends BaseHash<Double,Double>{
	private int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsDbl(int count) {
	    setName("trimLast" + count + "DigitsDbl");
		this.numDigits = count;
	}

	
	public Double call(Double field) {
		Double r = null;
		if (field == null) {
			r = field;
		} else {
			r = Math.floor(field / POWERS_OF_10[numDigits]);
		}
		return r;
	}


    public int getNumDigits() {
        return numDigits;
    }
	
}
