package zingg.block;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import scala.collection.JavaConversions;
import scala.collection.Seq;

public abstract class BlockFunction<R> implements Serializable {

    public static final Log LOG = LogFactory.getLog(BlockFunction.class);
		
    Tree<Canopy<R>> tree;
    public BlockFunction(Tree<Canopy<R>> tree) {
        this.tree = tree;
    }
    
    
    public R call(R r) {
        StringBuilder bf = new StringBuilder();
        bf = Block.applyTree(r, tree, tree.getHead(), bf);
        Seq<Object> s = toSeq(r);
        List<Object> seqList = JavaConversions.seqAsJavaList(s);
        List<Object> returnList = new ArrayList<Object>(seqList.size()+1);
        returnList.addAll(seqList);
        returnList.add(bf.toString().hashCode());
        if (LOG.isDebugEnabled()) {
            for (Object o: returnList) {
                LOG.debug("return row col is " + o );
            }
        }
        
        return createRow(returnList); //RowFactory.create(returnList);			
    }

    public abstract Seq<Object> toSeq(R r) ;

    public abstract R createRow(List<Object> o);


}
