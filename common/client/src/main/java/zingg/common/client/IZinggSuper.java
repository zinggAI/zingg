package zingg.common.client;

//TODO need to revisit this interface
public interface IZinggSuper<S,D,R,C> {
    ZFrame<D,R,C>  getMarkedRecords();

    ZFrame<D,R,C>  getUnmarkedRecords();

    Long getMarkedRecordsStat(ZFrame<D, R, C> markedRecords, long value);

    Long getMatchedMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

    Long getUnmatchedMarkedRecordsStat(ZFrame<D, R, C> markedRecords);

    Long getUnsureMarkedRecordsStat(ZFrame<D, R, C> markedRecords);
}
