package zingg.common.core.preprocess.trim.data;

import java.util.ArrayList;
import java.util.List;

import zingg.common.core.model.model.InputDataModel;

public class TrimTestData {

    public static List<InputDataModel> getDataInputPreTrim() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", " The ZINGG IS a SPARK AppLiCation", "tWo ",
                "Yes. a good Application ", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE ", " THREE", "TRUE indeed ",
                "test"));
        sample.add(new InputDataModel("30", " It is WRITTEN in java and SCala", "four ", "      ", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile ", " FIVE", "thank you ", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostTrimOnField1() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "The ZINGG IS a SPARK AppLiCation", "tWo ",
                "Yes. a good Application ", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE", " THREE", "TRUE indeed ",
                "test"));
        sample.add(new InputDataModel("30", "It is WRITTEN in java and SCala", "four ", "      ", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile", " FIVE", "thank you ", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostTrimOnAllFields() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", "The ZINGG IS a SPARK AppLiCation", "tWo",
                "Yes. a good Application", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE", "THREE", "TRUE indeed",
                "test"));
        sample.add(new InputDataModel("30", "It is WRITTEN in java and SCala", "four", "", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile", "FIVE", "thank you", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostTrimOnNone() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", " The ZINGG IS a SPARK AppLiCation", "tWo ",
                "Yes. a good Application ", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE ", " THREE", "TRUE indeed ",
                "test"));
        sample.add(new InputDataModel("30", " It is WRITTEN in java and SCala", "four ", "      ", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile ", " FIVE", "thank you ", "test"));

        return sample;
    }

    public static List<InputDataModel> getDataInputPostTrimWhenMatchTypeDont_Use() {

        List<InputDataModel> sample = new ArrayList<InputDataModel>();
        sample.add(new InputDataModel("10", " The ZINGG IS a SPARK AppLiCation", "tWo",
                "Yes. a good Application ", "test"));
        sample.add(new InputDataModel("20", "It is VERY POpuLar in Data SCIENCE ", "THREE", "TRUE indeed ",
                "test"));
        sample.add(new InputDataModel("30", " It is WRITTEN in java and SCala", "four", "      ", "test"));
        sample.add(new InputDataModel("40", "Best of LUCK to ZINGG Mobile/T-Mobile ", "FIVE", "thank you ", "test"));

        return sample;
    }
    
}
