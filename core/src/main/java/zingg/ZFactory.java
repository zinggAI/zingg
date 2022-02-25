package zingg;

import java.util.HashMap;

import zingg.client.IZingg;
import zingg.client.IZinggFactory;
import zingg.client.ZinggOptions;

public class ZFactory implements IZinggFactory{

    public ZFactory() {}

    public static HashMap<ZinggOptions, String> zinggers = new  HashMap<ZinggOptions, String>();

    static {
        zinggers.put(ZinggOptions.TRAIN, Trainer.name);
        zinggers.put(ZinggOptions.FIND_TRAINING_DATA, TrainingDataFinder.name);
        zinggers.put(ZinggOptions.LABEL, Labeller.name);
        zinggers.put(ZinggOptions.MATCH, Matcher.name);
        zinggers.put(ZinggOptions.TRAIN_MATCH, TrainMatcher.name);
        zinggers.put(ZinggOptions.LINK, Linker.name);
        zinggers.put(ZinggOptions.GENERATE_DOCS, Documenter.name);
        zinggers.put(ZinggOptions.UPDATE_LABEL, LabelUpdater.name);
        zinggers.put(ZinggOptions.FIND_AND_LABEL, FindAndLabeller.name);
    }

    public IZingg get(ZinggOptions z) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return (IZingg) Class.forName(zinggers.get(z)).newInstance();
    }

   
    
}
