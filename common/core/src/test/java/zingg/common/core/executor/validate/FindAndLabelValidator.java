package zingg.common.core.executor.validate;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.ColName;
import zingg.common.core.executor.FindAndLabeller;

public class FindAndLabelValidator<S, D, R, C, T> extends ExecutorValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(FindAndLabelValidator.class);

    public TrainingDataFinderValidator<S, D, R, C, T> tdfv; 
    public LabellerValidator <S, D, R, C, T> lv; 
	
	  public FindAndLabelValidator(FindAndLabeller<S, D, R, C, T> executor) {
		    super(executor);
            this.tdfv = new TrainingDataFinderValidator<S, D, R, C, T>(executor.getFinder());
            this.lv = new LabellerValidator <S, D, R, C, T>(executor.getLabeller());
    }

    @Override
    public void validateResults() throws ZinggClientException {
        tdfv.validateResults();
        lv.validateResults();
    }
    
}
