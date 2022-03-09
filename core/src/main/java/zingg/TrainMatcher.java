package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.snowflake.snowpark_java.Column;
import com.snowflake.snowpark_java.DataFrame;
import com.snowflake.snowpark_java.Row;
import com.snowflake.snowpark_java.Functions;

import scala.collection.JavaConverters;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.Util;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;

public class TrainMatcher extends Matcher{

	protected static String name = "zingg.TrainMatcher";
	public static final Log LOG = LogFactory.getLog(TrainMatcher.class); 
	
	private Trainer trainer;

    public TrainMatcher() {
        setZinggOptions(ZinggOptions.MATCH);
		trainer = new Trainer();
    }

	@Override
	public void init(Arguments args, String license)
        throws ZinggClientException {
			super.init(args, license);
			trainer.copyContext(this);
	}

	@Override
    public void execute() throws ZinggClientException {
		trainer.execute();
		super.execute();
	}


	
	    
}
