package zingg.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.MatchType;

import com.google.common.collect.Ordering;

public class IntStat extends BaseStat<Integer> {

	public static final Log LOG = LogFactory.getLog(IntStat.class);

	public IntStat() {
		this(null, Ordering.natural());
	}

	public IntStat(Ordering comp) {
		this(null, comp);
	}

	public IntStat(MatchType f, Ordering comp) {
		super(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, f, comp);
	}

}
