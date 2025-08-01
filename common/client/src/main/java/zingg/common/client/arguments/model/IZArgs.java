package zingg.common.client.arguments.model;

import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.Pipe;

/**
 * Marker interface for all Zingg
 */
public interface IZArgs {

    String ZINGG_DIR = "/tmp/zingg";
    String MODEL_ID = "1";

    boolean getCollectMetrics();

	void setCollectMetrics(boolean collectMetrics);
    
    /**
	 * Set the location for Zingg to save its internal computations and
	 * models. Please set it to a place where the program has write access.
	 * 
	 * @param zinggDir
	 *            path to the Zingg directory
	
	public void setZinggInternal(Pipe[] zinggDir) {
		this.zinggInternal = zinggDir;
	}
	*/

	String getModelId();

	void setModelId(String modelId);

    /**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggDir();

	/**
	 * Set the location for Zingg to save its internal computations and
	 * models. Please set it to a place where the program has write access.
	 * 
	 * @param zinggDir
	 *            path to the Zingg directory
	 */
	void setZinggDir(String zinggDir);

	void setJobId(int jobId);

	int getJobId();

	/**
	 * Get the output directory where the match output will be saved
	 * 
	 * @return output directory path of the result
	 */
	Pipe[] getOutput();

	/**
	 * Set the output directory where the match result will be saved
	 * 
	 * @param outputDir
	 *            where the match result is saved
	 * @throws ZinggClientException
	 */
	void setOutput(Pipe[] outputDir) throws ZinggClientException;

}
