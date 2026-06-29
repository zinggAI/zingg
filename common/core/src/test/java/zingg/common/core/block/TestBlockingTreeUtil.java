package zingg.common.core.block;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.FieldDefinition;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.client.util.ListMap;
import zingg.common.core.block.data.DataUtility;
import zingg.common.core.block.model.Customer;
import zingg.common.core.block.model.CustomerDupe;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.CsvReader;
import zingg.common.core.util.HashUtil;
import zingg.common.core.util.Heuristics;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public abstract class TestBlockingTreeUtil<S, D, R, C, T> {

    protected String TEST_DATA_BASE_LOCATION;
    private int maxDepth = 1;
    private int totalNodes = 0;
    private static String TEST_FILE = "test.csv";
    private static String LARGE_TEST_FILE = "test_large.csv";
    private static String CONFIG_FILE = "config.json";
    private static String LARGE_CONFIG_FILE = "config_large.json";
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


    @Test
    public void testOriginalAndOptimizedProduceSameTree() throws Exception, ZinggClientException {
        setTestDataBaseLocation();
        List<Customer> testCustomers = dataUtility.getCustomers(TEST_DATA_BASE_LOCATION + "/" + LARGE_TEST_FILE);
        List<CustomerDupe> testCustomerDupes = dataUtility.getCustomerDupes(TEST_DATA_BASE_LOCATION + "/" + LARGE_TEST_FILE, false);
        DFObjectUtil<S, D, R, C> dfObjectUtil = getDFObjectUtil();

        ZFrame<D, R, C> zFrameTest = dfObjectUtil.getDFFromObjectList(testCustomers, Customer.class);
        ZFrame<D, R, C> zFramePositives = dfObjectUtil.getDFFromObjectList(testCustomerDupes, CustomerDupe.class);

        HashUtil<S, D, R, C, T> hashUtil = getHashUtil();
        String configFile = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(TEST_DATA_BASE_LOCATION + "/" + LARGE_CONFIG_FILE)).toURI()).toString();
        IArguments args = new ArgumentServiceImpl<Arguments>(Arguments.class).loadArguments(configFile);

        Tree<Canopy<R>> originalTree  = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "original");
        Tree<Canopy<R>> optimizedTree = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "cached");
        Assertions.assertTrue(dfsSameTreeValidation(originalTree, optimizedTree, 1),
                "Original and optimized getBestNode must produce identical blocking trees");
    }

    @Test
    public void testLargeDeepBlockingTree() throws Exception, ZinggClientException {
        setTestDataBaseLocation();
        List<Customer> testCustomers = dataUtility.getCustomers(TEST_DATA_BASE_LOCATION + "/" + LARGE_TEST_FILE);
        List<CustomerDupe> testCustomerDupes = dataUtility.getCustomerDupes(TEST_DATA_BASE_LOCATION + "/" + LARGE_TEST_FILE, false);
        DFObjectUtil<S, D, R, C> dfObjectUtil = getDFObjectUtil();

        ZFrame<D, R, C> zFrameTest = dfObjectUtil.getDFFromObjectList(testCustomers, Customer.class);
        ZFrame<D, R, C> zFramePositives = dfObjectUtil.getDFFromObjectList(testCustomerDupes, CustomerDupe.class);

        HashUtil<S, D, R, C, T> hashUtil = getHashUtil();
        String configFile = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(TEST_DATA_BASE_LOCATION + "/" + LARGE_CONFIG_FILE)).toURI()).toString();
        IArguments args = new ArgumentServiceImpl<Arguments>(Arguments.class).loadArguments(configFile);

        System.out.println("=== STARTING LARGE DEEP BLOCKING TREE TEST ===");
        Tree<Canopy<R>> blockingTreeDefault = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "default");
        Tree<Canopy<R>> blockingTreeCached = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "cached");
        Assertions.assertTrue(dfsSameTreeValidation(blockingTreeDefault, blockingTreeCached, 1));
        System.out.println("-------- max depth of trees -------- " + maxDepth);
        System.out.println("-------- total nodes in a trees ---- " + totalNodes);

        maxDepth = 1; totalNodes = 0;
        Tree<Canopy<R>> blockingTreeDefault2 = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "default");
        Tree<Canopy<R>> blockingTreeCached2 = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "cached");
        Assertions.assertTrue(dfsSameTreeValidation(blockingTreeDefault2, blockingTreeCached2, 1));
    }

    public void testSameBlockingTree(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives) throws Exception, ZinggClientException {
        setTestDataBaseLocation();
        HashUtil<S, D, R, C, T> hashUtil = getHashUtil();
        String configFile = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(TEST_DATA_BASE_LOCATION + "/" + CONFIG_FILE)).toURI()).toString();
        IArguments args = new ArgumentServiceImpl<Arguments>(Arguments.class).loadArguments(
                configFile);
        args.setBlockSize(8);

        Tree<Canopy<R>> blockingTreeOptimized = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "cached");
        Tree<Canopy<R>> blockingTreeDefault = getBlockingTree(zFrameTest, zFramePositives, hashUtil, args, "default");

        int depth = 1;
        //assert both the trees are equal
        Assertions.assertTrue(dfsSameTreeValidation(blockingTreeDefault, blockingTreeOptimized, depth));

        System.out.println("-------- max depth of trees -------- " + maxDepth);
        System.out.println("-------- total nodes in a trees ---- " + totalNodes);
    }


    private Tree<Canopy<R>> getBlockingTree(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives, HashUtil<S, D, R, C, T> hashUtil,
                                            IArguments args, String blockingTreeType) throws Exception, ZinggClientException {
        Block<D, R, C, T> block;
        if ("cached".equals(blockingTreeType)) {
            block = getCachedBasedBlock(zFrameTest, zFramePositives, hashUtil, args);
        } else if ("original".equals(blockingTreeType)) {
            block = getOriginalAlgoBlock(zFrameTest, zFramePositives, hashUtil, args);
        } else {
            block = getDefaultBlock(zFrameTest, zFramePositives, hashUtil, args);
        }
        Canopy<R> root = getCanopy(zFrameTest, zFramePositives, 1);
        return block.getBlockingTree(null, null, root, getFieldDefinitions(args));
    }

    private Block<D, R, C, T> getCachedBasedBlock(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives,
                                                  HashUtil<S, D, R, C, T> hashUtil, IArguments arguments) throws Exception {
        return getBlock(zFrameTest, 1, zFramePositives, -1, hashUtil.getHashFunctionList(), arguments, HashUtility.CACHED);
    }

    private Block<D, R, C, T> getOriginalAlgoBlock(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives,
                                                   HashUtil<S, D, R, C, T> hashUtil, IArguments arguments) throws Exception {
        ZFrame<D, R, C> sample = zFrameTest.sample(false, 1);
        long totalCount = sample.count();
        long blockSize = Heuristics.getMaxBlockSize(totalCount, arguments.getBlockSize());
        ZFrame<D, R, C> positives = zFramePositives.coalesce(1);
        return getOriginalAlgoBlock(sample, positives, hashUtil.getHashFunctionList(), blockSize);
    }

    private Block<D, R, C, T> getDefaultBlock(ZFrame<D, R, C> zFrameTest, ZFrame<D, R, C> zFramePositives,
                                              HashUtil<S, D, R, C, T> hashUtil, IArguments arguments) throws Exception {
        return getBlock(zFrameTest, 1, zFramePositives, -1, hashUtil.getHashFunctionList(), arguments, HashUtility.DEFAULT);
    }


    private boolean dfsSameTreeValidation(Tree<Canopy<R>> node1, Tree<Canopy<R>> node2, int depth) {
        totalNodes++;
        maxDepth = Math.max(maxDepth, depth);

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

    private Block<D, R, C, T> getBlock(ZFrame<D, R, C> testData, double sampleFraction, ZFrame<D,R,C> positives,
                                       long blockSize, ListMap<T, HashFunction<D,R,C,T>> hashFunctions, IArguments args,
                                       HashUtility hashUtility) {
        ZFrame<D,R,C> sample = testData.sample(false, sampleFraction);
        long totalCount = sample.count();
        if (blockSize == -1) blockSize = Heuristics.getMaxBlockSize(totalCount, args.getBlockSize());
        positives = positives.coalesce(1);
        return getBlock(sample, positives, hashFunctions, blockSize, hashUtility);
    }

    private Canopy<R> getCanopy(ZFrame<D,R,C> testData, ZFrame<D,R,C> positives, double sampleFraction) {
        ZFrame<D,R,C> sample = testData.sample(false, sampleFraction);
        return new Canopy<R>(sample.collectAsList(), positives.collectAsList());
    }

    private List<FieldDefinition> getFieldDefinitions(IArguments arguments) {
        List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();

        for (FieldDefinition def : arguments.getFieldDefinition()) {
            if (! (def.getMatchType() == null || def.getMatchType().contains(MatchTypes.DONT_USE))) {
                fieldDefinitions.add(def);
            }
        }
        return fieldDefinitions;
    }

    protected abstract DFObjectUtil<S, D, R, C> getDFObjectUtil();
    protected abstract BlockingTreeUtil<S, D, R, C, T> getBlockingTreeUtil();
    protected abstract HashUtil<S, D, R, C, T> getHashUtil();
    protected abstract void setTestDataBaseLocation();
    protected abstract Block<D, R, C, T> getBlock(ZFrame<D,R,C> sample, ZFrame<D,R,C> positives,
                                                  ListMap<T, HashFunction<D,R,C,T>>hashFunctions, long blockSize);
    protected abstract Block<D, R, C, T> getBlock(ZFrame<D,R,C> sample, ZFrame<D,R,C> positives,
                                                  ListMap<T, HashFunction<D,R,C,T>>hashFunctions, long blockSize,
                                                  HashUtility hashUtility);
    protected abstract Block<D, R, C, T> getOriginalAlgoBlock(ZFrame<D,R,C> sample, ZFrame<D,R,C> positives,
                                                              ListMap<T, HashFunction<D,R,C,T>>hashFunctions, long blockSize);
}