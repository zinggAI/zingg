package zingg.common.core.hash;

/**
 * Base class for hash functions related to trimming of floats
 *
 */
public class TrimLastDigitsFloat extends BaseHash<Float,Float>{
	private static final long serialVersionUID = 1L;
	private int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsFloat(int count) {
	    setName("trimLast" + count + "DigitsFloat");
		this.numDigits = count;
	}

	
	public Float call(Float field) {
		Float r = null;
		if (field == null) {
			r = field;
		} else {
			r = (float)(Math.floor(field / POWERS_OF_10[numDigits]));
		}
		return r;
	}


    public int getNumDigits() {
        return numDigits;
    }
	
}
