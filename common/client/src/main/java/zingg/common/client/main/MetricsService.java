package zingg.common.client.main;

import zingg.common.client.IZingg;
import zingg.common.client.ZFrame;

public class MetricsService<S,D,R,C> {
    private final IZingg<S,D,R,C> zingg;

    public MetricsService(IZingg<S,D,R,C> zingg) {
        this.zingg = zingg;
    }

    public void postMetrics() {
        zingg.postMetrics();
    }

    public Long getMarkedRecordsStat(ZFrame<D,R,C> markedRecords, long value) {
        return zingg.getMarkedRecordsStat(markedRecords, value);
    }

    public Long getMatchedMarkedRecordsStat(ZFrame<D,R,C> markedRecords) {
        return zingg.getMatchedMarkedRecordsStat(markedRecords);
    }

    // Other metrics methods...
}
