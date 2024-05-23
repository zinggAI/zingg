package zingg.common.core.match;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.IArguments;
import zingg.common.client.util.DSUtil;

public abstract class AOutputBuilder<S,D,R,C> implements IMatchOutputBuilder<S,D,R,C> {

    public static final Log LOG = LogFactory.getLog(GraphMatchOutputBuilder.class); 
    private DSUtil<S,D,R,C> dSUtil;
    protected IArguments args;

   
    public AOutputBuilder(DSUtil<S,D,R,C> dsUtil, IArguments args){
        this.dSUtil = dsUtil;
        this.args = args;
    }

    public DSUtil<S, D, R, C> getDSUtil() {
        return dSUtil;
    }

    public void setDSUtil(DSUtil<S, D, R, C> dsUtil) {
        this.dSUtil = dsUtil;
    }

    public IArguments getArgs() {
        return args;
    }

    public void setArgs(IArguments args) {
        this.args = args;
    }

    


}
