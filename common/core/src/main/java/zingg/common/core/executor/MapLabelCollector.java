package zingg.common.core.executor;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ILabelCollector;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;

/**
 * Applies labels that were decided somewhere else -- a Spark Connect client, a
 * notebook, a web form -- keyed by cluster id.
 *
 * The decision of which pair is a match belongs wherever the human is; this
 * only records it. Everything after that (restoring the original row shape via
 * LabellerUtil.postProcessLabel, and writing to the marked pipe) is done by
 * Labeller, so a remote front end goes through exactly the same path as the
 * terminal one.
 */
public class MapLabelCollector<S, D, R, C, T> implements ILabelCollector<D, R, C> {

	public static final Log LOG = LogFactory.getLog(MapLabelCollector.class);

	protected final Labeller<S, D, R, C, T> labeller;
	protected final Map<String, Integer> clusterLabels;

	/**
	 * @param clusterLabels ColName.CLUSTER_COLUMN value -> label, using the same
	 *                      values the CLI accepts (see ColValues.MATCH_TYPE_*)
	 */
	public MapLabelCollector(Labeller<S, D, R, C, T> labeller, Map<String, Integer> clusterLabels) {
		this.labeller = labeller;
		this.clusterLabels = clusterLabels;
	}

	@Override
	public ZFrame<D, R, C> collectLabels(ZFrame<D, R, C> unmarkedPairs) throws ZinggClientException {
		if (clusterLabels == null || clusterLabels.isEmpty()) {
			LOG.warn("No labels supplied, nothing to record");
			return null;
		}
		if (unmarkedPairs == null || unmarkedPairs.count() == 0) {
			LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData "
					+ "job to build some pairs to be labelled.");
			return null;
		}

		ZFrame<D, R, C> lines = unmarkedPairs.cache();
		ZFrame<D, R, C> updatedRecords = null;
		for (Map.Entry<String, Integer> entry : clusterLabels.entrySet()) {
			ZFrame<D, R, C> currentPair = lines.filter(lines.equalTo(ColName.CLUSTER_COLUMN, entry.getKey()));
			if (currentPair.isEmpty()) {
				// Already marked, or never part of this model's unmarked set.
				LOG.warn("Ignoring label for cluster " + entry.getKey() + ": not an unmarked pair");
				continue;
			}
			labeller.getTrainingDataModel().updateLabellerStat(entry.getValue(), Labeller.INCREMENT);
			updatedRecords = labeller.getTrainingDataModel().updateRecords(entry.getValue(), currentPair,
					updatedRecords);
		}
		return updatedRecords;
	}

}
