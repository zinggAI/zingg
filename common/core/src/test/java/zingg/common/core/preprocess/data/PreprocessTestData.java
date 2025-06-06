package zingg.common.core.preprocess.data;

import java.util.ArrayList;
import java.util.List;

import zingg.common.core.preprocess.model.PriorStopWordProcess;

public class PreprocessTestData {

    public static List<PriorStopWordProcess> getDataInputPreProcessed() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess(" 10 ", " The ZINGG IS a SpaRk AppLiCation ", "two",
                " Yes. a good application ", "test"));
        sample.add(new PriorStopWordProcess("20", " It is VERY POpuLar in Data SCIENCE", " Three ", " true indeed ",
                "test"));
        sample.add(new PriorStopWordProcess("30", " It is WRITTEN in java and SCala", "four", "", " test "));
        sample.add(new PriorStopWordProcess("40", " Best of LUCK to zingg Mobile/T-Mobile  ", "Five", " thank you ", "test"));
        sample.add(new PriorStopWordProcess("50"," " , "SIX", null,"test"));

        return sample;
    }

    public static List<PriorStopWordProcess> getDataInputPostProcessed() {

        List<PriorStopWordProcess> sample = new ArrayList<PriorStopWordProcess>();
        sample.add(new PriorStopWordProcess(" 10 ", "zingg spark application", "two", "yes. a good application", "test"));
        sample.add(new PriorStopWordProcess("20", "very popular data science", " Three ", "true indeed", "test"));
        sample.add(new PriorStopWordProcess("30", "written java scala", "four", "", " test "));
        sample.add(new PriorStopWordProcess("40", "best luck to zingg ", "Five", "thank you", "test"));
        sample.add(new PriorStopWordProcess("50","" , "SIX", null,"test"));

        return sample;
    }

}
