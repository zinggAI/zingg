package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;

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

	protected DataFrame getBlocks(DataFrame blocked, DataFrame bAll) throws Exception{
		return DSUtil.joinWithItselfSourceSensitive(blocked, ColName.HASH_COL, args).cacheResult();
	}

	protected DataFrame selectColsFromBlocked(DataFrame blocked) {
		return blocked;
	}

	public void writeOutput(DataFrame blocked, DataFrame dupes) {
		try {
			// input dupes are pairs
			/// pick ones according to the threshold by user
			DataFrame dupesActual = getDupesActualForGraph(dupes);

			// all clusters consolidated in one place
			if (args.getOutput() != null) {

				// input dupes are pairs
				//dupesActual = DFUtil.addClusterRowNumber(dupesActual, spark);
				dupesActual = Util.addUniqueCol(dupesActual, ColName.ID_COL);
				DataFrame dupes2 = DSUtil.alignLinked(dupesActual, args);
				LOG.debug("uncertain output schema is " + dupes2.schema());
				PipeUtil.write(dupes2, args, args.getOutput());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected DataFrame getDupesActualForGraph(DataFrame dupes) {
		DataFrame dupesActual = dupes
				.filter(dupes.col(ColName.PREDICTION_COL).in(ColValues.IS_MATCH_PREDICTION));
		return dupesActual;
	}

}
