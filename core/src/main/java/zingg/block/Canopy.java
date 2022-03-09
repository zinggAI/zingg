package zingg.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Row;

import zingg.client.FieldDefinition;
import zingg.client.util.ListMap;
import zingg.hash.HashFunction;
import zingg.client.util.ColName;


public class Canopy implements Serializable {

	public static final Log LOG = LogFactory.getLog(Canopy.class);

	// created by function edge leading from parent to this node
	HashFunction function;
	// aplied on field
	FieldDefinition context;
	// list of duplicates passed from parent
	List<Row> dupeN;
	// number of duplicates eliminated after function applied on fn context
	long elimCount;
	// hash of canopy
	Object hash;
	// training set
	List<Row> training;
	// duplicates remaining after function is applied
	List<Row> dupeRemaining;

	public Canopy() {
	}

	public Canopy(List<Row> training, List<Row> dupeN) {
		this.training = training; //.cacheResult();
		this.dupeN = dupeN;
	}

	public Canopy(List<Row> training, List<Row> dupeN, HashFunction function,
			FieldDefinition context) {
		this(training, dupeN);
		this.function = function;
		this.context = context;
		// prepare();
	}

	/**
	 * @return the function
	 */
	public HashFunction getFunction() {
		return function;
	}

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(HashFunction function) {
		this.function = function;
	}

	/**
	 * @return the context
	 */
	public FieldDefinition getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(FieldDefinition context) {
		this.context = context;
	}

	

	/**
	 * @return the dupeN
	 */
	public List<Row> getDupeN() {
		return dupeN;
	}

	/**
	 * @param dupeN
	 *            the dupeN to set
	 */
	public void setDupeN(List<Row> dupeN) {
		this.dupeN = dupeN;
	}

	/**
	 * @return the elimCount
	 */
	public long getElimCount() {
		return elimCount;
	}

	/**
	 * @param elimCount
	 *            the elimCount to set
	 */
	public void setElimCount(long elimCount) {
		this.elimCount = elimCount;
	}

	/**
	 * @return the hash
	 */
	public Object getHash() {
		return hash;
	}

	/**
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(Object hash) {
		this.hash = hash;
	}

	/**
	 * @return the training
	 */
	public List<Row> getTraining() {
		return training;
	}

	/**
	 * @param training
	 *            the training to set
	 */
	public void setTraining(List<Row> training) {
		this.training = training;
	}

	public List<Canopy> getCanopies() {
		//long ts = System.currentTimeMillis();
		/*
		List<Row> newTraining = function.apply(training, context.fieldName, ColName.HASH_COL).cacheResult();
		LOG.debug("getCanopies0" + (System.currentTimeMillis() - ts));
		List<Canopy> returnCanopies = new ArrayList<Canopy>();
		//first find unique hashes
		//then split the training into per hash
		List<Row> uniqueHashes = newTraining.select(ColName.HASH_COL).distinct().collectAsList();
		LOG.debug("getCanopies1" + (System.currentTimeMillis() - ts));
		for (Row row : uniqueHashes) {
			Object key = row.get(0);
			List<Row> tupleList = newTraining.filter(newTraining.col(ColName.HASH_COL).equalTo(key))
					.cacheResult();
			tupleList = tupleList.drop(ColName.HASH_COL);
			Canopy can = new Canopy(tupleList, dupeRemaining);
			//LOG.debug(" canopy size is " + tupleList.count() + " for  hash "
			//		+ key);
			can.hash = key;
			returnCanopies.add(can);
		}
		LOG.debug("getCanopies2" + (System.currentTimeMillis() - ts));
		return returnCanopies;*/
		ListMap<Object, Row> hashes = new ListMap<Object, Row>();
		List<Canopy> returnCanopies = new ArrayList<Canopy>();
		
		for (Row r : training) {
			hashes.add(function.apply(r, context.fieldName), r);
		}
		for (Object o: hashes.keySet()) {
			Canopy can = new Canopy(hashes.get(o), dupeRemaining);
			can.hash = o;
			returnCanopies.add(can);
		}
		hashes = null;
		//LOG.debug("getCanopies2" + (System.currentTimeMillis() - ts));
		return returnCanopies;
	}
	
