package zingg.client.helper;

import java.util.List;

import zingg.common.client.ZFrame;

public abstract class DFObjectUtil<S, D, R, C> {

        S session;

        public DFObjectUtil(S s) {
                this.session = s;
        }

        public S getSession() {
                return this.session;
        }

        public void setSession(S session) {
                this.session = session;
        }

        public abstract ZFrame<D, R, C> getDFFromObjectList(List objList, Class objClass) throws Exception;

}