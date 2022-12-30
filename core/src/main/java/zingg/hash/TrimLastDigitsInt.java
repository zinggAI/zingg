package zingg.hash;

/**
 * Base class for hash functions related to trimming of integers
 * @author vikasgupta
 *
 * @param <D>
 * @param <R>
 * @param <C>
 * @param <T>
 */
public abstract class TrimLastDigitsInt<D,R,C,T> extends HashFunction<D,R,C,T> {
	int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsInt(int count) {
		super("trimLast" + count + "DigitsInt");//, DataTypes.IntegerType, DataTypes.IntegerType, true);
		this.numDigits = count;
	}

	public Integer call(Integer field) {
		Integer r = null;
		if (field == null) {
			r = field;
		} else {
			r = field / POWERS_OF_10[numDigits];
		}
		return r;
	}


}
