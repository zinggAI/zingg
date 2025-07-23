package zingg.spark.client.util;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.IModelHelper;
import zingg.spark.client.pipe.SparkPipe;

public class SparkModelHelper implements IModelHelper<Dataset<Row>, Row, Column>{

    
	public String getZinggBaseModelDir(IZArgs args){
		return args.getZinggDir() + "/" + args.getModelId();
	}

	
	public String getZinggModelDir(IZArgs args) {
		return getZinggBaseModelDir(args) + "/model";
	}

	@Override
	
	public String getZinggDocDir(IZArgs args) {
		return getZinggBaseModelDir(args) + "/docs/";
	}

	@Override
	public String getZinggModelDocFile(IZArgs args) {
		return getZinggDocDir(args) + "/model.html";
	}

	@Override
	public String getZinggDataDocFile(IZArgs args) {
		return getZinggDocDir(args) + "/data.html";
	}

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	@Override
	public String getZinggBaseTrainingDataDir(IZArgs args) {
		return getZinggBaseModelDir(args) + "/trainingData/";
	}



	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	@Override
	public String getZinggTrainingDataUnmarkedDir(IZArgs args) {
		return this.getZinggBaseTrainingDataDir(args) + "/unmarked/";
	}

	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	@Override
	public String getZinggTrainingDataMarkedDir(IZArgs args) {
		return this.getZinggBaseTrainingDataDir(args) + "/marked/";
	}
	
	/**
	 * Location for internal Zingg use.
	 * 
	 * @return the path for internal Zingg usage
	 */
	@Override
	public String getZinggPreprocessedDataDir(IZArgs args) {
		return args.getZinggDir() + "/preprocess";
	}
	
	/**
	 * This is an internal block file location Not to be used directly by the
	 * client
	 * 
	 * @return the blockFile
	 */
	
	public String getBlockFile(IZArgs args) {
		return getZinggModelDir(args) + "/block/zingg.block";
	}
	
	/**
	 * This is the internal model location Not to be used by the client
	 * 
	 * @return model path
	 */
	@Override
	public String getModel(IZArgs args) {
		return getZinggModelDir(args) + "/classifier/best.model";
	}

	@Override
	public Pipe<Dataset<Row>, Row, Column> getTrainingDataUnmarkedPipe(IZArgs args) {
		Pipe<Dataset<Row>, Row, Column>p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getZinggTrainingDataUnmarkedDir(args));
		return p;
	}

	@Override
	public Pipe<Dataset<Row>, Row, Column> getTrainingDataMarkedPipe(IZArgs args) {
		Pipe<Dataset<Row>, Row, Column>p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getZinggTrainingDataMarkedDir(args));
		return p;
	}
	
	@Override
	public Pipe<Dataset<Row>, Row, Column> getModelDocumentationPipe(IZArgs args) {
		Pipe<Dataset<Row>, Row, Column>p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_TEXT);
		p.setProp(FilePipe.LOCATION, getZinggModelDocFile(args));
		return p;
	}

	

	@Override
	public Pipe<Dataset<Row>, Row, Column> getBlockingTreePipe(IZArgs args) {
		SparkPipe p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getBlockFile(args));
		//p.setMode(SaveMode.Overwrite.toString());
		
		p.setOverwriteMode();
		return p;
	}


	public Pipe<Dataset<Row>, Row, Column> getStopWordsPipe(String fileName) {
		SparkPipe p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_CSV);
		p.setProp(FilePipe.HEADER, "true");
		p.setProp(FilePipe.LOCATION, fileName);
		//p.setMode(SaveMode.Overwrite.toString());
		p.setOverwriteMode();
		return p;
	}

}
