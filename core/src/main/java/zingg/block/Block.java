package zingg.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.ml.util.SchemaUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import zingg.client.FieldDefinition;
import zingg.client.util.ListMap;
import zingg.hash.HashFunction;
import zingg.client.util.ColName;
import scala.collection.JavaConversions;
import scala.collection.Seq;

public class Block implements Serializable {

	public static final Log LOG = LogFactory.getLog(Block.class);

	protected Dataset<Row> dupes;
	// Class[] types;
	ListMap<DataType, HashFunction> functionsMap;
	long maxSize;
	Dataset<Row> training;
	protected ListMap<HashFunction, String> childless;

	protected Block(Dataset<Row> training, Dataset<Row> dupes) {
		this.training = training;
		this.dupes = dupes;
		childless = new ListMap<HashFunction, String>();
		// types = getSampleTypes();
		/*
		 * for (Class type : types) { LOG.info("Type is " + type); }
		 */
	}

	public Block(Dataset<Row> training, Dataset<Row> dupes,
			ListMap<DataType, HashFunction> functionsMap, long maxSize) {
		this(training, dupes);
		this.functionsMap = functionsMap;
		// functionsMap.prettyPrint();
		this.maxSize = maxSize;
	}

	/**
	 * @return the dupes
	 */
	public Dataset<Row> getDupes() {
		return dupes;
	}

	/**
	 * @param dupes
	 *            the dupes to set
	 */
	public void setDupes(Dataset<Row> dupes) {
		this.dupes = dupes;
	}

	/**
	 * @return the types
	 * 
	 *         public Class[] getTypes() { return types; }
	 */

	/**
	 * @param types
	 *            the types to set
	 * 
	 *            public void setTypes(Class[] types) { this.types = types; }
	 * 
	 *            /**
	 * @return the maxSize
	 */
	public long getMaxSize() {
		return maxSize;
	}

	/**
	 * @param maxSize
	 *            the maxSize to set
	 */
	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	/**
	 * @return the functionsMap
	 */
	public Map<DataType, List<HashFunction>> getFunctionsMap() {
		return functionsMap;
	}

	
	protected void setFunctionsMap(ListMap<DataType, HashFunction> m) {
		this.functionsMap = m;
	}

	
	public Canopy getNodeFromCurrent(Canopy node, HashFunction function,
			FieldDefinition context) {
		Canopy trial = new Canopy();
		trial = node.copyTo(trial);
		// node.training, node.dupeN, function, context);
		trial.function = function;
		trial.context = context;
		return trial;
	}

	public Canopy getBestNode(Tree<Canopy> tree, Canopy parent, Canopy node,
			List<FieldDefinition> fieldsOfInterest) throws Exception {
		long least = Long.MAX_VALUE;
		int maxElimination = 0;
		Canopy best = null;
		for (FieldDefinition field : fieldsOfInterest) {
			//LOG.debug("Trying for " + field);
			//Class type = FieldClass.getFieldClassClass(field.getFieldClass());
			FieldDefinition context = field;
			if (least ==0) break;//how much better can it get?
			// applicable functions
			List<HashFunction> functions = functionsMap.get(field.getDataType());
			if (functions != null) {
				
				for (HashFunction function : functions) {
					// /if (!used.contains(field.getIndex(), function) &&
					if (least ==0) break;//how much better can it get?
					if (!isFunctionUsed(tree, node, field.fieldName, function) //&&
							//!childless.contains(function, field.fieldName)
							) 
							{
						LOG.debug("Evaluating field " + field.fieldName
								+ " and function " + function + " for " + field.dataType);
						Canopy trial = getNodeFromCurrent(node, function,
								context);
						trial.estimateElimCount();
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
								LOG.debug("No child " + function);
								//childless.add(function, field.fieldName);
							}
							
						}
					}
				}
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
	 */
	public Tree<Canopy> getBlockingTree(Tree<Canopy> tree, Canopy parent,
			Canopy node, List<FieldDefinition> fieldsOfInterest) throws Exception {
		/*if (LOG.isDebugEnabled()) {
			LOG.debug("Tree so far ");
			LOG.debug(tree);
		}*/
		long size = node.getTrainingSize();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Size, maxSize " + size + ", " + maxSize);
		}
		if (size > maxSize && node.getDupeN() != null && node.getDupeN().size() > 0) {
			//LOG.debug("Size is bigger ");
			Canopy best = getBestNode(tree, parent, node, fieldsOfInterest);
			if (best != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(" HashFunction is " + best + " and node is " + node);
				}
				best.copyTo(node);
				// used.add(node.context.getOperandFields()[0],
				// best.getFunction());
				// used.add(1, best.getFunction());
				if (tree == null && parent == null) {
					tree = new Tree<Canopy>(node);
				} 
				/*else {
					// /tree.addLeaf(parent, node);
					used = new ListMap<Integer, HashFunction>();
				}*/
				List<Canopy> canopies = node.getCanopies();
				if (LOG.isDebugEnabled()) {
					LOG.debug(" Children size is " + canopies.size());
				}
				for (Canopy n : canopies) {
					node.clearBeforeSaving();
					tree.addLeaf(node, n);
					if (LOG.isDebugEnabled()) {
						LOG.debug(" Finding for " + n);
					}
				
					getBlockingTree(tree, node, n, fieldsOfInterest);
				}
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
			}
			// tree.addLeaf(parent, node);
			node.clearBeforeSaving();
		}

