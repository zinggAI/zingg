package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.util.IDataFrameUtility;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class TestTrainer<S,D,R,C,T> {
    
    protected final Context<S, D, R, C, T> context;

    public TestTrainer(Context<S,D,R,C,T> context) {
        this.context = context;
    }
    
    public ZFrame<D,R,C> tenRowsDF;
    public ZFrame<D,R,C> oneRowsDF;

    @BeforeAll
    public void setupDF() {
        tenRowsDF = getDataFrameUtility().createDFWithDoubles(10,1, context.getSession());
        oneRowsDF = getDataFrameUtility().createDFWithDoubles(1,1, context.getSession());
    }

    public abstract Trainer<S,D,R,C,T> getTestTrainer();

    public abstract IDataFrameUtility<S,D,R,C,T> getDataFrameUtility();

    public abstract ZFrame<D,R,C> getTenRowsDF(ZFrame<D,R,C> tenRowsDF);

    public abstract ZFrame<D,R,C> getOneRowsDF(ZFrame<D,R,C> oneRowsDF);

    @Test
    public void testVerifyTrainingPosDatasetLess() throws Throwable{
        try {
            Trainer<S,D,R,C,T> trainer = getTestTrainer();
            trainer.verifyTraining(getOneRowsDF(oneRowsDF), getTenRowsDF(tenRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {
        }

    }

    @Test
    public void testVerifyTrainingNegDatasetLess() throws Throwable{
        try {
            Trainer<S,D,R,C,T> trainer = getTestTrainer();
            trainer.verifyTraining(getTenRowsDF(tenRowsDF), getOneRowsDF(oneRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }
    }
    
    @Test
    public void testVerifyTrainingBothDatasetLess() throws Throwable{
        try {
            Trainer<S,D,R,C,T> trainer = getTestTrainer();
            trainer.verifyTraining(getOneRowsDF(oneRowsDF), getOneRowsDF(oneRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingBothDatasetMore() throws Throwable{
        try {
            Trainer<S,D,R,C,T> trainer = getTestTrainer();
            trainer.verifyTraining(getTenRowsDF(tenRowsDF), getTenRowsDF(tenRowsDF));
            
        }
        catch(ZinggClientException e) {
            fail("Exception should not have been thrown when training data is appopriate");
        }

    }

    @Test
    public void testVerifyTrainingBothDatasetNull() throws Throwable{
        try {
            Trainer<S,D,R,C,T> trainer = getTestTrainer();
            trainer.verifyTraining(null, null);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }
    }

}
