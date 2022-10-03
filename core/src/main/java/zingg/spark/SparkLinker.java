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
import zingg.Linker;
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


public class SparkLinker extends Linker<SparkSession, Dataset<Row>, Row, Column,DataType> {

	public static String name = "zingg.SparkLinker";
	public static final Log LOG = LogFactory.getLog(SparkLinker.class);

	public SparkLinker() {
		setZinggOptions(ZinggOptions.LINK);
	}

	
	@Override
	public void cleanup() throws ZinggClientException {
		// TODO Auto-generated method stub
		
	}

	

	@Override
	protected Model getModel() {
		Model model = new SparkModel(this.featurers);
		model.register(getContext());
		model.load(args.getModel());
		return model;
	}


	@Override
	public void setSession(SparkSession session) {
		// TODO Auto-generated method stub
		
	}

	
	
}
