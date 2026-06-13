package zingg.common.core.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.strategy.field.DefaultFieldDefinitionStrategy;
import zingg.common.core.block.strategy.field.FieldDefinitionStrategy;
import zingg.common.core.block.strategy.hash.HashFunctionUtilityFactory;
import zingg.common.core.block.strategy.hash.HashUtility;
import zingg.common.core.block.strategy.hash.IHashFunctionUtility;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.hash.HashFunction;

public abstract class Block<D,R,C,T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(Block.class);
	protected final IHashFunctionUtility<D, R, C, T> hashFunctionUtility;
	//private FieldDefinitionStrategy<R> fieldDefinitionStrategy;

	// Class[] types;
	protected ListMap<T, HashFunction<D,R,C,T>> functionsMap;
	protected long maxSize;
	protected ListMap<HashFunction<D,R,C,T>, String> childless;
	protected List<? extends FieldDefinition> fieldDefinitions;
	protected IArguments args;
	protected FieldDefinitionStrategy<R> fieldDefinitionStrategy;

	public Block() {
		this.hashFunctionUtility = HashFunctionUtilityFactory.getHashFunctionUtility(HashUtility.CACHED);
		fieldDefinitionStrategy = new DefaultFieldDefinitionStrategy<R>();
	}

	public Block(ListMap<T, HashFunction<D, R, C, T>> functionsMap, long maxSize, IArguments args) {
		this();
		childless =  new ListMap<HashFunction<D,R,C,T>, String>();
		this.functionsMap = functionsMap;
		// functionsMap.prettyPrint();
		this.maxSize = maxSize;
		this.args = args;
		fieldDefinitions = new FieldDefUtil().getFieldDefinitionNotDontUse(args.getFieldDefinition());
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
			Canopy<R>node) throws Exception, ZinggClientException {
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
			BestNodeSelector<D, R, C, T> selector = new BestNodeSelector<>(hashFunctionUtility, functionsMap, fieldDefinitions, fieldDefinitionStrategy, getFeatureFactory());
			Canopy<R>best = selector.getBestNode(tree, parent, node);
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
				
					getBlockingTree(tree, node, n);
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





	public abstract FeatureFactory<T> getFeatureFactory();

	public void setFieldDefinitionStrategy(FieldDefinitionStrategy<R> fieldDefinitionStrategy) {
		this.fieldDefinitionStrategy = fieldDefinitionStrategy;
	}
}

