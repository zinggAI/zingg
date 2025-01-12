package zingg.common.core.preprocess;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.data.EventTestData;
import zingg.common.core.model.PriorStopWordProcess;

public abstract class TestPreprocessors<S,D,R,C,T> {

    public static final Log LOG = LogFactory.getLog(TestPreprocessors.class);
    protected ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);
	private final DFObjectUtil<S, D, R, C> dfObjectUtil;
	private final Context<S, D, R, C, T> context;

    public TestPreprocessors(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
		this.dfObjectUtil = dfObjectUtil;
		this.context = context;
	}
    
    @Test
    public void TestPreprocessorsFlow() throws ZinggClientException, Exception{
        IArguments args = argsUtil.createArgumentsFromJSON(TestPreprocessors.class.getResource("/Users/sania/zingg/common/core/src/test/resources/preProcess/configTestPreprocess.json").getFile(), "test");
        
        IPreprocessors<S,D,R,C,T> preprocessors = getPreprocessors();
        
        ZFrame<D,R,C> inputDF = dfObjectUtil.getDFFromObjectList(EventTestData.getData2Original(), PriorStopWordProcess.class);
        ZFrame<D,R,C> expectedDF = dfObjectUtil.getDFFromObjectList(EventTestData.getData2Expected(), PriorStopWordProcess.class);

        ZFrame<D,R,C> resultDF = preprocessors.preprocess(inputDF);
        
        assertTrue(resultDF.except(expectedDF).isEmpty());
        assertTrue(expectedDF.except(resultDF).isEmpty());
    
    }

    public abstract IPreprocessors<S,D,R,C,T> getPreprocessors();

}
