package zingg.common.core.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ListMap;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.hash.HashFunction;

public abstract class Block<D,R,C,T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(Block.class);
	private final IHashFunctionUtility<D, R, C, T> hashFunctionUtility;

	protected ZFrame<D,R,C> dupes;
	// Class[] types;
	ListMap<T, HashFunction<D,R,C,T>> functionsMap;
	long maxSize;
	ZFrame<D,R,C> training;
	protected ListMap<HashFunction<D,R,C,T>, String> childless;

	public Block(HashUtility hashUtility) {
		HashFunctionUtilityFactory<D, R, C, T> hashFunctionUtilityFactory = new HashFunctionUtilityFactory<D, R, C, T>();
		this.hashFunctionUtility = hashFunctionUtilityFactory.getHashFunctionUtility(hashUtility);
	}

	public Block(ZFrame<D,R,C> training, ZFrame<D,R,C> dupes, HashUtility hashUtility) {
		HashFunctionUtilityFactory<D, R, C, T> hashFunctionUtilityFactory = new HashFunctionUtilityFactory<D, R, C, T>();
		this.hashFunctionUtility = hashFunctionUtilityFactory.getHashFunctionUtility(hashUtility);
		this.training = training;
		this.dupes = dupes;
		childless =  new ListMap<HashFunction<D,R,C,T>, String>();
		// types = getSampleTypes();
		/*
		 * for (Class type : types) { LOG.info("Type is " + type); }
		 */
	}

	public Block(ZFrame<D,R,C> training, ZFrame<D,R,C> dupes,
		ListMap<T, HashFunction<D, R, C, T>> functionsMap, long maxSize, HashUtility hashUtility) {
		this(training, dupes, hashUtility);
		this.functionsMap = functionsMap;
		// functionsMap.prettyPrint();
		this.maxSize = maxSize;
	}

	/**
	 * @return the dupes
	 */
	public ZFrame<D,R,C> getDupes() {
		return dupes;
	}

	/**
	 * @param dupes
	 *            the dupes to set
	 */
	public void setDupes(ZFrame<D,R,C> dupes) {
		this.dupes = dupes;
	}

	/**
	 * @return the types
	 * 
	 */

	/**
	 * @param types
	 * the types to set
	 * 
	 *           
	 * @return the maxSize
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize
	 *  the maxSize to set
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the functionsMap
	 */
	public Map<T, List<HashFunction<D,R,C,T>>> getFunctionsMap() {
		return functionsMap;
	}

	
	protected void setFunctionsMap(ListMap<T, HashFunction<D,R,C,T>> m) {
		this.functionsMap = m;
	}

	protected Canopy<R> getCanopy(){
		return new Canopy<R>();
	}
	
	public Canopy<R>getNodeFromCurrent(Canopy<R>node, HashFunction<D,R,C,T> function,
			FieldDefinition context) {
		Canopy<R>trial = getCanopy();
		trial = node.copyTo(trial);
		// node.training, node.dupeN, function, context);
		trial.function = function;
		trial.context = context;
		return trial;
	}

	public void estimateElimCount(Canopy<R> c, long elimCount) {
		c.estimateElimCount();
	}

	public Canopy<R>getBestNode(Tree<Canopy<R>> tree, Canopy<R>parent, Canopy<R>node,
			List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		int maxElimination = 0;
		Canopy<R>best = null;
		for (FieldDefinition field : fieldsOfInterest) {
			if (LOG.isDebugEnabled()){
				LOG.debug("Trying for " + field + " with data type " + field.getDataType() + " and real dt " 
					+ getFeatureFactory().getDataTypeFromString(field.getDataType()));
			}
			//Class type = FieldClass.getFieldClassClass(field.getFieldClass());
			FieldDefinition context = field;
			if (least ==0) break;//how much better can it get?
			// applicable functions
			List<HashFunction<D,R,C,T>> functions = functionsMap.get(getFeatureFactory().getDataTypeFromString(field.getDataType()));
			if (LOG.isDebugEnabled()){
				LOG.debug("functions are " + functions);
			}
			
			if (functions != null) {
				
				for (HashFunction function : functions) {
					// /if (!used.contains(field.getIndex(), function) &&
					if (least ==0) break;//how much better can it get?
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node) //&&
							//!childless.contains(function, field.fieldName)
							) 
							{
						if (LOG.isDebugEnabled()){
							LOG.debug("Evaluating field " + field.fieldName
								+ " and function " + function + " for " + field.dataType);
						}
						Canopy<R>trial = getNodeFromCurrent(node, function,
								context);
						estimateElimCount(trial, least);
						long elimCount = trial.getElimCount();

						
						//int trSize = (int) Math.ceil(0.02d * node.dupeN.count());
						//boolean isNotEliminatingMoreThan1Percent = elimCount <= trSize ? true
						//		: false;

						if (LOG.isDebugEnabled()) {
							LOG.debug("Elim Count is " + elimCount
						
								+ " ,least is "
								+ least
								//+ " , training is "
								//+ node.training
								+ ", dupe count " + node.dupeN.size());
						}
						if (least > elimCount) {
							long childrenSize = trial.estimateCanopies();
							if (childrenSize > 1) {
						
								// && isNotEliminatingMoreThan1Percent) {
								if (LOG.isDebugEnabled()) {
									LOG.debug("Yes, this fn has potential " + function);
								}
								least = elimCount;
								best = trial;
								best.elimCount = least;
								/*if (elimCount == 0) {
									LOG.debug("Out of this tyranny " + function);
									break;
								}*/
							}
							else {
								if (LOG.isDebugEnabled()){
									LOG.debug("No child " + function);
								}
								//childless.add(function, field.fieldName);
							}
							
						}
					}
				}
			}
			else {
				LOG.debug("functions are null??????");
			}
		}
		return best;

	}

	/**
	 * Holy Grail of Standalone
	 * 
	 * @param tree
	 * @param parent
	 * @param node
	 * @param used
	 * @return
	 * @throws ZinggClientException 
	 */
	public Tree<Canopy<R>> getBlockingTree(Tree<Canopy<R>> tree, Canopy<R>parent,
			Canopy<R>node, List<FieldDefinition> fieldsOfInterest) throws Exception, ZinggClientException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Tree so far ");
			LOG.debug(tree);
		}
		long size = node.getTrainingSize();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Size, maxSize " + size + ", " + maxSize);
		}
		if (size > maxSize && node.getDupeN() != null && node.getDupeN().size() > 0) {
			LOG.debug("Size is bigger ");
			Canopy<R>best = getBestNode(tree, parent, node, fieldsOfInterest);
			if (best != null) {
				//add function, context info for this best node in set
				hashFunctionUtility.addHashFunctionIfRequired(best);
				if (LOG.isDebugEnabled()) {
					LOG.debug(" HashFunction is " + best + " and node is " + node);
				}
				best.copyTo(node);
				// used.add(node.context.getOperandFields()[0],
				// best.getFunction());
				// used.add(1, best.getFunction());
				if (tree == null && parent == null) {
					tree = new Tree<Canopy<R>>(node);
				} 
				/*else {
					// /tree.addLeaf(parent, node);
					used = new ListMap<Integer, HashFunction>();
				}*/
				List<Canopy<R>> canopies = node.getCanopies();
				if (LOG.isDebugEnabled()) {
					LOG.debug(" Children size is " + canopies.size());
				}
				for (Canopy<R>n : canopies) {
					node.clearBeforeSaving();
					tree.addLeaf(node, n);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Finding for " + n);
					}
				
					getBlockingTree(tree, node, n, fieldsOfInterest);
				}
				//remove function, context info for this best node as we are returning from best node
				hashFunctionUtility.removeHashFunctionIfRequired(best);
			}
			else {
				node.clearBeforeSaving();
			}
		} else {
			if ((node.getDupeN() == null) || (node.getDupeN().size() == 0)) {
				LOG.warn("Ran out of training at size " + size + " for node " + node);
			}
			else {
				LOG.debug("Min size reached " + size + " for node " + node);
				if (tree==null) {
					throw new ZinggClientException("Unable to create Zingg models due to insufficient data. Please run Zingg after adding more data");
				}
			}
			// tree.addLeaf(parent, node);
			node.clearBeforeSaving();
		}

		return tree;
	}

