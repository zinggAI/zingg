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

import zingg.common.client.Arguments;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZinggOptions;
import zingg.common.client.license.IZinggLicense;
import zingg.common.core.executor.ZinggBase;
import zingg.spark.client.ZSparkSession;

public class SparkPeekModel extends ZinggBase<ZSparkSession, Dataset<Row>, Row, Column, DataType>{

	private static final long serialVersionUID = 1L;
	protected static String name = "zingg.spark.core.executor.SparkPeekModel";
	public static final Log LOG = LogFactory.getLog(SparkPeekModel.class); 
	
	public SparkPeekModel() {
		//setZinggOptions(ZinggOptions.PEEK_MODEL);
		setContext(new ZinggSparkContext());
		
	}

	@Override
    public void init(Arguments args, IZinggLicense license)
        throws ZinggClientException {
		super.init(args, license);
		getContext().setUtils();
		//we wil not init here as we wnt py to drive
		//the spark session etc
		//getContext().init(license);
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


} 
