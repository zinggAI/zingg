package zingg.common.core.block;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BlockFunction<R> implements Serializable {

    public static final Log LOG = LogFactory.getLog(BlockFunction.class);
		
    Tree<Canopy<R>> tree;
    public BlockFunction(Tree<Canopy<R>> tree) {
        this.tree = tree;
    }
    
    
    public R call(R r) {
        StringBuilder bf = new StringBuilder();
        bf = Block.applyTree(r, tree, tree.getHead(), bf);
        return createRow(r, bf); //RowFactory.create(returnList);			
    }

    public abstract List<Object> getListFromRow(R r) ;

    public abstract R getRowFromList(List<Object> lob);

    public R createRow(R r, StringBuilder bf) {
        List<Object> currentRowValues = getListFromRow(r);
        currentRowValues.add(hashBlockingPath(bf.toString()));
        if (LOG.isDebugEnabled()) {
            for (Object o: currentRowValues) {
                LOG.debug("return row col is " + o );
            }
        }
        return getRowFromList(currentRowValues);
    }

    /**
     * Returns a stable 64-bit key for the complete blocking path. A digest is
     * used instead of String.hashCode(), whose signed 32-bit range creates
     * avoidable collisions for unrelated paths.
     */
    public static long hashBlockingPath(String blockingPath) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(blockingPath.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(digest).getLong();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required for blocking keys", e);
        }
    }


}
