package zingg.common.client.util;

import java.util.List;

import zingg.common.client.ZFrame;

public abstract class DFObjectUtil<S, D, R, C> {

    protected final IWithSession<S> withSession;

    protected DFObjectUtil(IWithSession<S> withSession) {
        this.withSession = withSession;
    }

    public abstract ZFrame<D, R, C> getDFFromObjectList(List objList, Class objClass) throws Exception;

}
