package zingg.spark.core.block;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.IArguments;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.Tree;
import zingg.common.client.util.DFObjectUtil;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.block.blockingTree.SparkDefaultBlockingTreeBuilder;
import zingg.spark.core.block.blockingTree.SparkOptimizedBlockingTreeBuilder;
import zingg.spark.core.block.model.Customer;
import zingg.spark.core.block.model.CustomerDupe;
import zingg.spark.core.context.ZinggSparkContext;
import zingg.spark.core.executor.ZinggSparkTester;
import zingg.spark.core.util.SparkBlockingTreeUtil;
import zingg.spark.client.util.SparkPipeUtil;
import zingg.spark.core.util.SparkHashUtil;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static java.lang.Math.max;

public class TestBlockingTreeUtil extends ZinggSparkTester {

    protected ZinggSparkContext sparkContext;
    private SparkSession sparkSession;
    private static String TEST_DATA_BASE_LOCATION = "/home/administrator/zingg/zinggEnterprise/spark/examples/febrl120k/";
    int maxDepth = 1, totalNodes = 0;

    public TestBlockingTreeUtil() throws ZinggClientException {
        this.sparkContext = new ZinggSparkContext();
        this.sparkSession = spark;
        sparkContext.init(sparkSession);
    }

    @Test
    public void testSameBlockingTree() throws Exception, ZinggClientException {
        IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
        iWithSession.setSession(sparkSession);
        DFObjectUtil<SparkSession,Dataset<Row>,Row,Column> sparkDFObjectUtil = new SparkDFObjectUtil(iWithSession);
        SparkPipeUtil pipeUtil = new SparkPipeUtil(sparkSession);
        SparkBlockingTreeUtil sparkBlockingTreeUtilOptimized = new SparkBlockingTreeUtil(sparkSession, pipeUtil, new SparkOptimizedBlockingTreeBuilder());
        SparkBlockingTreeUtil sparkBlockingTreeUtil = new SparkBlockingTreeUtil(sparkSession, pipeUtil, new SparkDefaultBlockingTreeBuilder());
        SparkHashUtil sparkHashUtil = new SparkHashUtil(sparkSession);


        IArguments args = new ArgumentsUtil(Arguments.class).createArgumentsFromJSON(
                TEST_DATA_BASE_LOCATION + "config.json",
                "");
        args.setBlockSize(8);

        List<Customer> testCustomers = getCustomers();
        List<CustomerDupe> testCustomerDupes = getCustomerDupes();

        ZFrame<Dataset<Row>, Row, Column> zFrameTest = sparkDFObjectUtil.getDFFromObjectList(testCustomers, Customer.class);
        ZFrame<Dataset<Row>, Row, Column> zFramePositives = sparkDFObjectUtil.getDFFromObjectList(testCustomerDupes, CustomerDupe.class);

        long ts = System.currentTimeMillis();
        Tree<Canopy<Row>> blockingTreeOptimized = sparkBlockingTreeUtilOptimized.createBlockingTree(zFrameTest, zFramePositives, 1, -1,
                args, sparkHashUtil.getHashFunctionList());
        System.out.println("************ time taken to create optimized blocking tree ************ " + (System.currentTimeMillis() - ts));

        ts = System.currentTimeMillis();
        Tree<Canopy<Row>> blockingTreeDefault = sparkBlockingTreeUtil.createBlockingTree(zFrameTest, zFramePositives, 1, -1,
                args, sparkHashUtil.getHashFunctionList());
        System.out.println("************ time taken to create blocking tree ************ " + (System.currentTimeMillis() - ts));

        int depth = 1;
        //assert both the trees are equal
        Assertions.assertTrue(dfsSameTreeValidation(blockingTreeDefault, blockingTreeOptimized, depth));

        System.out.println("-------- max depth of trees -------- " + maxDepth);
        System.out.println("-------- total nodes in a trees -------- " + totalNodes);
    }


