package zingg.spark;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.RelationalGroupedDataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.types.DataType;

import scala.collection.JavaConverters;
import zingg.TrainingDataFinder;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.spark.model.SparkModel;
import zingg.client.SparkFrame;
import zingg.client.ZFrame;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.ColValues;
import zingg.client.util.Util;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtilBase;


public class SparkTrainingDataFinder extends TrainingDataFinder<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkTrainingDataFinder";
	public static final Log LOG = LogFactory.getLog(SparkTrainingDataFinder.class);

	public SparkTrainingDataFinder() {
		setZinggOptions(ZinggOptions.FIND_TRAINING_DATA);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setSpark(SparkSession session) {
		// TODO Auto-generated method stub
		
	}
	
}
