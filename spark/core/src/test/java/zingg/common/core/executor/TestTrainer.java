package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.spark.core.executor.ZinggSparkTester;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestTrainer extends ZinggSparkTester{
    
    public static Dataset<Row> tenRowsDF;
    public static Dataset<Row> oneRowsDF;

    @BeforeAll
    public void setupDF() {
        tenRowsDF = createDFWithDoubles(10,1);
        oneRowsDF = createDFWithDoubles(1,1);
    }

    @Test
    public void testVerifyTrainingPosDatasetLess() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(oneRowsDF, tenRowsDF);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {
        }

    }

    @Test
    public void testVerifyTrainingNegDatasetLess() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(tenRowsDF, oneRowsDF);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }
    }
    
    @Test
    public void testVerifyTrainingBothDatasetLess() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(oneRowsDF, oneRowsDF);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingBothDatasetMore() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(tenRowsDF, tenRowsDF);
            
        }
        catch(ZinggClientException e) {
            fail("Exception should not have been thrown when training data is appopriate");
        }

    }

    @Test
    public void testVerifyTrainingBothDatasetNull() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(null, null);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingPosDatasetNull() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(null, tenRowsDF);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingNegDatasetNull() throws Throwable{
        try {
            Trainer trainer = new Trainer();
            trainer.verifyTraining(tenRowsDF, null);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

}