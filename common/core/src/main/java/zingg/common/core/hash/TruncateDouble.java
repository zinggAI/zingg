package zingg.common.core.hash;

/**
 * Base class for hash functions related to truncating of doubles
 * 
 * @author vikasgupta
 *
 */
public class TruncateDouble extends BaseHash<Double,Double>{
	private int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateDouble(int numDecimalPlaces) {
	    setName("truncateDoubleTo" + numDecimalPlaces + "Places");//, DataTypes.DoubleType, DataTypes.DoubleType, true);
		this.numDecimalPlaces = numDecimalPlaces;
	}

	
	public Double call(Double field) {
		Double r = null;
		if (field == null) {
			r = field;
		} else {
			r = Math.floor(field * POWERS_OF_10[numDecimalPlaces]) / POWERS_OF_10[numDecimalPlaces];
		}
		return r;
	}


    public int getNumDecimalPlaces() {
        return numDecimalPlaces;
    }

}
