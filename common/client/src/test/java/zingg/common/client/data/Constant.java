package zingg.common.client.data;


import zingg.common.client.schema.Schema;
import zingg.common.client.schema.SchemaCluster;
import zingg.common.client.schema.SchemaClusterNull;
import zingg.common.client.schema.SchemaInput;
import zingg.common.client.schema.SchemaWithMixedDataType;
import zingg.common.client.schema.SchemaZScore;

import java.util.ArrayList;
import java.util.List;

public class Constant {

    //sample data classes to be used for testing
    public static List<Schema> createEmptySampleData() {

        return new ArrayList<>();
    }

    public static List<Schema> createSampleDataList() {
        List<Schema> sample = new ArrayList<Schema>();
        sample.add(new Schema("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Schema("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Schema("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Schema("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Schema("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Schema("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Schema("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Schema("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Schema("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Schema("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Schema("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<Schema> createSampleDataListDistinct() {
        List<Schema> sample = new ArrayList<Schema>();
        sample.add(new Schema("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Schema("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Schema("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Schema("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Schema("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Schema("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Schema("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Schema("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Schema("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Schema("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<Schema> createSampleDataListWithDistinctSurnameAndPostcode() {
        List<Schema> sample = new ArrayList<Schema>();
        sample.add(new Schema("07317257", "erjc", "henson", "hendersonville", "2873g"));
        sample.add(new Schema("03102490", "jhon", "kozak", "henders0nville", "28792"));
        sample.add(new Schema("02890805", "david", "pisczek", "durham", "27717"));
        sample.add(new Schema("04437063", "e5in", "bbrown", "greenville", "27858"));
        sample.add(new Schema("03211564", "susan", "jones", "greenjboro", "274o7"));

        sample.add(new Schema("04155808", "jerome", "wilkins", "battleborn", "2780g"));
        sample.add(new Schema("05723231", "clarinw", "pastoreus", "elizabeth city", "27909"));
        sample.add(new Schema("06087743", "william", "craven", "greenshoro", "27405"));
        sample.add(new Schema("00538491", "marh", "jackdon", "greensboro", "27406"));
        sample.add(new Schema("01306702", "vonnell", "palmer", "siler sity", "273q4"));

        return sample;
    }

    public static List<SchemaWithMixedDataType> createSampleDataListWithMixedDataType() {
        List<SchemaWithMixedDataType> sample = new ArrayList<SchemaWithMixedDataType>();
        sample.add(new SchemaWithMixedDataType(7317257, "erjc", "henson", 10.021, 2873));
        sample.add(new SchemaWithMixedDataType(3102490, "jhon", "kozak", 3.2434, 28792));
        sample.add(new SchemaWithMixedDataType(2890805, "david", "pisczek", 5436.0232, 27717));
        sample.add(new SchemaWithMixedDataType(4437063, "e5in", "bbrown", 67.0, 27858));
        sample.add(new SchemaWithMixedDataType(3211564, "susan", "jones", 7343.2324, 2747));

        sample.add(new SchemaWithMixedDataType(4155808, "jerome", "wilkins", 50.34, 2780));
        sample.add(new SchemaWithMixedDataType(5723231, "clarinw", "pastoreus", 87.2323, 27909));
        sample.add(new SchemaWithMixedDataType(6087743, "william", "craven", 834.123, 27405));
        sample.add(new SchemaWithMixedDataType(538491, "marh", "jackdon", 123.123, 27406));
        sample.add(new SchemaWithMixedDataType(1306702, "vonnell", "palmer", 83.123, 2734));

        return sample;
    }

    public static List<SchemaZScore> createSampleDataZScore() {

        List<SchemaZScore> sample = new ArrayList<>();
        sample.add(new SchemaZScore(0, 100, 900));
        sample.add(new SchemaZScore(1, 100, 1001));
        sample.add(new SchemaZScore(1, 100, 1002));
        sample.add(new SchemaZScore(1, 100, 2001));
        sample.add(new SchemaZScore(1, 100, 2002));
        sample.add(new SchemaZScore(11, 100, 9002));
        sample.add(new SchemaZScore(3, 300, 3001));
        sample.add(new SchemaZScore(3, 300, 3002));
        sample.add(new SchemaZScore(3, 400, 4001));
        sample.add(new SchemaZScore(4, 400, 4002));

        return sample;
    }

    public static List<SchemaCluster> createSampleDataCluster() {

        List<SchemaCluster> sample = new ArrayList<>();
        sample.add(new SchemaCluster(1, 100, 1001, "b"));
        sample.add(new SchemaCluster(2, 100, 1002, "a"));
        sample.add(new SchemaCluster(3, 100, 2001, "b"));
        sample.add(new SchemaCluster(4, 900, 2002, "c"));
        sample.add(new SchemaCluster(5, 111, 9002, "c"));

        return sample;
    }

    public static List<SchemaClusterNull> createSampleDataClusterWithNull() {

        List<SchemaClusterNull> sample = new ArrayList<>();
        sample.add(new SchemaClusterNull(1, 100, 1001, "b"));
        sample.add(new SchemaClusterNull(2, 100, 1002, "a"));
        sample.add(new SchemaClusterNull(3, 100, 2001, null));
        sample.add(new SchemaClusterNull(4, 900, 2002, "c"));
        sample.add(new SchemaClusterNull(5, 111, 9002, null));

        return sample;
    }

    public static List<SchemaInput> createSampleDataInput() {

        List<SchemaInput> sample = new ArrayList<>();
        sample.add(new SchemaInput(1, "fname1", "b"));
        sample.add(new SchemaInput(2, "fname", "a"));
        sample.add(new SchemaInput(3, "fna", "b"));
        sample.add((new SchemaInput(4, "x", "c")));
        sample.add(new SchemaInput(5, "y", "c"));
        sample.add(new SchemaInput(11, "new1", "b"));
        sample.add(new SchemaInput(22, "new12", "a"));
        sample.add(new SchemaInput(33, "new13", "b"));
        sample.add(new SchemaInput(44, "new14", "c"));
        sample.add(new SchemaInput(55, "new15", "c"));

        return sample;
    }

}
