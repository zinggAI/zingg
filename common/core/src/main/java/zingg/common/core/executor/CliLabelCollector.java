package zingg.common.core.executor;

import zingg.common.client.ILabelCollector;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

/**
 * Collects labels by prompting a human on the terminal -- the behaviour Zingg
 * has always had, now expressed as one ILabelCollector among others rather
 * than as the only thing Labeller can do.
 *
 * The loop itself still lives in Labeller.processRecordsCli, because
 * LabelUpdater overrides that method for the updateLabel phase.
 */
public class CliLabelCollector<S, D, R, C, T> implements ILabelCollector<D, R, C> {

	protected final Labeller<S, D, R, C, T> labeller;

	public CliLabelCollector(Labeller<S, D, R, C, T> labeller) {
		this.labeller = labeller;
	}

	@Override
	public ZFrame<D, R, C> collectLabels(ZFrame<D, R, C> unmarkedPairs) throws ZinggClientException {
		return labeller.processRecordsCli(unmarkedPairs);
	}

}
