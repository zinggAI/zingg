package zingg.common.core.executor.blockingverifier;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.context.Context;
import zingg.common.core.executor.blockingverifier.data.BlockingVerifyData;
import zingg.common.core.executor.blockingverifier.model.BlockCountsData;
import zingg.common.core.executor.blockingverifier.model.BlockedData;

public abstract class TestVerifyBlocking<S,D,R,C,T> {
    
    public static final Log LOG = LogFactory.getLog(TestVerifyBlocking.class);
    protected Context<S, D, R, C, T> context;
    protected DFObjectUtil<S, D, R, C> dfObjectUtil;
    protected IVerifyBlockingPipes<S,D,R,C> verifyBlockingPipes;
    IArguments args = new Arguments();

    public TestVerifyBlocking(){

    }

    @BeforeEach
	public void setUp(){
		try {
			String configPath = getClass().getResource("../../../../../blockingverifier/config.json").getFile();
			ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);
			args = argsUtil.createArgumentsFromJSON(configPath);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.info("Unexpected exception received " + e.getMessage());
			fail(e.getMessage());
		}
	}

    public void initialize(DFObjectUtil<S, D, R, C> dfObjectUtil, Context<S, D, R, C, T> context) {
        this.dfObjectUtil = dfObjectUtil;
        this.context = context;
    }

    public abstract VerifyBlocking<S,D,R,C,T> getVerifyBlocker();

    public abstract IVerifyBlockingPipes<S,D,R,C> getVerifyBlockingPipes();

    @Test
    public void testGetBlockCounts() throws ZinggClientException, Exception{
        VerifyBlocking<S,D,R,C,T> vb = getVerifyBlocker(); 
        vb.setArgs(args);
        verifyBlockingPipes = getVerifyBlockingPipes();
        verifyBlockingPipes.setTimestamp(vb.getTimestamp());

        ZFrame<D,R,C> blocked = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getBlockedDF1(), BlockedData.class);
        ZFrame<D,R,C> blockCounts = vb.getBlockCounts(blocked,verifyBlockingPipes);
        blockCounts = blockCounts.sortDescending(ColName.HASH_COUNTS_COL);

        ZFrame<D,R,C> expBlockCounts = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getExpectedBlockedDF1(), BlockCountsData.class);

        assertTrue(expBlockCounts.except(blockCounts).isEmpty());
		assertTrue(blockCounts.except(expBlockCounts).isEmpty());
    }

    @Test
    public void testGetBlockSamples() throws Exception, ZinggClientException{
        VerifyBlocking<S,D,R,C,T> vb = getVerifyBlocker(); 
        vb.setArgs(args);
        verifyBlockingPipes = getVerifyBlockingPipes();
        verifyBlockingPipes.setTimestamp(vb.getTimestamp());

        ZFrame<D,R,C> blocked = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getBlockedDF1(), BlockedData.class);
        ZFrame<D,R,C> blockCounts = vb.getBlockCounts(blocked,verifyBlockingPipes);
        ZFrame<D,R,C> blockTopRec = blockCounts.sortDescending(ColName.HASH_COUNTS_COL).limit(3); 
        List<R> topRec = blockTopRec.collectAsList();

        ZFrame<D,R,C> matchingRec1 = vb.getMatchingRecords(topRec.get(0), blockTopRec, blocked, 3915);
        assertTrue(matchingRec1.count()== (long)3);

        ZFrame<D,R,C> matchingRec2 = vb.getMatchingRecords(topRec.get(2), blockTopRec, blocked, 3910);        
        assertTrue(matchingRec2.count()== (long)1);

    }


}
