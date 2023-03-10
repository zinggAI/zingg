package zingg.spark.core.executor;

import java.util.HashMap;

import zingg.common.client.IZingg;
import zingg.common.client.IZinggFactory;
import zingg.common.client.ZinggOptions;
import zingg.spark.core.executor.SparkDocumenter;
import zingg.spark.core.executor.SparkFindAndLabeller;
import zingg.spark.core.executor.SparkLabelUpdater;
import zingg.spark.core.executor.SparkLabeller;
import zingg.spark.core.executor.SparkLinker;
import zingg.spark.core.executor.SparkMatcher;
import zingg.spark.core.executor.SparkRecommender;
import zingg.spark.core.executor.SparkTrainMatcher;
import zingg.spark.core.executor.SparkTrainer;
import zingg.spark.core.executor.SparkTrainingDataFinder;

public class SparkZFactory implements IZinggFactory{

    public SparkZFactory() {}

    public static HashMap<ZinggOptions, String> zinggers = new  HashMap<ZinggOptions, String>();

    static {
        zinggers.put(ZinggOptions.TRAIN, SparkTrainer.name);
        zinggers.put(ZinggOptions.FIND_TRAINING_DATA, SparkTrainingDataFinder.name);
        zinggers.put(ZinggOptions.LABEL, SparkLabeller.name);
        zinggers.put(ZinggOptions.MATCH, SparkMatcher.name);
        zinggers.put(ZinggOptions.TRAIN_MATCH, SparkTrainMatcher.name);
        zinggers.put(ZinggOptions.LINK, SparkLinker.name);
        zinggers.put(ZinggOptions.GENERATE_DOCS, SparkDocumenter.name);
        zinggers.put(ZinggOptions.UPDATE_LABEL, SparkLabelUpdater.name);
        zinggers.put(ZinggOptions.FIND_AND_LABEL, SparkFindAndLabeller.name);
        zinggers.put(ZinggOptions.RECOMMEND, SparkRecommender.name);
        zinggers.put(ZinggOptions.PEEK_MODEL, SparkPeekModel.name);
    }

    public IZingg get(ZinggOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (IZingg) Class.forName(zinggers.get(z)).newInstance();
    }

   
    
}
