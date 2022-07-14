package zingg;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import org.apache.spark.deploy.PythonRunner;

public class PeekModel extends ZinggBase{

	protected static String name = "zingg.PeekModel";
	public static final Log LOG = LogFactory.getLog(PeekModel.class); 
	
	public PeekModel() {
		setZinggOptions(ZinggOptions.PEEK_MODEL);
	}

	@Override
    public void init(Arguments args, String license)
        throws ZinggClientException {
        startTime = System.currentTimeMillis();
        this.args = args;        
    }

	@Override
	public void execute() throws ZinggClientException {
		try {
			LOG.info("Generic Python phase starts");
			
			PythonRunner.main(new String[]{"python/phases/assessModel.py",
				 "pyFiles", 
				 "--phase", 
				 "peekModel", 
				 "--conf", 
				 args.getConfFile()
				});

			LOG.info("Generic Python phase ends");
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZinggClientException(e.getMessage());
		}
	}

} 