//	public boolean isFunctionUsed(FieldDefinition fieldDefinition, HashFunction<D, R, C, T> function) {
//		return hashFunctionsInCurrentNodePath.contains(getKey(fieldDefinition, function));
//	}

	public List<Canopy<R>> getHashSuccessors(Collection<Canopy<R>> successors, Object hash) {
		List<Canopy<R>> retCanopy = new ArrayList<Canopy<R>>();
		for (Canopy<R>c: successors) {
			if (hash == null && c!= null && c.getHash() == null) retCanopy.add(c);
			if (c!= null && c.getHash() != null && c.getHash().equals(hash)) {
				retCanopy.add(c);
			}
		}
		return retCanopy;
	}

	/*public static StringBuilder applyTree(Row tuple, Tree<Canopy<R>> tree,
			Canopy<R>root, StringBuilder result) {
		LOG.debug("Applying root " + root + " on " + tuple);
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			LOG.debug("Applied root " + root + " and got " + hash);
			result = result.append("|").append(hash);
			for (Canopy<R>c : getHashSuccessors(tree.getSuccessors(root), hash)) {
				// LOG.info("Successr hash " + c.getHash() + " and our hash "+
				// hash);
				if (c != null) {
					// //LOG.debug("c.hash " + c.getHash() + " and our hash " + hash);
					if ((c.getHash() != null)) {
						//LOG.debug("Hurdle one over ");
						//if ((c.getHash().equals(hash))) {
							// //LOG.debug("Hurdle 2 start " + c);
							applyTree(tuple, tree, c, result);
							// //LOG.debug("Hurdle 2 over ");
						//}
					}
				}
			}
		}
		return result;
	}*/
	
	public static <R> StringBuilder applyTree(R tuple, Tree<Canopy<R>> tree,
			Canopy<R>root, StringBuilder result) {
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			
			result = result.append("|").append(hash);
			for (Canopy<R>c : tree.getSuccessors(root)) {
				// LOG.info("Successr hash " + c.getHash() + " and our hash "+
				// hash);
				if (c != null) {
					// //LOG.debug("c.hash " + c.getHash() + " and our hash " + hash);
					if ((c.getHash() != null)) {
						//LOG.debug("Hurdle one over ");
						if ((c.getHash().equals(hash))) {
							// //LOG.debug("Hurdle 2 start " + c);
							applyTree(tuple, tree, c, result);
							// //LOG.debug("Hurdle 2 over ");
						}
					}
				}
			}
		}
		//LOG.debug("apply first step clustering result " + result);
		return result;
	}

	public void printTree(Tree<Canopy<R>> tree,
			Canopy<R>root) {
		if (root.dupeN != null) {
			LOG.info(" dupeN not null " + root);
			LOG.info(root.dupeN.size());
		}
		
		if (root.dupeRemaining != null) {
			LOG.info(" dupeRemaining not null " + root);
			LOG.info(root.dupeRemaining.size());
		}
		
		if (root.training != null) {
			LOG.info(" training not null " + root);
			LOG.info(root.training.size());
		}
		for (Canopy<R>c : tree.getSuccessors(root)) {
			printTree(tree, c);
		}			
	}

	public abstract FeatureFactory<T> getFeatureFactory();
	
	
}