		return tree;
	}

	public boolean checkFunctionInNode(Canopy node, String name,
			HashFunction function) {
		if (node.getFunction() != null && node.getFunction().equals(function)
				&& node.context.fieldName.equals(name)) {
			return true;
		}
		return false;
	}

	public boolean isFunctionUsed(Tree<Canopy> tree, Canopy node, String fieldName,
			HashFunction function) {
		// //LOG.debug("Tree " + tree);
		// //LOG.debug("Node  " + node);
		// //LOG.debug("Index " + index);
		// //LOG.debug("Function " + function);
		boolean isUsed = false;
		if (node == null || tree == null)
			return false;
		if (checkFunctionInNode(node, fieldName, function))
			return true;
		Tree<Canopy> nodeTree = tree.getTree(node);
		if (nodeTree == null)
			return false;

		Tree<Canopy> parent = nodeTree.getParent();
		if (parent != null) {
			Canopy head = parent.getHead();
			while (head != null) {
				// check siblings of node
				/*for (Tree<Canopy> siblings : parent.getSubTrees()) {
					Canopy sibling = siblings.getHead();
					if (checkFunctionInNode(sibling, index, function))
						return true;
				}*/
				// check parent of node
				return isFunctionUsed(tree, head, fieldName, function);
			}
		}
		return isUsed;
	}
	
	public static StructType appendHashCol(StructType s) {
		StructType retSchema = SchemaUtils.appendColumn(s, ColName.HASH_COL, DataTypes.IntegerType, false);
		LOG.debug("returning schema after step 1 is " + retSchema);
		return retSchema;
	}

	public static List<Canopy> getHashSuccessors(Collection<Canopy> successors, Object hash) {
		List<Canopy> retCanopy = new ArrayList<Canopy>();
		for (Canopy c: successors) {
			if (hash == null && c!= null && c.getHash() == null) retCanopy.add(c);
			if (c!= null && c.getHash() != null && c.getHash().equals(hash)) {
				retCanopy.add(c);
			}
		}
		return retCanopy;
	}

	/*public static StringBuilder applyTree(Row tuple, Tree<Canopy> tree,
			Canopy root, StringBuilder result) {
		LOG.debug("Applying root " + root + " on " + tuple);
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			LOG.debug("Applied root " + root + " and got " + hash);
			result = result.append("|").append(hash);
			for (Canopy c : getHashSuccessors(tree.getSuccessors(root), hash)) {
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
	
	public static StringBuilder applyTree(Row tuple, Tree<Canopy> tree,
			Canopy root, StringBuilder result) {
		if (root.function != null) {
			Object hash = root.function.apply(tuple, root.context.fieldName);
			
			result = result.append("|").append(hash);
			for (Canopy c : tree.getSuccessors(root)) {
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

	public static void printTree(Tree<Canopy> tree,
			Canopy root) {
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
		for (Canopy c : tree.getSuccessors(root)) {
			printTree(tree, c);
		}			
	}
	
	public static class BlockFunction implements MapFunction<Row, Row> {
		
		Tree<Canopy> tree;
		public BlockFunction(Tree<Canopy> tree) {
			this.tree = tree;
		}
		
		@Override
		public Row call(Row r) {
			StringBuilder bf = new StringBuilder();
			bf = Block.applyTree(r, tree, tree.getHead(), bf);
			Seq<Object> s = r.toSeq();
			List<Object> seqList = JavaConversions.seqAsJavaList(s);
			List<Object> returnList = new ArrayList<Object>(seqList.size()+1);
			returnList.addAll(seqList);
			returnList.add(bf.toString().hashCode());
			if (LOG.isDebugEnabled()) {
				for (Object o: returnList) {
					LOG.debug("return row col is " + o );
				}
			LOG.debug("returning row " + RowFactory.create(returnList) );
			}
			
			return RowFactory.create(returnList.toArray());			
		}

	}

}

