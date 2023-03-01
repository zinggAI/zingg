package zingg.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.PythonRunner;

import zingg.ZinggBase;
import zingg.client.Arguments;
import zingg.client.ClientOptions;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;

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
