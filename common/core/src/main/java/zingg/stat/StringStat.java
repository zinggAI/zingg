package zingg.stat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.MatchType;

import com.google.common.collect.Ordering;

public class StringStat extends BaseStat<String> {

	public static final Log LOG = LogFactory.getLog(StringStat.class);

	public StringStat() {
		this(Ordering.natural());
	}

	public StringStat(Ordering ordering) {
		this(null, ordering);
	}

	public StringStat(MatchType f, Ordering ordering) {
		super(0, "", "", 0, f, ordering);
	}

}
