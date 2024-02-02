package zingg.common.core.executor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);
	
	protected IArguments args;
	

	protected S session;
	
	protected final String PARENT_CONFIG_FILE = "PARENT-CONFIG-FILE";
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;
		// set up args
		String configFile = setupArgs();					
	}

	public String setupArgs() throws ZinggClientException, IOException {
		String configFile = getClass().getClassLoader().getResource(getConfigFile()).getFile();
		args = new ArgumentsUtil().createArgumentsFromJSON(
			configFile, 
			"findTrainingData");
		return configFile;
	}

	public abstract String getConfigFile();
	
	public abstract String getConfigIncrFile();
	
	public abstract String getConfigApproveFile();
	

	public void testExecutors(List<ExecutorTester<S, D, R, C, T>> executorTesterList) throws ZinggClientException {
		for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
			executorTester.execute();
			executorTester.validateResults();
		}
	}	

	public abstract void tearDown();
	
	public String getFileContentAsStr(String filePath) throws IOException {		

		StringBuilder fileContent = new StringBuilder();
		
		try (
			InputStream ioStream = this.getClass().getClassLoader().getResourceAsStream(filePath);	
			InputStreamReader streamReader = new InputStreamReader(ioStream);
			BufferedReader reader = new BufferedReader(streamReader);
		)
		{
			for (String line; (line = reader.readLine()) != null;) {
				fileContent.append(line);
				fileContent.append("\n");
			}
		}		
		return fileContent.toString();
	}
	
	
}
