package zingg.common.client.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ListMap<A, B> extends HashMap<A, List<B>> implements Serializable {
	public static final Log LOG = LogFactory.getLog(ListMap.class);

	public void add(A a, B b) {
		List<B> listB = null;
		if (containsKey(a)) {
			listB = get(a);
		} else {
			listB = new ArrayList<B>();
		}
		listB.add(b);
		put(a, listB);
	}

	public boolean contains(A a, B b) {
		boolean contains = false;
		if (containsKey(a)) {
			List<B> listB = get(a);
			contains = listB.contains(b);
		}
		return contains;
	}

	public int totalSize() {
		int count = 0;
		for (A a : this.keySet()) {
			count += get(a).size();
		}
		return count;
	}

	public void prettyPrint() {
		LOG.info("Printing ");
		for (A a : this.keySet()) {
			LOG.info("For key " + a);
			for (B b : get(a)) {
				LOG.info("List contains " + b);
			}
		}
	}
}
