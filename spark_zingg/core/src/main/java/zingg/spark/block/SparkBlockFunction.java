package zingg.spark.block;

import java.util.List;

import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;

import scala.collection.Seq;
import zingg.block.BlockFunction;
import zingg.block.Canopy;
import zingg.block.Tree;

public class SparkBlockFunction extends BlockFunction<Row> implements MapFunction<Row, Row>{
   

    public SparkBlockFunction(Tree<Canopy<Row>> tree) {
        super(tree);
    }

    @Override
    public Seq<Object> toSeq(Row r) {
        return r.toSeq();
    }

    @Override
    public Row createRow(List<Object> o) {
        return RowFactory.create(o.toArray());			
    }
    
    @Override
    public Row call(Row r){
        return super.call(r);
    }
}
