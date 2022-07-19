package zingg.domain.model.labels;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.util.ColName;
import zingg.domain.model.predictions.Prediction;
import zingg.domain.model.predictions.PredictionIterator;
import zingg.domain.model.predictions.Predictions;
import zingg.domain.model.records.RecordRepository;
import zingg.domain.model.stats.LabellerStat;
import zingg.domain.model.stats.StatsEmitter;
import zingg.domain.model.stats.StatsService;
import zingg.util.PipeUtil;

import java.util.List;

public class Labeller{

    public static final Log LOG = LogFactory.getLog(Labeller.class);

    private final StatsService statsService;

    private StatsEmitter statsEmitter;
    private final RecordRepository recordRepository;

    private LabellerStat labellerStat;

    private LabellerUserInterfaceFactory labellerUserInterfaceFactory;



    private final Arguments args;

    public Labeller(StatsService statsService, RecordRepository recordRepository, Arguments args) {
        this.statsService = statsService;
        this.recordRepository = recordRepository;
        this.args = args;
    }

    public Dataset<Row> getMarkedRecords() {
        return recordRepository.getRecords(false,false,PipeUtil.getTrainingDataMarkedPipe(args));
    }


    private Dataset<Row> getUnmarkedRecords(Dataset<Row> markedRecords) {
        Dataset<Row> unmarkedRecords = recordRepository.getRecords(false,false,PipeUtil.getTrainingDataUnmarkedPipe(args));
        return  unmarkedRecords.join(markedRecords,
                    unmarkedRecords.col(
                        ColName.CLUSTER_COLUMN
                    ).equalTo(
                        markedRecords.col(ColName.CLUSTER_COLUMN)
                    ),
        "left_anti"
            );
    }

    public void execute() throws ZinggClientException {
        try {
            LOG.info("Reading inputs for labelling phase ...");
            Dataset<Row> marketRecords = getMarkedRecords();
//            getMarkedRecordsStat(marketRecords);
            Dataset<Row> unmarkedRecords = getUnmarkedRecords(marketRecords);
            print(unmarkedRecords);
            LOG.info("Finished labelling phase");
        } catch (Exception e) {
            throw new ZinggClientException(e.getMessage(),e);
        }
    }

    private LabellerUserInterface getUserInterface(Dataset<Row> lines, Arguments args){
        List<Column> displayCols = getDisplayColumns(lines, args);
        return labellerUserInterfaceFactory.newInterface(displayCols);
    }


    private boolean processSinglePrediction(LabellerUserInterface labellerUserInterface, PredictionIterator predictionIterator){
        labellerUserInterface.printProgress(predictionIterator);
        Prediction currentPrediction = predictionIterator.next();
        LabellingCommand selected_option = labellerUserInterface.getFeedback(currentPrediction);
        if (selected_option instanceof LabellingCommand.UpdateLabels){
            LabellingCommand.UpdateLabels update = (LabellingCommand.UpdateLabels) selected_option;
            labellerStat.updateStat(update.labelFeedback, 1);
            currentPrediction.acceptFeedback(update.labelFeedback);
        }
        statsEmitter.emitStats(labellerStat);
        if (selected_option.isAbort()) {
            LOG.info("User has quit in the middle. Updating the records.");
            return true;
        }
        updatedRecords = updateRecords(selected_option, currentPrediction, updatedRecords);
    }

    private void processNonEmptyRowSet(Dataset<Row> lines) throws ZinggClientException {
        LabellerUserInterface labellerUserInterface = getUserInterface(lines,null);
        statsEmitter.emitStats(labellerStat);
        lines = lines.cache();
        Predictions predictions = Predictions.fromLines(lines);
        try {
            PredictionIterator predictionIterator = predictions.iterator();
            boolean noBreak = true;
            LabellerOutput output = LabellerOutput.empty();
            while(predictionIterator.hasNext() && noBreak){

                noBreak = processSinglePrediction(labellerUserInterface, predictionIterator);
            }
            writeLabelledOutput(updatedRecords);
            LOG.warn("Processing finished.");
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                e.printStackTrace();
            }
            LOG.warn("Labelling error has occured " + e.getMessage());
            throw new ZinggClientException("An error has occured while Labelling.", e);
        }
    }

    public void print(Dataset<Row> lines) throws ZinggClientException {
        LOG.info("Processing Records for CLI Labelling");
        if (lines != null && lines.count() > 0) {
            processNonEmptyRowSet(lines);
        } else {
            LOG.info("It seems there are no unmarked records at this moment. Please run findTrainingData job to build some pairs to be labelled and then run this labeler.");
        }
    }

    private static Dataset<Row> updateRecords(LabelFeedback feedback, Dataset<Row> newRecords, Dataset<Row> updatedRecords) {
        newRecords = newRecords.withColumn(ColName.MATCH_FLAG_COL, functions.lit(feedback.value));
        if (updatedRecords == null) {
            updatedRecords = newRecords;
        } else {
            updatedRecords = updatedRecords.union(newRecords);
        }
        return updatedRecords;
    }




}
