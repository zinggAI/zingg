package zingg.common.core.block;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TreePrinter<R> implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final Log LOG = LogFactory.getLog(TreePrinter.class);

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
}
