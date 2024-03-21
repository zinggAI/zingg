package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;

public class LinkerTester<S, D, R, C, T> extends ExecutorTester<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(LinkerTester.class);

    public LinkerTester(Linker<S, D, R, C, T> linker) {
        super(linker);
    }

    @Override
    public void validateResults() throws ZinggClientException {
        LOG.info("link successful");
        ZFrame<D, R, C> outputData = getOutputData();
        System.out.println("Inside validateResults in LinkerTester" + outputData);
        outputData.show();
        System.out.println(outputData.count());
        assertTrue(outputData != null, "Output data is not null");
    }

    @SuppressWarnings("unchecked")
    public ZFrame<D, R, C> getOutputData() throws ZinggClientException {
        // This is giving matcher output, need to get the linker output.
        ZFrame<D, R, C> output = executor.getContext().getPipeUtil().
                read(false, false, executor.getArgs().getOutput());
                
		System.out.println("Inside getOutputData in LinkerTester" + output);
        return output;
	}

}