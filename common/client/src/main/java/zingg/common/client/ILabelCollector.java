package zingg.common.client;

/**
 * Supplies the labels for a set of unmarked training pairs.
 *
 * This is the seam between the labelling pipeline and whatever is actually
 * doing the labelling. The pipeline in Labeller.execute() -- read the unmarked
 * pairs, preprocess them, collect labels, restore the original row shape and
 * write the result -- is the same for every front end; only this step differs.
 * A terminal session reads from stdin (CliLabelCollector), a remote client
 * hands over decisions it already collected (MapLabelCollector), a notebook or
 * web form can supply its own.
 *
 * Implementations must not write anything: returning the labelled rows is the
 * whole contract, and Labeller does the postprocessing and the write, so no
 * front end can accidentally skip either.
 */
public interface ILabelCollector<D, R, C> {

	/**
	 * @param unmarkedPairs the pairs still needing a label, already preprocessed
	 * @return the same rows with ColName.MATCH_FLAG_COL set to the chosen label,
	 *         or null if nothing was labelled
	 */
	ZFrame<D, R, C> collectLabels(ZFrame<D, R, C> unmarkedPairs) throws ZinggClientException;

}
