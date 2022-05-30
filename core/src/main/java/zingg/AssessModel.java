package zingg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;

import scala.collection.JavaConverters;
import zingg.block.Block;
import zingg.block.Canopy;
import zingg.block.Tree;
import zingg.model.Model;
import zingg.client.Arguments;
import zingg.client.ZinggClientException;
import zingg.client.ZinggOptions;
import zingg.client.util.ColName;
import zingg.client.util.Util;
import zingg.util.BlockingTreeUtil;
import zingg.util.DSUtil;
import zingg.util.GraphUtil;
import zingg.util.ModelUtil;
import zingg.util.PipeUtil;

public class AssessModel extends Labeller{

	protected static String name = "zingg.AssessModel";
	public static final Log LOG = LogFactory.getLog(AssessModel.class); 
	
	//private Trainer trainer;

    public AssessModel() {
        setZinggOptions(ZinggOptions.ASSESS_MODEL);
		//trainer = new Trainer();
    }

	public static void main(String[] args) {
		System.out.println("Assessmodel");
	  }

}
