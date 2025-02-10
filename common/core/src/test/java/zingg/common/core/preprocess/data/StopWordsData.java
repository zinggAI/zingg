package zingg.common.core.preprocess.data;

import java.util.ArrayList;
import java.util.List;

import zingg.common.core.preprocess.model.PostStopWordProcess;
import zingg.common.core.preprocess.model.PriorStopWordProcess;
import zingg.common.core.preprocess.model.Statement;

public class StopWordsData {

    public static List<Statement> getData1Original() {

        List<Statement> sample = new ArrayList<Statement>();
        sample.add(new Statement("The zingg is a Spark application"));
        sample.add(new Statement("It is very popular in data Science"));
        sample.add(new Statement("It is written in Java and Scala"));
        sample.add(new Statement("Best of luck to zingg"));

        return sample;
    }

    public static List<Statement> getData1Expected() {

        List<Statement> sample = new ArrayList<Statement>();
        sample.add(new Statement("zingg spark application"));
        sample.add(new Statement("very popular in data science"));
        sample.add(new Statement("written in java and scala"));
        sample.add(new Statement("best luck to zingg"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData2Original() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "It is very popular in Data Science", "Three", "true indeed",
                "test"));
        sample.add(new PriorStopWordProcess("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData2Expected() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "zingg spark application", "two", "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "very popular data science", "Three", "true indeed", "test"));
        sample.add(new PriorStopWordProcess("30", "written java scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "best luck to zingg ", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData3Original() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "It is very popular in Data Science", "Three", "true indeed",
                "test"));
        sample.add(new PriorStopWordProcess("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData3Expected() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "zingg spark application", "two", "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "very popular data science", "Three", "true indeed", "test"));
        sample.add(new PriorStopWordProcess("30", "written java scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "best luck to zingg ", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData4original() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "It is very popular in data science", "Three", "true indeed",
                "test"));
        sample.add(new PriorStopWordProcess("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "Best of luck to zingg", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PostStopWordProcess> getData4Expected() {

        List<PostStopWordProcess> sample = new ArrayList<PostStopWordProcess>();
        sample.add(new PostStopWordProcess("1648811730857:10", "10", "1.0", "0.555555", "-1",
                "The zingg spark application", "two", "Yes. good application", "test"));
        sample.add(new PostStopWordProcess("1648811730857:20", "20", "1.0", "1.0", "-1",
                "It very popular data science", "Three", "true indeed", "test"));
        sample.add(new PostStopWordProcess("1648811730857:30", "30", "1.0", "0.999995", "-1",
                "It written java scala", "four", "", "test"));
        sample.add(new PostStopWordProcess("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
                "thank", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getData5Original() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "It is very popular in data science", "Three", "true indeed",
                "test"));
        sample.add(new PriorStopWordProcess("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "Best of luck to zingg", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PostStopWordProcess> getData5Actual() {

        List<PostStopWordProcess> sample = new ArrayList<PostStopWordProcess>();
        sample.add(new PostStopWordProcess("1648811730857:10", "10", "1.0", "0.555555", "-1",
                "The zingg spark application", "two", "Yes. good application", "test"));
        sample.add(new PostStopWordProcess("1648811730857:20", "20", "1.0", "1.0", "-1",
                "It very popular data science", "Three", "true indeed", "test"));
        sample.add(new PostStopWordProcess("1648811730857:30", "30", "1.0", "0.999995", "-1",
                "It written java scala", "four", "", "test"));
        sample.add(new PostStopWordProcess("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
                "thank", "test"));

        return sample;
    }
    
}
