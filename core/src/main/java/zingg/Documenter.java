package zingg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;

import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.pipe.Pipe;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.util.DSUtil;
import zingg.util.PipeUtil;

import freemarker.template.*;
import java.util.*;
import java.io.*;

public class Documenter extends ZinggBase {

	protected static String name = "zingg.Documenter";
	public static final Log LOG = LogFactory.getLog(Documenter.class);

	public Documenter() {
		setZinggOptions(ZinggOptions.GENERATE_DOCS);
	}

	public void execute() throws ZinggClientException {
		try {
			LOG.info("Document generation in progress");
			Dataset<Row> markedRecords = PipeUtil.read(spark, false, false, PipeUtil.getTrainingDataMarkedPipe(args));
			markedRecords = markedRecords.cache();
			List<Column> displayCols = DSUtil.getFieldDefColumns(markedRecords, args, false);
			List<Row> clusterIDs = markedRecords.select(ColName.CLUSTER_COLUMN).distinct().collectAsList();
			int totalPairs = clusterIDs.size();
			test(markedRecords);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	public void test(Dataset<Row> pairs) throws Exception {

        /* ------------------------------------------------------------------------ */
        /* You should do this ONLY ONCE in the whole application life-cycle:        */

        /* Create and adjust the configuration singleton */
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		cfg.setClassForTemplateLoading(this.getClass(), "/");
       // cfg.setDirectoryForTemplateLoading(new File("/where/you/store/templates"));
        // Recommended settings for new projects:
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setFallbackOnNullLoopVariable(false);

        /* ------------------------------------------------------------------------ */
        /* You usually do these for MULTIPLE TIMES in the application life-cycle:   */

        /* Create a data-model */
        Map root = new HashMap();
        root.put("user", "Big Joe");
		root.put("clusters", pairs.collectAsList());
       
        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("model.ftlh");

        /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);
        temp.process(root, out);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
    }

	/*	
		try {
			double score;
			double prediction;
			Dataset<Row> updatedRecords = null;
			int selected_option = -1;
			String msg1, msg2;
			
			
			for (int index = 0; index < totalPairs; index++){	
				Dataset<Row> currentPair = lines.filter(lines.col(ColName.CLUSTER_COLUMN).equalTo(
						clusterIDs.get(index).getAs(ColName.CLUSTER_COLUMN))).cache();
				
				score = currentPair.head().getAs(ColName.SCORE_COL);
				prediction = currentPair.head().getAs(ColName.PREDICTION_COL);
 
				msg1 = String.format("\tRecord pair %d out of %d records to be labelled by the user.\n", index, totalPairs);
				String matchType = LabelMatchType.get(prediction).msg;
				msg2 = String.format("\tZingg predicts the records %s with a similarity score of %.2f\n", 
					matchType, score);
				String msgHeader = msg1 + msg2;

				selected_option = displayRecordsAndGetUserInput(DSUtil.select(currentPair, displayCols), msgHeader);
				if (selected_option == 9) {
					LOG.info("User has quit in the middle. Updating the records.");
					break;
				}
				updatedRecords = updateRecords(selected_option, currentPair, updatedRecords);				
			}
			writeLabelledOutput(updatedRecords);
			LOG.warn("Processing finished.");
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				e.printStackTrace();
			}
			LOG.warn("Labelling error has occured " + e.getMessage());
			throw new ZinggClientException(e.getMessage());
		}
		return;
	}
	*/

	
	
}