	public long estimateCanopies() {
		//long ts = System.currentTimeMillis();	
		Set<Object> hashes = new HashSet<Object>();
		for (Row r : training) {
			hashes.add(function.apply(r, context.fieldName));
		}
		/*
		List<Row> newTraining = function.apply(training, context.fieldName, ColName.HASH_COL);
		long uniqueHashes = (long) newTraining.select(functions.approxCountDistinct(
				newTraining.col(ColName.HASH_COL))).takeAsList(1).get(0).get(0);
		LOG.debug("estimateCanopies" + (System.currentTimeMillis() - ts) + " and count is " + uniqueHashes);
		/*newTraining.agg(
				functions.approxCountDistinct(newTraining.col(ColName.HASH_COL))).show();
		long ts1 = System.currentTimeMillis();
		long uniqueHashes = newTraining.select(newTraining.col(ColName.HASH_COL)).distinct().count(); //.distinct().count();
		LOG.warn("estimateCanopies" + (System.currentTimeMillis() - ts1) + " and count is " + uniqueHashes);
*/
		long uniqueHashes = hashes.size();
		LOG.debug("estimateCanopies- unique hash count is " + uniqueHashes);

		return uniqueHashes;
	}

	public long getTrainingSize() {
		return training.size(); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";
		if (context != null) {
			str = "Canopy [function=" + function + ", context=" + context.fieldName
				+ ", elimCount=" + elimCount + ", hash=" + hash;
		}
		else {
			str = "Canopy [function=" + function + ", context=" + context
				+ ", elimCount=" + elimCount + ", hash=" + hash;
		}
		if (training != null) {
			str += ", training=" + training.size();
		}
		str += "]";
		return str;
	}

	

	public void estimateElimCount() {
		//long ts = System.currentTimeMillis();																																																																				
		//the function is applied to both columns
		//if hash is equal, they are not going to be eliminated
		//filter on hash equal and count 
		LOG.debug("Applying " + function.getName());
		dupeRemaining = new ArrayList<Row>();
		for(Row r: dupeN) {
			Object hash1 = function.apply(r, context.fieldName);
			Object hash2 = function.apply(r, ColName.COL_PREFIX + context.fieldName);
			LOG.debug("hash1 " + hash1);		
			LOG.debug("hash2 " + hash2);
			if (hash1 == null && hash2 ==null) {
				dupeRemaining.add(r);
			}
			else if (hash1 != null && hash2 != null && hash1.equals(hash2)) {
				dupeRemaining.add(r);
				LOG.debug("NOT eliminatin " );	
			}
			else {
				LOG.debug("eliminatin " + r);		
			}
		}			
		elimCount = dupeN.size() - dupeRemaining.size();
		//LOG.debug("estimateElimCount" + (System.currentTimeMillis() - ts));
	}
	
	/*public ListMap<Object, Row> getHashForDupes(List<Row> d) {
		//dupeRemaining = new ArrayList<Row>();
		ListMap<Object, Row> returnMap = new ListMap<Object, Row>();
		for (Row pair : d) {
			Tuple first = pair.getFirst();
			Tuple second = pair.getSecond();
			Object hash1 = apply(first);
			Object hash2 = apply(second);
			if (hash1 == null) {
				if (hash2 != null) {
					//do nothing
				}
			} else {
				if (!hash1.equals(hash2)) {
					// LOG.info("Elimniation of " + pair);
					//do nothing
				} else {
					returnMap.add(hash1, pair);
				}
			}
		}
		return returnMap;
	}*/

	public Canopy copyTo(Canopy copyTo) {
		copyTo.function = function;
		copyTo.context = context;
		// list of duplicates passed from parent
		copyTo.dupeN = dupeN;
		// number of duplicates eliminated after function applied on fn context
		copyTo.elimCount = elimCount;
		// hash of canopy
		copyTo.hash = hash;
		// training set
		copyTo.training = training;
		// duplicates remaining after function is applied
		copyTo.dupeRemaining = dupeRemaining;
		return copyTo;
	}

	/**
	 * We will call this canopy's clear function to remove dupes, training and
	 * remaining data before we persist to disk this method is to be called just
	 * before
	 */
	public void clearBeforeSaving() {
		this.training = null;
		// this.elimCount = null;
		this.dupeN = null;
		this.dupeRemaining = null;
	}

}
