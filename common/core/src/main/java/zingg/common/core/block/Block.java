package zingg.common.core.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.ListMap;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.hash.HashFunction;

public abstract class Block<D, R, C, T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(Block.class);
	private final IHashFunctionUtility<D, R, C, T> hashFunctionUtility;
	private FieldDefinitionStrategy<R> fieldDefinitionStrategy;

	protected ZFrame<D, R, C> dupes;
	ListMap<T, HashFunction<D, R, C, T>> functionsMap;
	long maxSize;
	ZFrame<D, R, C> training;
	protected ListMap<HashFunction<D, R, C, T>, String> childless;

	public Block() {
		this.hashFunctionUtility = HashFunctionUtilityFactory.getHashFunctionUtility(HashUtility.CACHED);
	}

	public Block(ZFrame<D, R, C> training, ZFrame<D, R, C> dupes) {
		this();
		this.training = training;
		this.dupes = dupes;
		childless = new ListMap<HashFunction<D, R, C, T>, String>();
	}

	public Block(ZFrame<D, R, C> training, ZFrame<D, R, C> dupes,
				 ListMap<T, HashFunction<D, R, C, T>> functionsMap, long maxSize, FieldDefinitionStrategy<R> fieldDefinitionStrategy) {
		this(training, dupes);
		this.functionsMap = functionsMap;
		this.maxSize = maxSize;
		this.fieldDefinitionStrategy = fieldDefinitionStrategy;
	}

	@SuppressWarnings("unchecked")
	public Block(ZFrame<D, R, C> training, ZFrame<D, R, C> dupes,
				 ListMap<T, HashFunction<D, R, C, T>> functionsMap, long maxSize,
				 FieldDefinitionStrategy<R> fieldDefinitionStrategy, HashUtility hashUtility) {
		this.hashFunctionUtility = HashFunctionUtilityFactory.getHashFunctionUtility(hashUtility);
		this.training = training;
		this.dupes = dupes;
		this.childless = new ListMap<HashFunction<D, R, C, T>, String>();
		this.functionsMap = functionsMap;
		this.maxSize = maxSize;
		this.fieldDefinitionStrategy = fieldDefinitionStrategy;
	}

	public ZFrame<D, R, C> getDupes() {
		return dupes;
	}

	public void setDupes(ZFrame<D, R, C> dupes) {
		this.dupes = dupes;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	public Map<T, List<HashFunction<D, R, C, T>>> getFunctionsMap() {
		return functionsMap;
	}

	protected void setFunctionsMap(ListMap<T, HashFunction<D, R, C, T>> m) {
		this.functionsMap = m;
	}

	protected Canopy<R> getCanopy() {
		return new Canopy<R>();
	}

	public Canopy<R> getNodeFromCurrent(Canopy<R> node, HashFunction<D, R, C, T> function, FieldDefinition context) {
		Canopy<R> trial = getCanopy();
		trial = node.copyTo(trial);
		trial.function = function;
		trial.context = context;
		return trial;
	}

	public void estimateElimCount(Canopy<R> c, long elimCount) {
		c.estimateElimCount();
	}

	public Canopy<R> getBestNodeOriginal(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
										 List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		Canopy<R> best = null;
		List<FieldDefinition> adjustedFieldOfInterestList = getFieldOfInterestList(fieldsOfInterest, node);
		for (FieldDefinition field : adjustedFieldOfInterestList) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Trying for " + field + " with data type " + field.getDataType() + " and real dt "
						+ getFeatureFactory().getDataTypeFromString(field.getDataType()));
			}
			FieldDefinition context = field;
			if (least == 0) break;
			List<HashFunction<D, R, C, T>> functions = functionsMap.get(getFeatureFactory().getDataTypeFromString(field.getDataType()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("functions are " + functions);
			}
			if (functions != null) {
				for (HashFunction<D, R, C, T> function : functions) {
					if (least == 0) break;
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node)) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Evaluating field " + field.fieldName + " and function " + function + " for " + field.dataType);
						}
						Canopy<R> trial = getNodeFromCurrent(node, function, context);
						estimateElimCount(trial, least);
						long elimCount = trial.getElimCount();
						if (LOG.isDebugEnabled()) {
							LOG.debug("Elim Count is " + elimCount + " ,least is " + least + ", dupe count " + node.dupeN.size());
						}
						if (least > elimCount) {
							long childrenSize = trial.estimateCanopies();
							if (childrenSize > 1) {
								if (LOG.isDebugEnabled()) {
									LOG.debug("Yes, this fn has potential " + function);
								}
								least = elimCount;
								best = trial;
								best.elimCount = least;
							} else {
								if (LOG.isDebugEnabled()) {
									LOG.debug("No child " + function);
								}
							}
						}
					}
				}
			} else {
				LOG.debug("functions are null??????");
			}
		}
		return best;
	}

	// Original row extraction (function.apply per row, no precomputed values) + early exit.
	// Everything else same as original: estimateCanopies check, buildDupeRemaining for winner only.
	public Canopy<R> getBestNodeOriginalEarlyExit(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
												  List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		Canopy<R> best = null;
		List<FieldDefinition> adjustedFieldOfInterestList = getFieldOfInterestList(fieldsOfInterest, node);
		for (FieldDefinition field : adjustedFieldOfInterestList) {
			if (least == 0) break;
			List<HashFunction<D, R, C, T>> functions = functionsMap.get(getFeatureFactory().getDataTypeFromString(field.getDataType()));
			if (functions != null) {
				for (HashFunction<D, R, C, T> function : functions) {
					if (least == 0) break;
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node)) {
						Canopy<R> trial = getNodeFromCurrent(node, function, field);
						long elimCount = trial.countEliminationsWithRowExtraction(least);
						trial.elimCount = elimCount;
						if (least > elimCount) {
							long childrenSize = trial.estimateCanopies();
							if (childrenSize > 1) {
								least = elimCount;
								best = trial;
								best.elimCount = least;
							}
						}
					}
				}
			}
		}
		if (best != null) {
			best.buildDupeRemaining();
		}
		return best;
	}

	// Precomputed values + no estimateCanopies, but NO early exit on the counting loop.
	// Isolates the contribution of early exit vs. precomputed values.
	public Canopy<R> getBestNodeNoEarlyExit(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
											List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		Canopy<R> best = null;
		List<FieldDefinition> adjustedFieldOfInterestList = getFieldOfInterestList(fieldsOfInterest, node);
		for (FieldDefinition field : adjustedFieldOfInterestList) {
			if (least == 0) break;
			List<HashFunction<D, R, C, T>> functions = functionsMap.get(getFeatureFactory().getDataTypeFromString(field.getDataType()));
			if (functions != null && !functions.isEmpty()) {
				List<R> dupeN = node.getDupeN();
				Object[] preVals1 = new Object[dupeN.size()];
				Object[] preVals2 = new Object[dupeN.size()];
				HashFunction<D, R, C, T> refFn = functions.get(0);
				for (int i = 0; i < dupeN.size(); i++) {
					preVals1[i] = refFn.getAs(dupeN.get(i), field.fieldName);
					preVals2[i] = refFn.getAs(dupeN.get(i), ColName.COL_PREFIX + field.fieldName);
				}
				for (HashFunction<D, R, C, T> function : functions) {
					if (least == 0) break;
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node)) {
						Canopy<R> trial = getNodeFromCurrent(node, function, field);
						long elimCount = trial.countEliminationsWithValues(preVals1, preVals2, Long.MAX_VALUE);
						trial.elimCount = elimCount;
						if (least > elimCount && (elimCount > 0 || trial.estimateCanopies() > 1)) {
							least = elimCount;
							best = trial;
							best.elimCount = least;
						}
					}
				}
			} else {
				LOG.debug("functions are null??????");
			}
		}
		if (best != null) {
			best.buildDupeRemaining();
		}
		return best;
	}

	public Canopy<R> getBestNode(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node,
								 List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		Canopy<R> best = null;
		List<FieldDefinition> adjustedFieldOfInterestList = getFieldOfInterestList(fieldsOfInterest, node);
		for (FieldDefinition field : adjustedFieldOfInterestList) {
			if (least == 0) break;
			if (LOG.isDebugEnabled()) {
				LOG.debug("Trying for " + field + " with data type " + field.getDataType() + " and real dt "
						+ getFeatureFactory().getDataTypeFromString(field.getDataType()));
			}
			List<HashFunction<D, R, C, T>> functions = functionsMap.get(getFeatureFactory().getDataTypeFromString(field.getDataType()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("functions are " + functions);
			}
			if (functions != null && !functions.isEmpty()) {
				// Precompute raw field values once per row for this field.
				// All functions share the same getAs for a given data type, so
				// we use the first function as a representative extractor.
				List<R> dupeN = node.getDupeN();
				Object[] preVals1 = new Object[dupeN.size()];
				Object[] preVals2 = new Object[dupeN.size()];
				HashFunction<D, R, C, T> refFn = functions.get(0);
				for (int i = 0; i < dupeN.size(); i++) {
					preVals1[i] = refFn.getAs(dupeN.get(i), field.fieldName);
					preVals2[i] = refFn.getAs(dupeN.get(i), ColName.COL_PREFIX + field.fieldName);
				}

				for (HashFunction<D, R, C, T> function : functions) {
					if (least == 0) break;
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node)) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Evaluating field " + field.fieldName + " and function " + function + " for " + field.dataType);
						}
						Canopy<R> trial = getNodeFromCurrent(node, function, field);
						long elimCount = trial.countEliminationsWithValues(preVals1, preVals2, least);
						trial.elimCount = elimCount;
						if (LOG.isDebugEnabled()) {
							LOG.debug("Elim Count is " + elimCount + " ,least is " + least + ", dupe count " + node.dupeN.size());
						}
						// Skip estimateCanopies() when elimCount > 0: an eliminated pair
						// means ≥2 distinct hash values in training, so childrenSize > 1
						// is guaranteed. When elimCount == 0 we must still check.
						if (least > elimCount && (elimCount > 0 || trial.estimateCanopies() > 1)) {
							if (LOG.isDebugEnabled()) {
								LOG.debug("Yes, this fn has potential " + function);
							}
							least = elimCount;
							best = trial;
							best.elimCount = least;
						}
					}
				}
			} else {
				LOG.debug("functions are null??????");
			}
		}
		// Build dupeRemaining only for the winner — all other candidates discarded.
		if (best != null) {
			best.buildDupeRemaining();
		}
		return best;
	}

	public Tree<Canopy<R>> getBlockingTree(Tree<Canopy<R>> tree, Canopy<R> parent,
										   Canopy<R> node, List<FieldDefinition> fieldsOfInterest) throws Exception, ZinggClientException {
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
			Canopy<R> best = getBestNode(tree, parent, node, fieldsOfInterest);
			if (best != null) {
				hashFunctionUtility.addHashFunctionIfRequired(best);
				if (LOG.isDebugEnabled()) {
					LOG.debug(" HashFunction is " + best + " and node is " + node);
				}
				best.copyTo(node);
				if (tree == null && parent == null) {
					tree = new Tree<Canopy<R>>(node);
				}
				List<Canopy<R>> canopies = node.getCanopies();
				if (LOG.isDebugEnabled()) {
					LOG.debug(" Children size is " + canopies.size());
				}
				for (Canopy<R> n : canopies) {
					node.clearBeforeSaving();
					tree.addLeaf(node, n);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Finding for " + n);
					}
					getBlockingTree(tree, node, n, fieldsOfInterest);
				}
				hashFunctionUtility.removeHashFunctionIfRequired(best);
			} else {
				node.clearBeforeSaving();
			}
		} else {
			if ((node.getDupeN() == null) || (node.getDupeN().size() == 0)) {
				LOG.warn("Ran out of training at size " + size + " for node " + node);
			} else {
				LOG.debug("Min size reached " + size + " for node " + node);
				if (tree == null) {
					throw new ZinggClientException("Unable to create Zingg models due to insufficient data. Please run Zingg after adding more data");
				}
			}
			node.clearBeforeSaving();
		}
		return tree;
	}

	protected boolean isHashFunctionUsed(FieldDefinition field, HashFunction<D, R, C, T> function,
										 Tree<Canopy<R>> tree, Canopy<R> node) {
		return hashFunctionUtility.isHashFunctionUsed(field, function, tree, node);
	}

	public List<Canopy<R>> getHashSuccessors(Collection<Canopy<R>> successors, Object hash) {
		List<Canopy<R>> retCanopy = new ArrayList<Canopy<R>>();
		for (Canopy<R> c : successors) {
			if (hash == null && c != null && c.getHash() == null) {
				retCanopy.add(c);
			}
			if (c != null && c.getHash() != null && c.getHash().equals(hash)) {
				retCanopy.add(c);
			}
		}
		return retCanopy;
	}

	public static <R> StringBuilder applyTree(R tuple, Tree<Canopy<R>> tree,
											  Canopy<R> root, StringBuilder result) {
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			result = result.append("|").append(hash);
			for (Canopy<R> c : tree.getSuccessors(root)) {
				if (c != null) {
					if ((c.getHash() != null)) {
						if ((c.getHash().equals(hash))) {
							applyTree(tuple, tree, c, result);
						}
					}
				}
			}
		}
		return result;
	}

	public void printTree(Tree<Canopy<R>> tree, Canopy<R> root) {
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
		for (Canopy<R> c : tree.getSuccessors(root)) {
			printTree(tree, c);
		}
	}

	public List<FieldDefinition> getFieldOfInterestList(List<FieldDefinition> fieldDefinitions, Canopy<R> node) {
		return fieldDefinitionStrategy.getAdjustedFieldDefinitions(fieldDefinitions, node);
	}

	public abstract FeatureFactory<T> getFeatureFactory();

	public void setFieldDefinitionStrategy(FieldDefinitionStrategy<R> fieldDefinitionStrategy) {
		this.fieldDefinitionStrategy = fieldDefinitionStrategy;
	}
}
