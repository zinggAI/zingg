package zingg.snowpark;

import java.util.List;

import com.snowflake.snowpark_java.Row;
import org.apache.spark.sql.RowFactory;

import scala.collection.Seq;
import zingg.block.BlockFunction;
import zingg.block.Canopy;
import zingg.block.Tree;

public class SnowBlockFunction extends BlockFunction<Row> {

	public SnowBlockFunction(Tree<Canopy<Row>> tree) {
        super(tree);
    }
	
	public Seq<Object> toSeq(Row r) {
		return r.toSeq();

	}

	public Row createRow(List<Object> o){
		return Row.create(o.toArray());	
	}


}