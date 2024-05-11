package zingg.common.client.util;

import zingg.common.client.pipe.*;

import zingg.common.client.IZArgs;

public interface IModelHelper<D,R,C> {

    /**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */

	String getZinggBaseModelDir(IZArgs args);

	String getZinggModelDir(IZArgs args);

	String getZinggDocDir(IZArgs args);

	String getZinggModelDocFile(IZArgs args);

	String getZinggDataDocFile(IZArgs args);

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggBaseTrainingDataDir(IZArgs args);

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggTrainingDataUnmarkedDir(IZArgs args);

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggTrainingDataMarkedDir(IZArgs args);

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	String getZinggPreprocessedDataDir(IZArgs args);

	/**
	 * This is an internal block file location Not to be used directly by the
	 * client
	 * 
	 * @return the blockFile
	 */
	String getBlockFile(IZArgs args);

	/**
	 * This is the internal model location Not to be used by the client
	 * 
	 * @return model path
	 */
	String getModel(IZArgs args);

	public Pipe<D, R, C> getTrainingDataUnmarkedPipe(IZArgs args);

	public Pipe<D, R, C> getTrainingDataMarkedPipe(IZArgs args);
	
	public Pipe<D, R, C> getModelDocumentationPipe(IZArgs args);
	
	public Pipe<D, R, C> getBlockingTreePipe(IZArgs args);


	public Pipe<D,R,C> getStopWordsPipe(String fileName) ;

	
    
}
