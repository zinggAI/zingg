package zingg.common.core.block;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.FieldDefinition;
import zingg.common.client.util.ColName;
import zingg.common.core.hash.HashFunction;

/**
 * Encapsulates all getBestNode algorithm variants. Inject an instance into Block
 * so new variants can be added here without touching Block or Canopy.
 *
 * Block delegates its getBestNode call to whichever method is wired up via
 * setAlgorithm / subclassing.
 */
public class GetBestNode<D, R, C, T> implements Serializable {

    public static final Log LOG = LogFactory.getLog(GetBestNode.class);

    /**
     * Default entry point called by Block. Override in a subclass to switch
     * algorithms without modifying Block.
     */
    public Canopy<R> getBestNode(Block<D, R, C, T> block, Tree<Canopy<R>> tree, Canopy<R> parent,
                                  Canopy<R> node, List<FieldDefinition> fieldsOfInterest) throws Exception {
        return optimized(block, tree, parent, node, fieldsOfInterest);
    }

    /**
     * Precomputed field values + early-exit counting + deferred buildDupeRemaining.
     * This is the production algorithm.
     */
    public Canopy<R> optimized(Block<D, R, C, T> block, Tree<Canopy<R>> tree, Canopy<R> parent,
                                Canopy<R> node, List<FieldDefinition> fieldsOfInterest) throws Exception {
        long least = Long.MAX_VALUE;
        Canopy<R> best = null;
        Map<T, List<HashFunction<D, R, C, T>>> functionsMap = block.getFunctionsMap();
        List<FieldDefinition> adjustedFields = block.getFieldOfInterestList(fieldsOfInterest, node);

        for (FieldDefinition field : adjustedFields) {
            if (least == 0) break;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Trying for " + field + " with data type " + field.getDataType()
                        + " and real dt " + block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
            }
            List<HashFunction<D, R, C, T>> functions =
                    functionsMap.get(block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("functions are " + functions);
            }
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
                    if (!block.isHashFunctionUsed(field, function, tree, node)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Evaluating field " + field.fieldName + " and function "
                                    + function + " for " + field.dataType);
                        }
                        Canopy<R> trial = block.getNodeFromCurrent(node, function, field);
                        long elimCount = trial.countEliminationsWithValues(preVals1, preVals2, least);
                        trial.elimCount = elimCount;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Elim Count is " + elimCount + " ,least is " + least
                                    + ", dupe count " + node.dupeN.size());
                        }
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
        if (best != null) {
            best.buildDupeRemaining();
        }
        return best;
    }

    /**
     * Original algorithm: estimateElimCount() per candidate, no early exit,
     * buildDupeRemaining built for all evaluated trials.
     */
    public Canopy<R> original(Block<D, R, C, T> block, Tree<Canopy<R>> tree, Canopy<R> parent,
                               Canopy<R> node, List<FieldDefinition> fieldsOfInterest) throws Exception {
        long least = Long.MAX_VALUE;
        Canopy<R> best = null;
        Map<T, List<HashFunction<D, R, C, T>>> functionsMap = block.getFunctionsMap();
        List<FieldDefinition> adjustedFields = block.getFieldOfInterestList(fieldsOfInterest, node);

        for (FieldDefinition field : adjustedFields) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Trying for " + field + " with data type " + field.getDataType()
                        + " and real dt " + block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
            }
            FieldDefinition context = field;
            if (least == 0) break;
            List<HashFunction<D, R, C, T>> functions =
                    functionsMap.get(block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
            if (LOG.isDebugEnabled()) {
                LOG.debug("functions are " + functions);
            }
            if (functions != null) {
                for (HashFunction<D, R, C, T> function : functions) {
                    if (least == 0) break;
                    if (!block.isHashFunctionUsed(field, function, tree, node)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Evaluating field " + field.fieldName + " and function "
                                    + function + " for " + field.dataType);
                        }
                        Canopy<R> trial = block.getNodeFromCurrent(node, function, context);
                        block.estimateElimCount(trial, least);
                        long elimCount = trial.getElimCount();
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Elim Count is " + elimCount + " ,least is " + least
                                    + ", dupe count " + node.dupeN.size());
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


    /**
     * Extends optimized() by passing the winning field's precomputed values directly
     * to buildDupeRemaining. When a new best is found inside the field loop, its
     * preVals1/preVals2 arrays are snapshotted alongside it. buildDupeRemainingWithValues
     * then uses function.applyToValue(preVals[i]) instead of re-extracting via
     * function.apply(row, fieldName) — eliminating the per-row field-extraction cost
     * that buildDupeRemaining currently pays even though getBestNode already did it.
     */
    public Canopy<R> precomputedBuildDupeRemaining(Block<D, R, C, T> block, Tree<Canopy<R>> tree,
                                                    Canopy<R> parent, Canopy<R> node,
                                                    List<FieldDefinition> fieldsOfInterest) throws Exception {
        long least = Long.MAX_VALUE;
        Canopy<R> best = null;
        Object[] bestPreVals1 = null;
        Object[] bestPreVals2 = null;
        Map<T, List<HashFunction<D, R, C, T>>> functionsMap = block.getFunctionsMap();
        List<FieldDefinition> adjustedFields = block.getFieldOfInterestList(fieldsOfInterest, node);

        for (FieldDefinition field : adjustedFields) {
            if (least == 0) break;
            List<HashFunction<D, R, C, T>> functions =
                    functionsMap.get(block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
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
                    if (!block.isHashFunctionUsed(field, function, tree, node)) {
                        Canopy<R> trial = block.getNodeFromCurrent(node, function, field);
                        long elimCount = trial.countEliminationsWithValues(preVals1, preVals2, least);
                        trial.elimCount = elimCount;
                        if (least > elimCount && (elimCount > 0 || trial.estimateCanopies() > 1)) {
                            least = elimCount;
                            best = trial;
                            best.elimCount = least;
                            bestPreVals1 = preVals1;
                            bestPreVals2 = preVals2;
                        }
                    }
                }
            } else {
                LOG.debug("functions are null??????");
            }
        }
        if (best != null) {
            best.buildDupeRemainingWithValues(bestPreVals1, bestPreVals2);
        }
        return best;
    }

    /**
     * Probe-reuse optimisation: allocates one probe Canopy before the loop and
     * reuses it for every (field × function) candidate by simply reassigning
     * probe.function and probe.context. A real Canopy is materialised via
     * getNodeFromCurrent only when a new best is found (typically 1–2 times per
     * call). This cuts per-candidate Canopy + copyTo allocations from O(N) to O(1).
     *
     * Correctness: estimateCanopies() and countEliminationsWithValues() are pure
     * reads — they only touch probe.function, probe.context, probe.training, and
     * probe.dupeN, none of which change between iterations (dupeN/training come
     * from node via copyTo at the start; function/context are set explicitly each
     * time).
     */
    public Canopy<R> probeReuse(Block<D, R, C, T> block, Tree<Canopy<R>> tree, Canopy<R> parent,
                                 Canopy<R> node, List<FieldDefinition> fieldsOfInterest) throws Exception {
        long least = Long.MAX_VALUE;
        Canopy<R> best = null;
        Map<T, List<HashFunction<D, R, C, T>>> functionsMap = block.getFunctionsMap();
        List<FieldDefinition> adjustedFields = block.getFieldOfInterestList(fieldsOfInterest, node);

        // One probe Canopy shared across all candidates — no per-candidate allocation.
        Canopy<R> probe = block.getCanopy();
        node.copyTo(probe);

        for (FieldDefinition field : adjustedFields) {
            if (least == 0) break;
            List<HashFunction<D, R, C, T>> functions =
                    functionsMap.get(block.getFeatureFactory().getDataTypeFromString(field.getDataType()));
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
                    if (!block.isHashFunctionUsed(field, function, tree, node)) {
                        probe.function = function;
                        probe.context = field;
                        long elimCount = probe.countEliminationsWithValues(preVals1, preVals2, least);
                        if (least > elimCount && (elimCount > 0 || probe.estimateCanopies() > 1)) {
                            // Materialise a real Canopy only for the new best.
                            Canopy<R> winner = block.getNodeFromCurrent(node, function, field);
                            winner.elimCount = elimCount;
                            best = winner;
                            least = elimCount;
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
}
