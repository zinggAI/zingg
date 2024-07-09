package zingg.common.core.data;

import zingg.common.core.model.Event;
import zingg.common.core.model.EventCluster;
import zingg.common.core.model.Schema;
import zingg.common.core.model.SchemaActual;
import zingg.common.core.model.SchemaOriginal;

import java.util.ArrayList;
import java.util.List;

public class Constant {
    public static List<Event> createSampleEventData() {

        int row_id = 1;
        List<Event> sample = new ArrayList<>();
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

    public static List<EventCluster> createSampleClusterEventData() {

        int row_id = 1;
        List<EventCluster> sample = new ArrayList<>();
        sample.add(new EventCluster(row_id++, 1942, "quit Nation", "Mahatma",1942, "quit Nation", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma",1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit N", "Mahatma", 1942, "quit N", "Mahatma", 1L));
        sample.add(new EventCluster(row_id++, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));
        sample.add(new EventCluster(row_id++, 1942, "quit ", "Mahatm", 1942, "quit ", "Mahatm", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Ntn", "Mahama", 1942, "quit Ntn", "Mahama", 1L));
        sample.add(new EventCluster(row_id++, 1942, "quit Natin", "Mahaatma", 1942, "quit Natin", "Mahaatma", 1L));
        sample.add(new EventCluster(row_id, 1919, "JallianWal", "Punjb", 1919, "JallianWal", "Punjb", 2L));

        return sample;
    }

    public static List<Schema> getData1Original() {

        List<Schema> sample = new ArrayList<>();
        sample.add(new Schema("The zingg is a Spark application"));
        sample.add(new Schema("It is very popular in data Science"));
        sample.add(new Schema("It is written in Java and Scala"));
        sample.add(new Schema("Best of luck to zingg"));

        return sample;
    }

    public static List<Schema> getData1Expected() {

        List<Schema> sample = new ArrayList<>();
        sample.add(new Schema("zingg spark application"));
        sample.add(new Schema("very popular in data science"));
        sample.add(new Schema("written in java and scala"));
        sample.add(new Schema("best luck to zingg"));

        return sample;
    }

    public static List<SchemaOriginal> getData2Original() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "It is very popular in Data Science", "Three", "true indeed",
                "test"));
        sample.add(new SchemaOriginal("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaOriginal> getData2Expected() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "zingg spark application", "two", "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "very popular data science", "Three", "true indeed", "test"));
        sample.add(new SchemaOriginal("30", "written java scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "best luck to zingg ", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaOriginal> getData3Original() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "It is very popular in Data Science", "Three", "true indeed",
                "test"));
        sample.add(new SchemaOriginal("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "Best of luck to zingg Mobile/T-Mobile", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaOriginal> getData3Expected() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "zingg spark application", "two", "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "very popular data science", "Three", "true indeed", "test"));
        sample.add(new SchemaOriginal("30", "written java scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "best luck to zingg ", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaOriginal> getData4original() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "It is very popular in data science", "Three", "true indeed",
                "test"));
        sample.add(new SchemaOriginal("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "Best of luck to zingg", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaActual> getData4Expected() {

        List<SchemaActual> sample = new ArrayList<>();
        sample.add(new SchemaActual("1648811730857:10", "10", "1.0", "0.555555", "-1",
                "The zingg spark application", "two", "Yes. good application", "test"));
        sample.add(new SchemaActual("1648811730857:20", "20", "1.0", "1.0", "-1",
                "It very popular data science", "Three", "true indeed", "test"));
        sample.add(new SchemaActual("1648811730857:30", "30", "1.0", "0.999995", "-1",
                "It written java scala", "four", "", "test"));
        sample.add(new SchemaActual("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
                "thank", "test"));

        return sample;
    }

    public static List<SchemaOriginal> getData5Original() {

        List<SchemaOriginal> sample = new ArrayList<>();
        sample.add(new SchemaOriginal("10", "The zingg is a spark application", "two",
                "Yes. a good application", "test"));
        sample.add(new SchemaOriginal("20", "It is very popular in data science", "Three", "true indeed",
                "test"));
        sample.add(new SchemaOriginal("30", "It is written in java and scala", "four", "", "test"));
        sample.add(new SchemaOriginal("40", "Best of luck to zingg", "Five", "thank you", "test"));

        return sample;
    }

    public static List<SchemaActual> getData5Actual() {

        List<SchemaActual> sample = new ArrayList<>();
        sample.add(new SchemaActual("1648811730857:10", "10", "1.0", "0.555555", "-1",
                "The zingg spark application", "two", "Yes. good application", "test"));
        sample.add(new SchemaActual("1648811730857:20", "20", "1.0", "1.0", "-1",
                "It very popular data science", "Three", "true indeed", "test"));
        sample.add(new SchemaActual("1648811730857:30", "30", "1.0", "0.999995", "-1",
                "It written java scala", "four", "", "test"));
        sample.add(new SchemaActual("1648811730857:40", "40", "1.0", "1.0", "-1", "Best luck zingg", "Five",
                "thank", "test"));

        return sample;
    }
}
