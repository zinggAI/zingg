package zingg.common.core.executor.trainer;

import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.executor.Trainer;
import zingg.common.core.executor.trainer.util.IDataFrameUtility;

public abstract class TestTrainer<S,D,R,C,T> {
    
    protected Context<S, D, R, C, T> context;
    protected ZFrame<D,R,C> tenRowsDF;
    protected ZFrame<D,R,C> oneRowsDF;
    
    public TestTrainer(){
    }
    
    
    public void initialize(Context<S,D,R,C,T> context) {
        this.context = context;
    }

    public abstract Trainer<S,D,R,C,T> getTestTrainer();

    public void setUpDF(S s){
        tenRowsDF = getDataFrameUtility().createDFWithDoubles(10,1, s);
        oneRowsDF = getDataFrameUtility().createDFWithDoubles(1,1, s);
    }

    public abstract IDataFrameUtility<S,D,R,C,T> getDataFrameUtility();

    @Test
    public void testVerifyTrainingPosDatasetLess() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        trainer.verifyTraining(oneRowsDF, tenRowsDF);
        fail("Expected exception not getting thrown when training data is less");
    }

    @Test
    public void testVerifyTrainingNegDatasetLess() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        trainer.verifyTraining(tenRowsDF, oneRowsDF);
        fail("Expected exception not getting thrown when training data is less");
    }
    
    @Test
    public void testVerifyTrainingBothDatasetLess() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        trainer.verifyTraining(oneRowsDF,oneRowsDF);
        fail("Expected exception not getting thrown when training data is less");
    }

    @Test
    public void testVerifyTrainingBothDatasetMore() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        trainer.verifyTraining(tenRowsDF, tenRowsDF);
        fail("Exception should not have been thrown when training data is appopriate");
    }

    @Test
    public void testVerifyTrainingBothDatasetNull() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        trainer.verifyTraining(null, null);
        fail("Expected exception not getting thrown when training data is less");
    }

}
