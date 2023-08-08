/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

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
