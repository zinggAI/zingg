package zingg.spark;

import java.util.List;

import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import scala.collection.Seq;
import zingg.block.BlockFunction;
import zingg.block.Canopy;
import zingg.block.Tree;

public class SparkBlockFunction extends BlockFunction<Row> {

	public SparkBlockFunction(Tree<Canopy<Row>> tree) {
        super(tree);
    }
	
	public Seq<Object> toSeq(Row r) {
		return r.toSeq();

	}

	public Row createRow(List<Object> o){
		return RowFactory.create(o.toArray());	
	}


}