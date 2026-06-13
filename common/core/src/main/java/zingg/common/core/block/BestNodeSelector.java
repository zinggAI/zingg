package zingg.common.core.block;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.strategy.field.FieldDefinitionStrategy;
import zingg.common.core.block.strategy.hash.IHashFunctionUtility;
import zingg.common.core.feature.FeatureFactory;
import zingg.common.core.hash.HashFunction;

public class BestNodeSelector<D, R, C, T> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(BestNodeSelector.class);

	private final IHashFunctionUtility<D, R, C, T> hashFunctionUtility;
	private final ListMap<T, HashFunction<D, R, C, T>> functionsMap;
	private final List<? extends FieldDefinition> fieldDefinitions;
	private final FieldDefinitionStrategy<R> fieldDefinitionStrategy;
	private final FeatureFactory<T> featureFactory;

	public BestNodeSelector(IHashFunctionUtility<D, R, C, T> hashFunctionUtility,
			ListMap<T, HashFunction<D, R, C, T>> functionsMap,
			List<? extends FieldDefinition> fieldDefinitions,
			FieldDefinitionStrategy<R> fieldDefinitionStrategy,
			FeatureFactory<T> featureFactory) {
		this.hashFunctionUtility = hashFunctionUtility;
		this.functionsMap = functionsMap;
		this.fieldDefinitions = fieldDefinitions;
		this.fieldDefinitionStrategy = fieldDefinitionStrategy;
		this.featureFactory = featureFactory;
	}

	public Canopy<R> getBestNode(Tree<Canopy<R>> tree, Canopy<R> parent, Canopy<R> node) throws Exception {
		long least = Long.MAX_VALUE;
		int maxElimination = 0;
		Canopy<R> best = null;
		List<? extends FieldDefinition> adjustedFieldOfInterestList = getFieldOfInterestList(fieldDefinitions, node);
		for (FieldDefinition field : adjustedFieldOfInterestList) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Trying for " + field + " with data type " + field.getDataType() + " and real dt "
						+ featureFactory.getDataTypeFromString(field.getDataType()));
			}
			// Class type = FieldClass.getFieldClassClass(field.getFieldClass());
			FieldDefinition context = field;
			if (least == 0)
				break;// how much better can it get?
			// applicable functions
			List<HashFunction<D, R, C, T>> functions = functionsMap
					.get(featureFactory.getDataTypeFromString(field.getDataType()));
			if (LOG.isDebugEnabled()) {
				LOG.debug("functions are " + functions);
			}

			if (functions != null) {

				for (HashFunction function : functions) {
					// /if (!used.contains(field.getIndex(), function) &&
					if (least == 0)
						break;// how much better can it get?
					if (!hashFunctionUtility.isHashFunctionUsed(field, function, tree, node) //&&
							// !childless.contains(function, field.fieldName)
					) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Evaluating field " + field.fieldName + " and function " + function + " for "
									+ field.dataType);
						}
						Canopy<R> trial = node.getNodeFromCurrent(function, context);

						long elimCount = trial.getEstimatedElimCount(least);

						// int trSize = (int) Math.ceil(0.02d * node.dupeN.count());
						// boolean isNotEliminatingMoreThan1Percent = elimCount <= trSize ? true
						// : false;

						if (LOG.isDebugEnabled()) {
							LOG.debug("Elim Count is " + elimCount

									+ " ,least is " + least
									// + " , training is "
									// + node.training
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
								/*
								 * if (elimCount == 0) {
								 * LOG.debug("Out of this tyranny " + function);
								 * break;
								 * }
								 */
							} else {
								if (LOG.isDebugEnabled()) {
									LOG.debug("No child " + function);
								}
								// childless.add(function, field.fieldName);
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

	public List<? extends FieldDefinition> getFieldOfInterestList(List<? extends FieldDefinition> fieldDefinitions,
			Canopy<R> node) {
		return fieldDefinitionStrategy.getAdjustedFieldDefinitions(fieldDefinitions, node);
	}
}
