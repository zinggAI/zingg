package zingg.spark.core.executor;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.Trainer;
import zingg.spark.core.util.DataFrameUtility;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.TestSparkBase;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestSparkBase.class)
public class TestTrainer {
    
    public static Dataset<Row> tenRowsDF;
    public static Dataset<Row> oneRowsDF;

    @BeforeAll
    public void setupDF() {
        tenRowsDF = DataFrameUtility.createDFWithDoubles(10,1, TestSparkBase.spark);
        oneRowsDF = DataFrameUtility.createDFWithDoubles(1,1, TestSparkBase.spark);
    }

    @Test
    public void testVerifyTrainingPosDatasetLess() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(new SparkFrame(oneRowsDF), new SparkFrame(tenRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {
        }

    }

    @Test
    public void testVerifyTrainingNegDatasetLess() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(new SparkFrame(tenRowsDF), new SparkFrame(oneRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }
    }
    
    @Test
    public void testVerifyTrainingBothDatasetLess() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(new SparkFrame(oneRowsDF), new SparkFrame(oneRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingBothDatasetMore() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(new SparkFrame(tenRowsDF), new SparkFrame(tenRowsDF));
            
        }
        catch(ZinggClientException e) {
            fail("Exception should not have been thrown when training data is appopriate");
        }

    }

    @Test
    public void testVerifyTrainingBothDatasetNull() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(null, null);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingPosDatasetNull() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(null, new SparkFrame(tenRowsDF));
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

    @Test
    public void testVerifyTrainingNegDatasetNull() throws Throwable{
        try {
            Trainer trainer = new SparkTrainer();
            trainer.verifyTraining(new SparkFrame(tenRowsDF), null);
            fail("Expected exception not getting thrown when training data is less");
        }
        catch(ZinggClientException e) {

        }

    }

}