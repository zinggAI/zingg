package zingg.common.core.hash;

/**
 * Base class for hash functions related to truncating of floats
 * 
 *
 */
public class TruncateFloat extends BaseHash<Float,Float>{
	private static final long serialVersionUID = 1L;
	private int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateFloat(int numDecimalPlaces) {
	    setName("truncateFloatTo" + numDecimalPlaces + "Places");
		this.numDecimalPlaces = numDecimalPlaces;
	}

	
	public Float call(Float field) {
		Float r = null;
		if (field == null) {
			r = field;
		} else {
			r = (float)(Math.floor(field * POWERS_OF_10[numDecimalPlaces]) / POWERS_OF_10[numDecimalPlaces]);
		}
		return r;
	}


    public int getNumDecimalPlaces() {
        return numDecimalPlaces;
    }

}
