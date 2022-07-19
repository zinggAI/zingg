package zingg.domain.model.stats;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;

public class StatsService {

    private Long getMarkedRecordsStat(Dataset<Row> markedRecords, long value) {
        return markedRecords.filter(markedRecords.col(ColName.MATCH_FLAG_COL).equalTo(value)).count() / 2;
    }

    private Long getMatchedMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_MATCH);
    }

    private Long getUnmatchedMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_A_MATCH);
    }

    private Long getUnsureMarkedRecordsStat(Dataset<Row> markedRecords){
        return getMarkedRecordsStat(markedRecords, ColValues.MATCH_TYPE_NOT_SURE);
    }

    public LabellerStat getLabellerStats(Dataset<Row> markedRecords){
        if (markedRecords != null ) {
            long positivePairsCount = getMatchedMarkedRecordsStat(markedRecords);
            long negativePairsCount =  getUnmatchedMarkedRecordsStat(markedRecords);
            long notSurePairsCount = getUnsureMarkedRecordsStat(markedRecords);
            long totalCount = markedRecords.count() / 2;
            return new LabellerStat(positivePairsCount,negativePairsCount, totalCount, notSurePairsCount);
        }
        return LabellerStat.empty();
    }

}
