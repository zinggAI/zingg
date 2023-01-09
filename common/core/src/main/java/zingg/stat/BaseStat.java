package zingg.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.MatchType;

import com.google.common.collect.Ordering;

public abstract class BaseStat<E> implements Stat<E> {
	public static final Log LOG = LogFactory.getLog(BaseStat.class);

	public int numberOfNulls;
	public E min;
	public E max;
	public int length;
	public Ordering<E> comparator;
	public MatchType fieldType;

	public BaseStat(int numberOfNulls, E min, E max, int length, MatchType f,
			Ordering<E> comp) {
		super();
		this.numberOfNulls = numberOfNulls;
		this.min = min;
		this.max = max;
		this.length = length;
		this.fieldType = f;
		this.comparator = comp;
	}

	/**
	 * @return the numberOfNulls
	 */
	public int getNumberOfNulls() {
		return numberOfNulls;
	}

	/**
	 * @param numberOfNulls
	 *            the numberOfNulls to set
	 */
	public void setNumberOfNulls(int numberOfNulls) {
		this.numberOfNulls = numberOfNulls;
	}

	/**
	 * @return the min
	 */
	public E getMin() {
		return min;
	}

	/**
	 * @param min
	 *            the min to set
	 */
	public void setMin(E min) {
		this.min = min;
	}

	/**
	 * @return the max
	 */
	public E getMax() {
		return max;
	}

	/**
	 * @param max
	 *            the max to set
	 */
	public void setMax(E max) {
		this.max = max;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * @return the comp
	 */
	public Ordering<E> getComparator() {
		return comparator;
	}

	/**
	 * @param comp
	 *            the comp to set
	 */
	public void setComparator(Ordering<E> comp) {
		this.comparator = comp;
	}

	public MatchType getFieldType() {
		return fieldType;
	}

	public void setFieldType(MatchType f) {
		this.fieldType = f;
	}

	public void statVisit(E a) {
		if (a == null) {
			numberOfNulls++;
		} else {
			max = comparator.max(a, max);
			min = comparator.min(a, min);
		}
	}

}
