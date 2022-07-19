//package zingg.domain.model.labels;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.spark.sql.Column;
//import org.apache.spark.sql.Dataset;
//import org.apache.spark.sql.Row;
//import zingg.client.Arguments;
//import zingg.client.ZinggClientException;
//import zingg.domain.model.predictions.Prediction;
//import zingg.domain.model.predictions.PredictionIterator;
//import zingg.domain.model.predictions.Predictions;
//import zingg.domain.model.stats.LabellerStat;
//import zingg.domain.model.stats.StatsEmitter;
//import zingg.util.DSUtil;
//
//import java.util.List;
//
//public class LabellingResultPrinter {
//
//    public static final Log LOG = LogFactory.getLog(Labeller.class);
//
//    private LabellerUserInterfaceFactory labellerUserInterfaceFactory;
//
//    private final LabellerStat stats;
//
//
//    private StatsEmitter statsEmitter;
//
//    private LabellingResultPrinter(LabellerUserInterfaceFactory labellerUserInterfaceFactory, LabellerStat stats, StatsEmitter statsEmitter) {
//        this.labellerUserInterfaceFactory = labellerUserInterfaceFactory;
//        this.stats = stats;
//        this.statsEmitter = statsEmitter;
//    }
//
//    private LabellerUserInterface getUserInterface(Dataset<Row> lines, Arguments args){
//        List<Column> displayCols = getDisplayColumns(lines, args);
//        return labellerUserInterfaceFactory.newInterface(displayCols);
//    }
//
//
//    protected void printMarkedRecordsStat() {
////        String msg = String.format(
////                "\tLabelled pairs so far    : %d/%d MATCH, %d/%d DO NOT MATCH, %d/%d NOT SURE", positivePairsCount, totalCount,
////                negativePairsCount, totalCount, notSurePairsCount, totalCount);
////
////        System.out.println();
////        System.out.println();
////        System.out.println();
////        System.out.println(msg);
//    }
//    private List<Column> getDisplayColumns(Dataset<Row> lines, Arguments args) {
//        return DSUtil.getFieldDefColumns(lines, args, false, args.getShowConcise());
//    }
////
////    public Dataset<Row> getCurrentPair(Dataset<Row> lines, int index, List<Row> clusterIds) {
////        return lines.filter(lines.col(ColName.CLUSTER_COLUMN).equalTo(
////                clusterIds.get(index).getAs(ColName.CLUSTER_COLUMN))).cache();
////    }
////
//
//
//
//    protected LabellingCommand displayRecordsAndGetUserInput(Dataset<Row> records, String preMessage, String postMessage) {
//        //System.out.println();
//        System.out.println(preMessage);
//        records.show(false);
//        System.out.println(postMessage);
//        System.out.println("\tWhat do you think? Your choices are: ");
//        int selection = readCliInput();
//        return selection;
//    }
//}
