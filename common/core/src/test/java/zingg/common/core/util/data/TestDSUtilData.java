package zingg.common.core.util.data;

import java.util.ArrayList;
import java.util.List;

import zingg.common.core.util.model.FieldDefnForShowConciseData;
import zingg.common.core.util.model.TrainingData;
import zingg.common.core.util.model.TrainingSamplesData;

public class TestDSUtilData {

    public static List<TrainingSamplesData> getTrainingSamplesData() {

        List<TrainingSamplesData> sample = new ArrayList<TrainingSamplesData>();
        sample.add(new TrainingSamplesData("111","1","The zingg is a Spark application"));
        sample.add(new TrainingSamplesData("111","1","zingg is running on spark"));

        return sample;
    }

    public static List<TrainingData> getTrainingFile() {

        List<TrainingData> sample = new ArrayList<TrainingData>();
        sample.add(new TrainingData("121","1","written java scala","1.0","1.0"));
        sample.add(new TrainingData("121","1","It is written in java and scala","1.0","1.0"));
        sample.add(new TrainingData("101","0","The zingg is a Spark application","1.0","1.0"));
        sample.add(new TrainingData("101","0","It is very popular in data Science","1.0","1.0"));
        return sample;
    }

    public static List<FieldDefnForShowConciseData> getFieldDefnDataForShowConcise(){

        List<FieldDefnForShowConciseData> sample = new ArrayList<FieldDefnForShowConciseData>();
        sample.add(new FieldDefnForShowConciseData("1", "first", "one", "Junit"));
        sample.add(new FieldDefnForShowConciseData("2", "second", "two", "Junit"));
        sample.add(new FieldDefnForShowConciseData("3", "third", "three", "Junit"));
        sample.add(new FieldDefnForShowConciseData("4", "forth", "Four", "Junit"));
        return sample;

    }
    
}
