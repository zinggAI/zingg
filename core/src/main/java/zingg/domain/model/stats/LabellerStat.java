package zingg.domain.model.stats;

import zingg.client.util.ColValues;
import zingg.domain.model.labels.LabelFeedback;

public class LabellerStat {
    private long positivePairsCount;
    private long negativePairsCount;

    public LabellerStat(long positivePairsCount, long negativePairsCount, long totalCount, long notSurePairsCount) {
        this.positivePairsCount = positivePairsCount;
        this.negativePairsCount = negativePairsCount;
        this.totalCount = totalCount;
        this.notSurePairsCount = notSurePairsCount;
    }

    private long totalCount;

    private long notSurePairsCount;

    public long getPositivePairsCount() {
        return positivePairsCount;
    }

    public long getNegativePairsCount() {
        return negativePairsCount;
    }

    public long getNotSurePairsCount() {
        return notSurePairsCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public synchronized void updateStat(LabelFeedback feedback, int increment){
        totalCount += increment;
        if (feedback == LabelFeedback.MATCH) {
            positivePairsCount += increment;
        }
        else if (feedback == LabelFeedback.NO_MATCH) {
            negativePairsCount += increment;
        }
        else if (feedback == LabelFeedback.NOT_SURE) {
            notSurePairsCount += increment;
        }
    }

    public static final LabellerStat empty(){
        return new LabellerStat(0,0,0,0);
    }
}
