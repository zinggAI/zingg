package zingg.spark.core.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.PythonRunner;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.core.executor.ZinggBase;

public class SparkPeekModel extends ZinggBase<SparkSession, Dataset<Row>, Row, Column, DataType>{

	protected static String name = "zingg.spark.core.executor.SparkPeekModel";
	public static final Log LOG = LogFactory.getLog(SparkPeekModel.class); 
	
	public SparkPeekModel() {
		setZinggOptions(ZinggOptions.PEEK_MODEL);
		setContext(new ZinggSparkContext());
	}

	@Override
    public void init(Arguments args, String license)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        this.args = args;     
		//dont call init here as spark session already created   
		getContext().setUtils();
    }

	@Override
	public void execute() throws ZinggClientException {
		try {
			LOG.info("Generic Python phase starts");
			//LOG.info(this.getClass().getClassLoader().getResource("python/phases/assessModel.py").getFile());
			List<String> pyArgs = new ArrayList<String>();
			pyArgs.add("python/phases/"+clientOptions.get(ClientOptions.PHASE).getValue() + ".py");
			pyArgs.add("");
			for (String c: clientOptions.getCommandLineArgs()) {
				pyArgs.add(c);
			}
			PythonRunner.main(pyArgs.toArray(new String[pyArgs.size()]));

			LOG.info("Generic Python phase ends");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

	

} 
