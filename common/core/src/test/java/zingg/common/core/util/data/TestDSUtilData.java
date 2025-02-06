package zingg.common.core.util.data;

import java.util.ArrayList;
import java.util.List;

import zingg.common.core.util.model.TestShowConciseData;
import zingg.common.core.util.model.TestTrainingData;
import zingg.common.core.util.model.TrainingSamplesData;

public class TestDSUtilData {

    public static List<TrainingSamplesData> getTrainingSamplesData() {

        List<TrainingSamplesData> sample = new ArrayList<TrainingSamplesData>();
        sample.add(new TrainingSamplesData("111","1","The zingg is a Spark application"));
        sample.add(new TrainingSamplesData("111","1","zingg is running on spark"));

        return sample;
    }

    public static List<TestTrainingData> getTrainingFile() {

        List<TestTrainingData> sample = new ArrayList<TestTrainingData>();
        sample.add(new TestTrainingData("121","1","written java scala","1.0","1.0"));
        sample.add(new TestTrainingData("121","1","It is written in java and scala","1.0","1.0"));
        sample.add(new TestTrainingData("101","0","The zingg is a Spark application","1.0","1.0"));
        sample.add(new TestTrainingData("101","0","It is very popular in data Science","1.0","1.0"));
        return sample;
    }

    public static List<TestShowConciseData> getFieldDefnDataForShowConcise(){

        List<TestShowConciseData> sample = new ArrayList<TestShowConciseData>();
        sample.add(new TestShowConciseData("1", "first", "one", "Junit"));
        sample.add(new TestShowConciseData("2", "second", "two", "Junit"));
        sample.add(new TestShowConciseData("3", "third", "three", "Junit"));
        sample.add(new TestShowConciseData("4", "forth", "Four", "Junit"));
        return sample;

    }
    
}
