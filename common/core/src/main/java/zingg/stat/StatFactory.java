package zingg.stat;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.MatchType;

public class StatFactory {

	public static final Log LOG = LogFactory.getLog(StatFactory.class);

	private static HashMap<Class, Class<? extends Stat>> stats;

	private static void init() {
		stats = new HashMap<Class, Class<? extends Stat>>();
		stats.put(java.lang.String.class, StringStat.class);
		stats.put(java.lang.Integer.class, IntStat.class);
	}

	public static <E> Stat<E> getStat(MatchType f, Class<E> e) throws Exception {
		if (stats == null)
			init();
		Stat s = (Stat<E>) stats.get(e).newInstance();
		s.setFieldType(f);
		return s;
	}
}
