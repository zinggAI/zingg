package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;

import scala.collection.JavaConverters;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;

import zingg.scala.TypeTags;
import zingg.scala.DFUtil;

public class Linker extends Matcher {

	protected static String name = "zingg.Linker";
	public static final Log LOG = LogFactory.getLog(Linker.class);

	public Linker() {
		setZinggOptions(ZinggOptions.LINK);
	}

	protected Dataset<Row> getBlocks(Dataset<Row> blocked, Dataset<Row> bAll) throws Exception{
		return DSUtil.joinWithItselfSourceSensitive(blocked, ColName.HASH_COL, args).cache();
	}

	protected Dataset<Row> selectColsFromBlocked(Dataset<Row> blocked) {
		return blocked;
	}

	public void writeOutput(Dataset<Row> blocked, Dataset<Row> dupes) {
		try {
			// input dupes are pairs
			/// pick ones according to the threshold by user
			Dataset<Row> dupesActual = getDupesActualForGraph(dupes);

			// all clusters consolidated in one place
			if (args.getOutput() != null) {

				// input dupes are pairs
				dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
				dupesActual = Util.addUniqueCol(dupesActual, ColName.CLUSTER_COLUMN);
				Dataset<Row> dupes1 = DSUtil.alignLinked(dupesActual, args);
				Dataset<Row> dupes2 = dupes1.orderBy(ColName.CLUSTER_COLUMN);
				LOG.debug("uncertain output schema is " + dupes2.schema());
				PipeUtil.write(dupes2, args, ctx, args.getOutput());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected Dataset<Row> getDupesActualForGraph(Dataset<Row> dupes) {
		Dataset<Row> dupesActual = dupes
				.filter(dupes.col(ColName.PREDICTION_COL).equalTo(ColValues.IS_MATCH_PREDICTION));
		return dupesActual;
	}

}
