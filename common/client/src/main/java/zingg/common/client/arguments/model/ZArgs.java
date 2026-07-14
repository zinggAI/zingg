package zingg.common.client.arguments.model;

import java.io.IOException;
import java.io.StringWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

import com.fasterxml.jackson.core.JsonParser;


public abstract class ZArgs implements IZArgs {
    String zinggDir = IZArgs.ZINGG_DIR;
    String modelId = IZArgs.MODEL_ID;
    int jobId = 1;
	boolean collectMetrics = true;
	Pipe[] output; 


	@Override
	public String getModelId() {
		return modelId;
	}

	@Override
	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

    /**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	@Override
	public String getZinggDir() {
		return zinggDir;
	}

	/**
	 * Set the location for Zingg to save its internal computations and
	 * models. Please set it to a place where the program has write access.
	 * 
	 * @param zinggDir
	 *            path to the Zingg directory
	 */
	@Override
	public void setZinggDir(String zinggDir) {
		this.zinggDir = zinggDir;
	}

    /**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */

	

	@Override
	public int getJobId() {
		return jobId;
	}



	@Override
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}


	@Override
	public boolean getCollectMetrics() {
		return collectMetrics;
	}

	@Override
	public void setCollectMetrics(boolean collectMetrics) {
		this.collectMetrics = collectMetrics;
	}
	
    @Override
	public String toString() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS,
				true);
		//mapper.configure(JsonParser.Feature.FAIL_ON_EMPTY_BEANS, true)
		try {
			StringWriter writer = new StringWriter();
			return mapper.writeValueAsString(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Get the output directory where the match output will be saved
	 * 
	 * @return output directory path of the result
	 */
	@Override
	public Pipe[] getOutput() {
		return output;
	}

	/**
	 * Set the output directory where the match result will be saved
	 * 
	 * @param outputDir
	 *            where the match result is saved
	 * @throws ZinggClientException
	 */
	@Override
	public void setOutput(Pipe[] outputDir) throws ZinggClientException {
		//checkNullBlankEmpty(outputDir, " path for saving results");
		this.output = outputDir;
	}


}
