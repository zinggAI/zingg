package zingg.common.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.arguments.model.IZArgs;

public class ArgumentsAssembler {

    public final Log LOG = LogFactory.getLog(ArgumentsAssembler.class);

    public IZArgs assemble(IZArgs args, ClientOptions options) {
        int jobId = Long.valueOf(System.currentTimeMillis()).intValue();;

        if (options.get(ClientOptions.JOBID) != null) {
            LOG.info("Using job id from command line");
            jobId = Integer.parseInt(options.get(ClientOptions.JOBID).getValue());
        } else if (args.getJobId() != -1) {
            jobId = args.getJobId();
        }
        args.setJobId(jobId);
        if (options.get(ClientOptions.ZINGG_DIR) != null) {
            args.setZinggDir(options.get(ClientOptions.ZINGG_DIR).getValue());
        }
        if (options.get(ClientOptions.MODEL_ID) != null) {
            args.setModelId(options.get(ClientOptions.MODEL_ID).getValue());
        }
        if (options.get(ClientOptions.COLLECT_METRICS) != null) {
            args.setCollectMetrics(Boolean.parseBoolean(options.get(ClientOptions.COLLECT_METRICS).getValue()));
        }
        if (options.get(ClientOptions.SHOW_CONCISE) != null && args instanceof IArguments) {
            ((IArguments) args).setShowConcise(Boolean.parseBoolean(options.get(ClientOptions.SHOW_CONCISE).getValue()));
        }
        if (options.get(ClientOptions.COLUMN) != null && args instanceof IArguments) {
            ((IArguments) args).setColumn(options.get(ClientOptions.COLUMN).getValue());
        }
        return args;
    }
}
