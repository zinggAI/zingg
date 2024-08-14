package zingg.common.client.util;

import java.util.List;

import zingg.common.client.ZFrame;

public abstract class DFObjectUtil<S, D, R, C> {

    protected final IWithSession<S> iWithSession;

    protected DFObjectUtil(IWithSession<S> iWithSession) {
        this.iWithSession = iWithSession;
    }

    public abstract ZFrame<D, R, C> getDFFromObjectList(List objList, Class objClass) throws Exception;

}
