package zingg.common.core.util;

import zingg.common.client.ZFrame;

public interface IDataFrameUtility<S,D,R,C,T> {

    public ZFrame<D,R,C> createDFWithDoubles(int numRows, int numCols, S session);
    
}
