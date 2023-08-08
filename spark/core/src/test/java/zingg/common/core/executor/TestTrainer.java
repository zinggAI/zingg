/*
 * Zingg
 * Copyright (C) 2021-Present  Zingg Labs,inc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package zingg.common.core.executor;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import zingg.common.client.ZinggClientException;
import zingg.spark.client.SparkFrame;
import zingg.spark.core.executor.SparkTrainer;
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