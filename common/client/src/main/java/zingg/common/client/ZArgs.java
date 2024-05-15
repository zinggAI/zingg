package zingg.common.client;

public abstract class ZArgs implements IZArgs{
    String zinggDir = IZArgs.ZINGG_DIR;
    String modelId = IZArgs.MODEL_ID;
    int jobId = 1;
	boolean collectMetrics = true;


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
	


}
