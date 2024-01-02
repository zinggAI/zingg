package zingg.common.client;

import java.util.List;

import zingg.common.client.pipe.Pipe;

public interface IArguments {

	void setThreshold(double threshold);

	int getNumPartitions();

	void setNumPartitions(int numPartitions) throws ZinggClientException;

	/**
	 * Sample size to use for seeding labelled data We dont want to run over all
	 * the data, as we want a quick way to seed some labeled data which we can
	 * manually edit
	 * 
	 * @return sample percent as a float between 0 and 1
	 */

	float getLabelDataSampleSize();

	/**
	 * Set the fraction of data to be used from complete data set to be used for
	 * seeding the labelled data Labelling is costly and we want a fast
	 * approximate way of looking at a small sample of the records and
	 * identifying expected matches and non matches
	 * 
	 * @param labelDataSampleSize
	 *            - float between 0 and 1 denoting portion of dataset to use in
	 *            generating seed samples
	 * @throws ZinggClientException 
	 */
	void setLabelDataSampleSize(float labelDataSampleSize) throws ZinggClientException;

	/**
	 * get the field definitions associated with this client
	 * 
	 * @return list of field definitions
	 */
	List<? extends FieldDefinition> getFieldDefinition();

	/**
	 * Set the field definitions consisting of match field indices, types and
	 * classes
	 * 
	 * @see FieldDefinition
	 * @param fieldDefinition
	 *            list of fields
	 * @throws ZinggClientException 
	 */
	void setFieldDefinition(List<? extends FieldDefinition> fieldDefinition) throws ZinggClientException;

	/**
	 * Return the path to the positive labeled samples file
	 * 
	 * @return path to labeled positive sample file
	 */
	Pipe[] getTrainingSamples();

	/**
	 * Set the path to the positive training sample file
	 * 
	 * @param positiveTrainingSamples
	 *            path of the matching (positive)labeled sample file
	 * @throws ZinggClientException 
	 */
	void setTrainingSamples(Pipe[] trainingSamples) throws ZinggClientException;

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	
	public Pipe[] getZinggInternal() {
		return zinggInternal;
	}
	
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

	/**
	 * Get the location of the data file over which the match will be run
	 * 
	 * @return path of data file to be matched
	 */
	Pipe[] getData();

	/**
	 * Set the file path of the file to be matched.
	 * 
	 * @param dataFile
	 *            - full file path
	 *            /home/zingg/path/to/my/file/to/be/matched.csv
	 * @throws ZinggClientException 
	 */
	void setData(Pipe[] dataFile) throws ZinggClientException;

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

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */

	String getZinggBaseModelDir();

	String getZinggModelDir();

	String getZinggDocDir();

	String getZinggModelDocFile();

	String getZinggDataDocFile();

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggBaseTrainingDataDir();

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggTrainingDataUnmarkedDir();

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggTrainingDataMarkedDir();

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggPreprocessedDataDir();

	/**
	 * This is an internal block file location Not to be used directly by the
	 * client
	 * 
	 * @return the blockFile
	 */
	String getBlockFile();

	/**
	 * This is the internal model location Not to be used by the client
	 * 
	 * @return model path
	 */
	String getModel();

	int getJobId();

	void setJobId(int jobId);

	boolean getCollectMetrics();

	void setCollectMetrics(boolean collectMetrics);

	float getStopWordsCutoff();

	void setStopWordsCutoff(float stopWordsCutoff) throws ZinggClientException;

	boolean getShowConcise();

	void setShowConcise(boolean showConcise);

	String getColumn();

	void setColumn(String column);

	long getBlockSize();

	void setBlockSize(long blockSize);

	String[] getPipeNames();

	String getStopWordsDir();

}