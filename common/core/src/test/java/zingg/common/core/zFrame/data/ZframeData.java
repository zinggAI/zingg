package zingg.common.core.zFrame.data;


import java.util.ArrayList;
import java.util.List;

import zingg.common.core.zFrame.model.ClusterZScore;
import zingg.common.core.zFrame.model.InputWithZidAndSource;
import zingg.common.core.zFrame.model.PairPartOne;
import zingg.common.core.zFrame.model.PairPartTwo;
import zingg.common.core.zFrame.model.Person;
import zingg.common.core.zFrame.model.PersonMixed;

public class ZframeData {

    //sample data classes to be used for testing
    public static List<Person> createEmptySampleData() {

        return new ArrayList<Person>();
    }

    public static List<Person> createSampleDataList() {
        List<Person> sample = new ArrayList<Person>();
        sample.add(new Person("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Person("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Person("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Person("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Person("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Person("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Person("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Person("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Person("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Person("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Person("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<Person> createSampleDataListDistinct() {
        List<Person> sample = new ArrayList<Person>();
        sample.add(new Person("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Person("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Person("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Person("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Person("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Person("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Person("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Person("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Person("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Person("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<Person> createSampleDataListWithDistinctSurnameAndPostcode() {
        List<Person> sample = new ArrayList<Person>();
        sample.add(new Person("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Person("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Person("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Person("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Person("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Person("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Person("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Person("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Person("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Person("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<PersonMixed> createSampleDataListWithMixedDataType() {
        List<PersonMixed> sample = new ArrayList<PersonMixed>();
        sample.add(new PersonMixed(7317257, "erjc", "henson", 10.021, 2873));
        sample.add(new PersonMixed(3102490, "jhon", "kozak", 3.2434, 28792));
        sample.add(new PersonMixed(2890805, "david", "pisczek", 5436.0232, 27717));
        sample.add(new PersonMixed(4437063, "e5in", "bbrown", 67.0, 27858));
        sample.add(new PersonMixed(3211564, "susan", "jones", 7343.2324, 2747));

        sample.add(new PersonMixed(4155808, "jerome", "wilkins", 50.34, 2780));
        sample.add(new PersonMixed(5723231, "clarinw", "pastoreus", 87.2323, 27909));
        sample.add(new PersonMixed(6087743, "william", "craven", 834.123, 27405));
        sample.add(new PersonMixed(538491, "marh", "jackdon", 123.123, 27406));
        sample.add(new PersonMixed(1306702, "vonnell", "palmer", 83.123, 2734));

        return sample;
    }

    public static List<ClusterZScore> createSampleDataZScore() {

        List<ClusterZScore> sample = new ArrayList<ClusterZScore>();
        sample.add(new ClusterZScore(0L, "100", 900.0));
        sample.add(new ClusterZScore(1L, "100", 1001.0));
        sample.add(new ClusterZScore(1L, "100", 1002.0));
        sample.add(new ClusterZScore(1L, "100", 2001.0));
        sample.add(new ClusterZScore(1L, "100", 2002.0));
        sample.add(new ClusterZScore(11L, "100", 9002.0));
        sample.add(new ClusterZScore(3L, "300", 3001.0));
        sample.add(new ClusterZScore(3L, "300", 3002.0));
        sample.add(new ClusterZScore(3L, "400", 4001.0));
        sample.add(new ClusterZScore(4L, "400", 4002.0));

        return sample;
    }

    public static List<PairPartOne> createSampleDataCluster() {

        List<PairPartOne> sample = new ArrayList<PairPartOne>();
        sample.add(new PairPartOne(1L, "100", 1001.0, "b"));
        sample.add(new PairPartOne(2L, "100", 1002.0, "a"));
        sample.add(new PairPartOne(3L, "100", 2001.0, "b"));
        sample.add(new PairPartOne(4L, "900", 2002.0, "c"));
        sample.add(new PairPartOne(5L, "111", 9002.0, "c"));

        return sample;
    }

    public static List<PairPartTwo> createSampleDataClusterWithNull() {

        List<PairPartTwo> sample = new ArrayList<PairPartTwo>();
        sample.add(new PairPartTwo(1L, "100", 1001.0, "b"));
        sample.add(new PairPartTwo(2L, "100", 1002.0, "a"));
        sample.add(new PairPartTwo(3L, "100", 2001.0, null));
        sample.add(new PairPartTwo(4L, "900", 2002.0, "c"));
        sample.add(new PairPartTwo(5L, "111", 9002.0, null));

        return sample;
    }

    public static List<InputWithZidAndSource> createSampleDataInput() {

        List<InputWithZidAndSource> sample = new ArrayList<InputWithZidAndSource>();
        sample.add(new InputWithZidAndSource(1L, "fname1", "b"));
        sample.add(new InputWithZidAndSource(2L, "fname", "a"));
        sample.add(new InputWithZidAndSource(3L, "fna", "b"));
        sample.add((new InputWithZidAndSource(4L, "x", "c")));
        sample.add(new InputWithZidAndSource(5L, "y", "c"));
        sample.add(new InputWithZidAndSource(11L, "new1", "b"));
        sample.add(new InputWithZidAndSource(22L, "new12", "a"));
        sample.add(new InputWithZidAndSource(33L, "new13", "b"));
        sample.add(new InputWithZidAndSource(44L, "new14", "c"));
        sample.add(new InputWithZidAndSource(55L, "new15", "c"));

        return sample;
    }

}
