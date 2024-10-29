package zingg.common.core.block;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.block.data.CsvReader;
import zingg.common.core.block.data.DataUtility;
import zingg.common.core.block.model.Customer;
import zingg.common.core.block.model.CustomerDupe;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.HashUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;

public abstract class TestBlockingTreeUtil<S, D, R, C, T> {

    protected String TEST_DATA_BASE_LOCATION;
    private int maxDepth = 1;
    private int totalNodes = 0;
    private static String TEST_FILE = "test.csv";
    private static String CONFIG_FILE = "config.json";
    private final DataUtility dataUtility;

    public TestBlockingTreeUtil() {
        setTestDataBaseLocation();
        this.dataUtility = new DataUtility(new CsvReader());
    }

    @Test
    public void testSameBlockingTreeWithoutVariance() throws Exception, ZinggClientException {
        List<Customer> testCustomers = dataUtility.getCustomers(TEST_DATA_BASE_LOCATION + "/" + TEST_FILE);
        //setting variance as false
        List<CustomerDupe> testCustomerDupes = dataUtility.getCustomerDupes(TEST_DATA_BASE_LOCATION + "/" + TEST_FILE, false);
        DFObjectUtil<S, D, R, C> dfObjectUtil = getDFObjectUtil();

        ZFrame<D, R, C> zFrameTest = dfObjectUtil.getDFFromObjectList(testCustomers, Customer.class);
        ZFrame<D, R, C> zFramePositives = dfObjectUtil.getDFFromObjectList(testCustomerDupes, CustomerDupe.class);

        testSameBlockingTree(zFrameTest, zFramePositives);
    }

    @Test
    public void testSameBlockingTreeWithVariance() throws Exception, ZinggClientException {
        List<Customer> testCustomers = dataUtility.getCustomers(TEST_DATA_BASE_LOCATION + "/" + TEST_FILE);
        //setting variance as true
        List<CustomerDupe> testCustomerDupes = dataUtility.getCustomerDupes(TEST_DATA_BASE_LOCATION + "/" + TEST_FILE, true);
        DFObjectUtil<S, D, R, C> dfObjectUtil = getDFObjectUtil();

        ZFrame<D, R, C> zFrameTest = dfObjectUtil.getDFFromObjectList(testCustomers, Customer.class);
        ZFrame<D, R, C> zFramePositives = dfObjectUtil.getDFFromObjectList(testCustomerDupes, CustomerDupe.class);

        testSameBlockingTree(zFrameTest, zFramePositives);
    }

    public void testSameBlockingTree(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives) throws Exception, ZinggClientException {
        setTestDataBaseLocation();
        BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil = getBlockingTreeUtil();
        HashUtil<S, D, R, C, T> hashUtil = getHashUtil();


        IArguments args = new ArgumentsUtil(Arguments.class).createArgumentsFromJSON(
                TEST_DATA_BASE_LOCATION + "/" + CONFIG_FILE,
                "");
        args.setBlockSize(8);

        long ts = System.currentTimeMillis();
        Tree<Canopy<R>> blockingTreeOptimized = blockingTreeUtil.createBlockingTree(zFrameTest, zFramePositives, 1, -1,
                args, hashUtil.getHashFunctionList(), HashUtility.CACHED);
        System.out.println("************ time taken to create optimized blocking tree ************ " + (System.currentTimeMillis() - ts));

        ts = System.currentTimeMillis();
        Tree<Canopy<R>> blockingTreeDefault = blockingTreeUtil.createBlockingTree(zFrameTest, zFramePositives, 1, -1,
                args, hashUtil.getHashFunctionList(), HashUtility.DEFAULT);
        System.out.println("************ time taken to create blocking tree ************ " + (System.currentTimeMillis() - ts));

        int depth = 1;
        //assert both the trees are equal
        Assertions.assertTrue(dfsSameTreeValidation(blockingTreeDefault, blockingTreeOptimized, depth));

        System.out.println("-------- max depth of trees -------- " + maxDepth);
        System.out.println("-------- total nodes in a trees -------- " + totalNodes);
    }


    private boolean dfsSameTreeValidation(Tree<Canopy<R>> node1, Tree<Canopy<R>> node2, int depth) {
        totalNodes++;
        maxDepth = max(maxDepth, depth);

        //if both the node1 and node2 are null, return true
        if(node1 == null && node2 == null){
            return true;
        }
        //if only one of node1 or node2 is null, return false
        if(node1 == null || node2 == null){
            return false;
        }

        if (!performValidationOnNode1AndNode2(node1, node2)) {
            return false;
        }

        Iterator<Tree<Canopy<R>>> canopyIterator1 = node1.getSubTrees().iterator();
        Iterator<Tree<Canopy<R>>> canopyIterator2 = node2.getSubTrees().iterator();

        boolean isEqual = true;

        //recurse through sub-trees
        while (canopyIterator1.hasNext() && canopyIterator2.hasNext()) {
            isEqual &= dfsSameTreeValidation(canopyIterator1.next(), canopyIterator2.next(), depth + 1);
        }

        return isEqual;
    }


    private boolean performValidationOnNode1AndNode2(Tree<Canopy<R>> node1, Tree<Canopy<R>> node2) {
        boolean functionEqual = isNodeFunctionEqual(node1.getHead(), node2.getHead());
        boolean contextEqual = isNodeContextEqual(node1.getHead(), node2.getHead());
        boolean hashEqual = isNodeHashEqual(node1.getHead(), node2.getHead());
        boolean subtreeSizeEqual = isNodeSubTreesSizeEqual(node1, node2);

        return functionEqual && contextEqual && hashEqual && subtreeSizeEqual;
    }
    private boolean isNodeFunctionEqual(Canopy<R> node1Head, Canopy<R> node2Head) {
        if (node1Head.getFunction() == null && node2Head.getFunction() == null) {
            return true;
        } else if (node1Head.getFunction() == null || node2Head.getFunction() == null) {
            return false;
        } else {
            return Objects.equals(node1Head.getFunction().getName(), node2Head.getFunction().getName());
        }
    }

    private boolean isNodeHashEqual(Canopy<R> node1Head, Canopy<R> node2Head) {
        return Objects.equals(node1Head.getHash(), node2Head.getHash());
    }

    private boolean isNodeContextEqual(Canopy<R> node1Head, Canopy<R> node2Head) {

        if (node1Head.getContext() == null && node2Head.getContext() == null) {
            return true;
        } else if (node1Head.getContext() == null || node2Head.getContext() == null) {
            return false;
        } else {
            return Objects.equals(node1Head.getContext().getName(), node2Head.getContext().getName());
        }
    }

    private boolean isNodeSubTreesSizeEqual(Tree<Canopy<R>> node1, Tree<Canopy<R>> node2) {
        return node1.getSubTrees().size() == node2.getSubTrees().size();
    }


    protected abstract DFObjectUtil<S, D, R, C> getDFObjectUtil();
    protected abstract BlockingTreeUtil<S, D, R, C, T> getBlockingTreeUtil();
    protected abstract HashUtil<S, D, R, C, T> getHashUtil();
    protected abstract void setTestDataBaseLocation();
}