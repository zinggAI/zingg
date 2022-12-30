package zingg.hash;

/**
 * Base class for hash functions related to truncating of doubles
 * 
 * @author vikasgupta
 *
 * @param <D>
 * @param <R>
 * @param <C>
 * @param <T>
 */
public abstract class TruncateDouble<D,R,C,T> extends HashFunction<D,R,C,T> {
	int numDecimalPlaces;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TruncateDouble(int numDecimalPlaces) {
		super("truncateDoubleTo" + numDecimalPlaces + "Places");//, DataTypes.DoubleType, DataTypes.DoubleType, true);
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

}
