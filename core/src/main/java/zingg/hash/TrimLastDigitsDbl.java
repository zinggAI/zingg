package zingg.hash;

/**
 * Base class for hash functions related to trimming of doubles
 * @author vikasgupta
 *
 * @param <D>
 * @param <R>
 * @param <C>
 * @param <T>
 */
public abstract class TrimLastDigitsDbl<D,R,C,T> extends HashFunction<D,R,C,T> {
	int numDigits;
	static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000};
	public TrimLastDigitsDbl(int count) {
		super("trimLast" + count + "DigitsDbl");
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
	
}
