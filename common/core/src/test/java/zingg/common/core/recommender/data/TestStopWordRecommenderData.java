package zingg.common.core.recommender.data;

import java.util.ArrayList;
import java.util.List;
import zingg.common.core.recommender.model.WordByCount;

public class TestStopWordRecommenderData {

    public static List<WordByCount> getDataGivenStopWords(){

        List<WordByCount> sample = new ArrayList<WordByCount>();
        sample.add(new WordByCount("the", 44));
		sample.add(new WordByCount("of", 27));
		sample.add(new WordByCount("was", 11));
		sample.add(new WordByCount("in", 11));
		sample.add(new WordByCount("to", 9));
		sample.add(new WordByCount("passengers", 2));
		sample.add(new WordByCount("carried", 2));
		sample.add(new WordByCount("people", 1));
		sample.add(new WordByCount("iceberg", 1));
        return sample;

    }
    
}
