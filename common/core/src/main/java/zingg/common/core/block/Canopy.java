package zingg.common.core.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ListMap;
import zingg.common.core.hash.HashFunction;

public class Canopy<R> implements Serializable {

	private static final long serialVersionUID = -229533781044789499L;

	public static final Log LOG = LogFactory.getLog(Canopy.class);

	// created by function edge leading from parent to this node
	protected HashFunction function;
	// aplied on field
	protected FieldDefinition context;
	// list of duplicates passed from parent
	protected List<R> dupeN;
	// number of duplicates eliminated after function applied on fn context
	protected long elimCount;
	// hash of canopy
	protected Object hash;
	// training set
	protected List<R> training;
	// duplicates remaining after function is applied
	protected List<R> dupeRemaining;

	public Canopy() {
	}

	public Canopy(List<R> training, List<R> dupeN) {
		this.training = training;
		this.dupeN = dupeN;
	}

	public Canopy(List<R> training, List<R> dupeN, HashFunction function,
				  FieldDefinition context) {
		this(training, dupeN);
		this.function = function;
		this.context = context;
	}

	public HashFunction getFunction() {
		return function;
	}

	public void setFunction(HashFunction function) {
		this.function = function;
	}

	public FieldDefinition getContext() {
		return context;
	}

	public void setContext(FieldDefinition context) {
		this.context = context;
	}

	public List<R> getDupeN() {
		return dupeN;
	}

	public void setDupeN(List<R> dupeN) {
		this.dupeN = dupeN;
	}

	public long getElimCount() {
		return elimCount;
	}

	public void setElimCount(long elimCount) {
		this.elimCount = elimCount;
	}

	public Object getHash() {
		return hash;
	}

	public void setHash(Object hash) {
		this.hash = hash;
	}

	public List<R> getTraining() {
		return training;
	}

	public void setTraining(List<R> training) {
		this.training = training;
	}

	public List<Canopy<R>> getCanopies() {
		ListMap<Object, R> hashes = new ListMap<Object, R>();
		List<Canopy<R>> returnCanopies = new ArrayList<Canopy<R>>();
		for (R r : training) {
			hashes.add(function.apply(r, context.fieldName), r);
		}
		for (Object o : hashes.keySet()) {
			Canopy<R> can = new Canopy<R>(hashes.get(o), dupeRemaining);
			can.hash = o;
			returnCanopies.add(can);
		}
		hashes = null;
		return returnCanopies;
	}

	public long estimateCanopies() {
		Set<Object> hashes = new HashSet<Object>();
		for (R r : training) {
			hashes.add(function.apply(r, context.fieldName));
		}
		long uniqueHashes = hashes.size();
		LOG.debug("estimateCanopies- unique hash count is " + uniqueHashes);
		return uniqueHashes;
	}

	public long getTrainingSize() {
		return training.size();
	}

	@Override
	public String toString() {
		String str = "";
		if (context != null) {
			str = "Canopy [function=" + function + ", context=" + context.fieldName
					+ ", elimCount=" + elimCount + ", hash=" + hash;
		} else {
			str = "Canopy [function=" + function + ", context=" + context
					+ ", elimCount=" + elimCount + ", hash=" + hash;
		}
		if (training != null) {
			str += ", training=" + training.size();
		}
		str += "]";
		return str;
	}

	public void estimateElimCount() { // O(pairs)
		LOG.debug("Applying " + function.getName());
		dupeRemaining = new ArrayList<R>();
		for (R r : dupeN) {
			Object hash1 = function.apply(r, context.fieldName);
			Object hash2 = function.apply(r, ColName.COL_PREFIX + context.fieldName);
			LOG.debug("hash1 " + hash1);
			LOG.debug("hash2 " + hash2);
			if (hash1 == null && hash2 == null) {
				dupeRemaining.add(r);
			} else if (hash1 != null && hash2 != null && hash1.equals(hash2)) {
				dupeRemaining.add(r);
				LOG.debug("NOT eliminating ");
			} else {
				LOG.debug("eliminating " + r);
			}
		}
		elimCount = dupeN.size() - dupeRemaining.size();
	}

	// Original row extraction via function.apply, but exits early when count exceeds limit.
	// No ArrayList allocation — used to isolate early-exit contribution without precomputed values.
	public long countEliminationsWithRowExtraction(long limit) {
		long count = 0;
		for (R r : dupeN) {
			Object hash1 = function.apply(r, context.fieldName);
			Object hash2 = function.apply(r, ColName.COL_PREFIX + context.fieldName);
			boolean same = (hash1 == null && hash2 == null) ||
					(hash1 != null && hash2 != null && hash1.equals(hash2));
			if (!same && ++count > limit) return count;
		}
		elimCount = count;
		return count;
	}

	// Count eliminations using precomputed raw field values; exits early once count
	// exceeds limit. No ArrayList allocation — called for every candidate in getBestNode.
	public long countEliminationsWithValues(Object[] vals1, Object[] vals2, long limit) {
		long count = 0;
		for (int i = 0; i < dupeN.size(); i++) {
			Object hash1 = function.applyToValue(vals1[i]);
			Object hash2 = function.applyToValue(vals2[i]);
			boolean same = (hash1 == null && hash2 == null) ||
					(hash1 != null && hash2 != null && hash1.equals(hash2));
			if (!same && ++count > limit) return count;
		}
		return count;
	}

	// Build dupeRemaining for the winning candidate only — called once per getBestNode.
	public void buildDupeRemaining() {
		dupeRemaining = new ArrayList<>(dupeN.size());
		for (R r : dupeN) {
			Object hash1 = function.apply(r, context.fieldName);
			Object hash2 = function.apply(r, ColName.COL_PREFIX + context.fieldName);
			if ((hash1 == null && hash2 == null) ||
					(hash1 != null && hash2 != null && hash1.equals(hash2))) {
				dupeRemaining.add(r);
			}
		}
		elimCount = dupeN.size() - dupeRemaining.size();
	}

	public Canopy copyTo(Canopy copyTo) {
		copyTo.function = function;
		copyTo.context = context;
		copyTo.dupeN = dupeN;
		copyTo.elimCount = elimCount;
		copyTo.hash = hash;
		copyTo.training = training;
		copyTo.dupeRemaining = dupeRemaining;
		return copyTo;
	}

	public void clearBeforeSaving() {
		this.training = null;
		this.dupeN = null;
		this.dupeRemaining = null;
	}

}
