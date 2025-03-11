package zingg.common.core.preprocess.casenormalize.data;

import zingg.common.core.model.model.InputDataModel;
import zingg.common.core.preprocess.stopwords.model.PriorStopWordProcess;

import java.util.ArrayList;
import java.util.List;

public class EventTestData {

    public static List<PriorStopWordProcess> getDataInputPreProcessed() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "The ZINGG IS a SpaRk AppLiCation", "two",
                "Yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "It is VERY POpuLar in Data SCIENCE", "Three", "true indeed",
                "test"));
        sample.add(new PriorStopWordProcess("30", "It is WRITTEN in java and SCala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "Best of LUCK to zingg Mobile/T-Mobile", "Five", "thank you", "test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getDataInputPostProcessed() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess("10", "zingg spark application", "two", "yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "very popular data science", "Three", "true indeed", "test"));
        sample.add(new PriorStopWordProcess("30", "written java scala", "four", "", "test"));
        sample.add(new PriorStopWordProcess("40", "best luck to zingg ", "Five", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPreCaseNormalization() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "The ZINGG IS a SPARK AppLiCation", "tWo",
                "Yes. a good Application", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE", "THREE", "TRUE indeed",
                "test"));
        sample.add(new InputDataModel("30", "It is WRITTEN in java and SCala", "four", "", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile", "FIVE", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostCaseNormalizationField1() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "the zingg is a spark application", "tWo",
                "Yes. a good Application", "test"));
        sample.add(new InputDataModel("20", "it is very popular in data science", "THREE", "TRUE indeed",
                "test"));
        sample.add(new InputDataModel("30", "it is written in java and scala", "four", "", "test"));
        sample.add(new InputDataModel("40", "best of luck to zingg mobile/t-mobile", "FIVE", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostCaseNormalizationAllFields() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "the zingg is a spark application", "two",
                "yes. a good application", "test"));
        sample.add(new InputDataModel("20", "it is very popular in data science", "three", "true indeed",
                "test"));
        sample.add(new InputDataModel("30", "it is written in java and scala", "four", "", "test"));
        sample.add(new InputDataModel("40", "best of luck to zingg mobile/t-mobile", "five", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostCaseNormalizationNone() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "The ZINGG IS a SPARK AppLiCation", "tWo",
                "Yes. a good Application", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE", "THREE", "TRUE indeed",
                "test"));
        sample.add(new InputDataModel("30", "It is WRITTEN in java and SCala", "four", "", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile", "FIVE", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostCaseNormalizationWhenMatchTypeDONT_USE() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "The ZINGG IS a SPARK AppLiCation", "two",
                "Yes. a good Application", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE", "three", "TRUE indeed",
                "test"));
        sample.add(new InputDataModel("30", "It is WRITTEN in java and SCala", "four", "", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile", "five", "thank you", "test"));

        return sample;
    }
}
