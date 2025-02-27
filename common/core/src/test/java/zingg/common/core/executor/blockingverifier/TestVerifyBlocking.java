package zingg.common.core.executor.blockingverifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
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
    IArguments arguments = new Arguments();

    public TestVerifyBlocking(){

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
        verifyBlockingPipes = getVerifyBlockingPipes();

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
        verifyBlockingPipes = getVerifyBlockingPipes();

        ZFrame<D,R,C> blocked = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getBlockedDF1(), BlockedData.class);
        ZFrame<D,R,C> blockCounts = vb.getBlockCounts(blocked,verifyBlockingPipes);
        ZFrame<D,R,C> blockTopRec = vb.getTopRecordsDF(blockCounts); 
        assertTrue(checkNoOfTopBlocks(blockTopRec));

        List<R> topRec = blockTopRec.collectAsList();
        assertEquals("3930",blockTopRec.getAsString(topRec.get(1), ColName.HASH_COL));

        ZFrame<D,R,C> matchingRec1 = vb.getMatchingRecords(topRec.get(0), blockTopRec, blocked, 3915);
        assertTrue(matchingRec1.count()== 3L);

        ZFrame<D,R,C> matchingRec2 = vb.getMatchingRecords(topRec.get(2), blockTopRec, blocked, -3910);        
        assertTrue(matchingRec2.count()== 1L);

    }

    @Test
    public void testOutputBlockSamples() throws Exception, ZinggClientException{
        VerifyBlocking<S,D,R,C,T> vb = getVerifyBlocker(); 
        verifyBlockingPipes = getVerifyBlockingPipes();
        vb.setModelHelper(verifyBlockingPipes.getModelHelper());
        context.setPipeUtil(verifyBlockingPipes.getPipeUtil());
        verifyBlockingPipes.setTimestamp(vb.getTimestamp());
        arguments.setModelId("junit_vb");
        vb.setArgs(arguments);

        ZFrame<D,R,C> blocked = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getBlockedDF1(), BlockedData.class);
        ZFrame<D,R,C> blockCounts = dfObjectUtil.getDFFromObjectList(BlockingVerifyData.getExpectedBlockedDF1(), BlockCountsData.class);
        vb.getBlockSamples(blocked, blockCounts, verifyBlockingPipes);

        ZFrame<D, R, C> df1 = context.getPipeUtil().read(false, false, verifyBlockingPipes.getBlockSamplesPipe(arguments, ColName.BLOCK_SAMPLES + "3915"));
        ZFrame<D, R, C> df3 = context.getPipeUtil().read(false, false, verifyBlockingPipes.getBlockSamplesPipe(arguments, getMassagedTableName("-3910")));

        assertTrue(df1.count() == 3L);
        assertTrue(df3.count() == 1L);
    }

    public boolean checkNoOfTopBlocks(ZFrame<D,R,C> blockTopRec){
        if(blockTopRec.count() == 3L){
            return true;
        }
        else{
            return false;
        }
    }

    public abstract String getMassagedTableName(String hash);


}