    private boolean dfsSameTreeValidation(Tree<Canopy<Row>> node1, Tree<Canopy<Row>> node2, int depth) {
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

        Iterator<Tree<Canopy<Row>>> canopyIterator1 = node1.getSubTrees().iterator();
        Iterator<Tree<Canopy<Row>>> canopyIterator2 = node2.getSubTrees().iterator();

        boolean isEqual = true;

        //recurse through sub-trees
        while (canopyIterator1.hasNext() && canopyIterator2.hasNext()) {
            isEqual &= dfsSameTreeValidation(canopyIterator1.next(), canopyIterator2.next(), depth + 1);
        }

        return isEqual;
    }


    private boolean performValidationOnNode1AndNode2(Tree<Canopy<Row>> node1, Tree<Canopy<Row>> node2) {
        boolean functionEqual = isNodeFunctionEqual(node1.getHead(), node2.getHead());
        boolean contextEqual = isNodeContextEqual(node1.getHead(), node2.getHead());
        boolean hashEqual = isNodeHashEqual(node1.getHead(), node2.getHead());
        boolean subtreeSizeEqual = isNodeSubTreesSizeEqual(node1, node2);

        return functionEqual && contextEqual && hashEqual && subtreeSizeEqual;
    }
    private boolean isNodeFunctionEqual(Canopy<Row> node1Head, Canopy<Row> node2Head) {
        if (node1Head.getFunction() == null && node2Head.getFunction() == null) {
            return true;
        } else if (node1Head.getFunction() == null || node2Head.getFunction() == null) {
            return false;
        } else {
            return Objects.equals(node1Head.getFunction().getName(), node2Head.getFunction().getName());
        }
    }

    private boolean isNodeHashEqual(Canopy<Row> node1Head, Canopy<Row> node2Head) {
        return Objects.equals(node1Head.getHash(), node2Head.getHash());
    }

    private boolean isNodeContextEqual(Canopy<Row> node1Head, Canopy<Row> node2Head) {

        if (node1Head.getContext() == null && node2Head.getContext() == null) {
            return true;
        } else if (node1Head.getContext() == null || node2Head.getContext() == null) {
            return false;
        } else {
            return Objects.equals(node1Head.getContext().getName(), node2Head.getContext().getName());
        }
    }

    private boolean isNodeSubTreesSizeEqual(Tree<Canopy<Row>> node1, Tree<Canopy<Row>> node2) {
        return node1.getSubTrees().size() == node2.getSubTrees().size();
    }

    private CSVReader getCSVReader() throws FileNotFoundException {
        FileReader filereader = new FileReader(TEST_DATA_BASE_LOCATION + "test20k.csv");
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withSkipLines(1)
                .build();

        return csvReader;
    }

    private List<CustomerDupe> getCustomerDupes() throws IOException, CsvException {

        List<CustomerDupe> testCustomerDupes = new ArrayList<>();

        CSVReader csvReader = getCSVReader();
        List<String[]> allData = csvReader.readAll();
        for (String[] row : allData) {
            String[] dupe = new String[2 * row.length];
            System.arraycopy(row, 0, dupe, 0, row.length);
            String[] varianceAddedRow = getVarianceAddedRow(row);
            System.arraycopy(varianceAddedRow, 0, dupe, varianceAddedRow.length, varianceAddedRow.length);
            testCustomerDupes.add(new CustomerDupe(dupe));
        }
        return testCustomerDupes;
    }

    private String[] getVarianceAddedRow(String[] row) {
        String[] varianceAddedRow = new String[row.length];
        varianceAddedRow[0] = row[0];
        for(int idx = 1; idx < row.length; idx++) {
            varianceAddedRow[idx] = "v_" + row[idx] + "_v";
        }

        return varianceAddedRow;
    }
    private List<Customer> getCustomers() throws IOException, CsvException {

        List<Customer> testCustomers = new ArrayList<>();

        CSVReader csvReader = getCSVReader();
        List<String[]> allData = csvReader.readAll();
        for (String[] row : allData) {
            testCustomers.add(new Customer(row));
        }
        return testCustomers;
    }

}