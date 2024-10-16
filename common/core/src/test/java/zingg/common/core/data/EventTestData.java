package zingg.common.core.data;

import zingg.common.core.model.Event;
import zingg.common.core.model.EventPair;
import zingg.common.core.model.Statement;
import zingg.common.core.model.PostStopWordProcess;
import zingg.common.core.model.PriorStopWordProcess;

import java.util.ArrayList;
import java.util.List;

public class EventTestData {
    public static List<Event> createSampleEventData() {

        int row_id = 1;
        List<Event> sample = new ArrayList<Event>();
        sample.add(new Event(row_id++, 1942, "quit India", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disob", "India"));
        sample.add(new Event(row_id++, 1942, "quit India", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidience", "India"));
        sample.add(new Event(row_id++, 1942, "quit Hindustan", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JW", "Amritsar"));
        sample.add(new Event(row_id++, 1930, "Civil Dis", "India"));
        sample.add(new Event(row_id++, 1942, "quit Nation", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add((new Event(row_id++, 1942, "quit N", "Mahatma")));
        sample.add((new Event(row_id++, 1919, "JallianWal", "Punjb")));
        sample.add(new Event(row_id++, 1942, "quit ", "Mahatm"));
        sample.add(new Event(row_id++, 1942, "quit Ntn", "Mahama"));
        sample.add(new Event(row_id++, 1942, "quit Natin", "Mahaatma"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disob", "India"));
        sample.add(new Event(row_id++, 1942, "quit India", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidience", "India"));
        sample.add(new Event(row_id++, 1942, "Quit Bharat", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidence", "India"));
        sample.add(new Event(row_id++, 1942, "quit Hindustan", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JW", "Amritsar"));
        sample.add(new Event(row_id++, 1930, "Civil Dis", "India"));
        sample.add(new Event(row_id++, 1942, "quit Nation", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit N", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit ", "Mahatm"));
        sample.add(new Event(row_id++, 1942, "quit Ntn", "Mahama"));
        sample.add(new Event(row_id++, 1942, "quit Natin", "Mahaatma"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disob", "India"));
        sample.add(new Event(row_id++, 1942, "quit India", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidience", "India"));
        sample.add(new Event(row_id++, 1942, "Quit Bharat", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidence", "India"));
        sample.add(new Event(row_id++, 1942, "quit Hindustan", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JW", "Amritsar"));
        sample.add(new Event(row_id++, 1930, "Civil Dis", "India"));
        sample.add(new Event(row_id++, 1942, "quit Nation", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit N", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit ", "Mahatm"));
        sample.add(new Event(row_id++, 1942, "quit Ntn", "Mahama"));
        sample.add(new Event(row_id++, 1942, "quit Natin", "Mahaatma"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disob", "India"));
        sample.add(new Event(row_id++, 1942, "quit India", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidience", "India"));
        sample.add(new Event(row_id++, 1942, "Quit Bharat", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JallianWala", "Punjab"));
        sample.add(new Event(row_id++, 1930, "Civil Disobidence", "India"));
        sample.add(new Event(row_id++, 1942, "quit Hindustan", "Mahatma Gandhi"));
        sample.add(new Event(row_id++, 1919, "JW", "Amritsar"));
        sample.add(new Event(row_id++, 1930, "Civil Dis", "India"));
        sample.add(new Event(row_id++, 1942, "quit Nation", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit N", "Mahatma"));
        sample.add(new Event(row_id++, 1919, "JallianWal", "Punjb"));
        sample.add(new Event(row_id++, 1942, "quit ", "Mahatm"));
        sample.add(new Event(row_id++, 1942, "quit Ntn", "Mahama"));
        sample.add(new Event(row_id, 1942, "quit Natin", "Mahaatma"));

        return sample;
    }

    public static List<EventPair> createSampleClusterEventData() {

        int row_id = 1;
        List<EventPair> sample = new ArrayList<EventPair>();
        sample.add(new EventPair(row_id++, 1942, "quit Nation", "Mahatma",1942, "quit Nation", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma",1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventPair(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventPair(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventPair(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventPair(row_id, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));

        return sample;
    }

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
