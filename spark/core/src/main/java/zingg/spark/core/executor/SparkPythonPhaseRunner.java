package zingg.spark.core.executor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.PythonRunner;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.options.ZinggOptions;

import zingg.common.core.executor.ZinggBase;
import zingg.spark.core.context.ZinggSparkContext;


public class SparkPythonPhaseRunner extends ZinggBase<SparkSession, Dataset<Row>, Row, Column, DataType>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.core.executor.SparkPythonPhaseRunner";
	public static final Log LOG = LogFactory.getLog(SparkPythonPhaseRunner.class);
	
	public SparkPythonPhaseRunner() {
		setZinggOption(ZinggOptions.PEEK_MODEL);
		setContext(new ZinggSparkContext());
		
	}

	@Override
    public void init(IZArgs args, SparkSession s, ClientOptions options)
        throws ZinggClientException {
		super.init(args,s,options);
		getContext().setUtils();
		//we wil not init here as we wnt py to drive
		//the spark session etc
		getContext().init(s);
    }

	@Override
	public void execute() throws ZinggClientException {
		try {
			//closing session here
			//as pyspark will further create it
			//TODO getOrCreate not working in pyspark
			SparkSession sparkSession = context.getSession();
			sparkSession.stop();
			LOG.info("Generic Python phase starts");
			//LOG.info(this.getClass().getClassLoader().getResource("python/phases/assessModel.py").getFile());
			List<String> pyArgs = new ArrayList<String>();
			String phase = clientOptions.get(ClientOptions.PHASE).getValue();
			pyArgs.add("python/phases/" + phase + ".py");
			pyArgs.add("");
			for (String c: clientOptions.getCommandLineArgs()) {
				pyArgs.add(c);
			}
			PythonRunner.main(pyArgs.toArray(new String[pyArgs.size()]));

			LOG.info("Generic Python phase ends");
		} catch (Exception exception) {
			throw new ZinggClientException("Error occurred while executing python phase, ", exception);
		}
	}


} 
