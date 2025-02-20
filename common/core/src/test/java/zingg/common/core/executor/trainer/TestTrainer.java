package zingg.common.core.executor.trainer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        ZinggClientException ex = assertThrows(ZinggClientException.class, 
            () -> trainer.verifyTraining(oneRowsDF, tenRowsDF),
            "Expected exception not thrown when positive training data is less");
        assertTrue(ex.getMessage().contains("insufficient training data"), "Unexpected error message: " + ex.getMessage());
    }

    @Test
    public void testVerifyTrainingNegDatasetLess() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        ZinggClientException ex = assertThrows(ZinggClientException.class, 
            () -> trainer.verifyTraining(tenRowsDF, oneRowsDF),
            "Expected exception not thrown when negative training data is less");
        assertTrue(ex.getMessage().contains("insufficient training data"), "Unexpected error message: " + ex.getMessage());
    }
    
    @Test
    public void testVerifyTrainingBothDatasetLess() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        ZinggClientException ex = assertThrows(ZinggClientException.class, 
            () -> trainer.verifyTraining(oneRowsDF, oneRowsDF),
            "Expected exception not thrown when both datasets are insufficient");
        assertTrue(ex.getMessage().contains("insufficient training data"), "Unexpected error message: " + ex.getMessage());
    }

    @Test
    public void testVerifyTrainingBothDatasetMore() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        // no exception is thrown
        assertDoesNotThrow(() -> trainer.verifyTraining(tenRowsDF, tenRowsDF),
            "Exception should not have been thrown when training data is appropriate");
    }

    @Test
    public void testVerifyTrainingBothDatasetNull() throws ZinggClientException{
        Trainer<S,D,R,C,T> trainer = getTestTrainer();
        ZinggClientException ex = assertThrows(ZinggClientException.class, 
            () -> trainer.verifyTraining(null, null),
            "Expected exception not thrown when both datasets are null");
        assertTrue(ex.getMessage().contains("insufficient positive training data"), "Unexpected error message: " + ex.getMessage());
    }

}
