package zingg.spark.core.block;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import zingg.common.client.FieldDefinition;
import zingg.common.client.MatchTypes;
import zingg.common.client.ZinggClientException;
import zingg.common.client.ZFrame;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.ListMap;
import zingg.common.client.util.WithSession;
import zingg.common.core.block.Block;
import zingg.common.core.block.Canopy;
import zingg.common.core.block.DefaultFieldDefinitionStrategy;
import zingg.common.core.block.GetBestNode;
import zingg.common.core.block.HashUtility;
import zingg.common.core.block.Tree;
import zingg.common.core.block.data.DataUtility;
import zingg.common.core.block.model.Customer;
import zingg.common.core.block.model.CustomerDupe;
import zingg.common.core.hash.HashFunction;
import zingg.common.core.util.CsvReader;
import zingg.common.core.util.Heuristics;
import zingg.spark.client.SparkClient;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.util.SparkHashUtil;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark comparing the original getBestNode algorithm against the
 * optimized one (precomputed field values + early-exit + deferred buildDupeRemaining).
 * Both benchmarks use HashUtility.CACHED so the only variable is the algorithm.
 *
 * Run via Maven:
 *   mvn test-compile exec:exec -Pbenchmark -pl spark/core
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(1)
@Warmup(iterations = 2, batchSize = 1)
@Measurement(iterations = 5, batchSize = 1)
public class BlockingTreeBenchmark {

    private static final String DATA_DIR = "testFebrl";
    private static final String LARGE_CSV = DATA_DIR + "/test_large.csv";
    private static final String LARGE_CONFIG = DATA_DIR + "/config_large.json";

    private ZFrame<Dataset<Row>, Row, Column> sample;
    private ZFrame<Dataset<Row>, Row, Column> positives;
    private ListMap<DataType, HashFunction<Dataset<Row>, Row, Column, DataType>> hashFunctions;
    private long blockSize;
    private List<FieldDefinition> fieldDefinitions;

    // Pre-collected lists so each invocation creates a fresh Canopy without
    // triggering a Spark action (collectAsList is done once in setup).
    private List<Row> trainingList;
    private List<Row> positivesList;

    private SparkSession spark;

    @Setup(Level.Trial)
    public void setup() throws Exception, ZinggClientException {
        spark = SparkSession.builder()
                .master("local[*]")
                .appName("ZinggBlockingTreeBenchmark")
                .config("spark.driver.bindAddress", "127.0.0.1")
                .config("spark.driver.host", "127.0.0.1")
                .getOrCreate();

        new SparkClient().checkAndSetCheckpoint(spark);

        IWithSession<SparkSession> withSession = new WithSession<>();
        withSession.setSession(spark);
        SparkDFObjectUtil dfObjectUtil = new SparkDFObjectUtil(withSession);

        DataUtility dataUtility = new DataUtility(new CsvReader());
        List<Customer> customers = dataUtility.getCustomers(LARGE_CSV);
        List<CustomerDupe> customerDupes = dataUtility.getCustomerDupes(LARGE_CSV, false);

        ZFrame<Dataset<Row>, Row, Column> zFrameTest =
                dfObjectUtil.getDFFromObjectList(customers, Customer.class);
        ZFrame<Dataset<Row>, Row, Column> zFramePositives =
                dfObjectUtil.getDFFromObjectList(customerDupes, CustomerDupe.class);

        InputStream configStream = Objects.requireNonNull(
                getClass().getClassLoader().getResourceAsStream(LARGE_CONFIG),
                "Config resource not found: " + LARGE_CONFIG
        );
        Path tempConfig = Files.createTempFile("zingg-config-", ".json");
        tempConfig.toFile().deleteOnExit();
        Files.copy(configStream, tempConfig, StandardCopyOption.REPLACE_EXISTING);
        IArguments args = new ArgumentServiceImpl<>(Arguments.class).loadArguments(tempConfig.toString());

        sample = zFrameTest.sample(false, 1.0);
        long totalCount = sample.count();
        blockSize = Heuristics.getMaxBlockSize(totalCount, args.getBlockSize());
        positives = zFramePositives.coalesce(1);

        hashFunctions = new SparkHashUtil(spark).getHashFunctionList();
        fieldDefinitions = getActiveFieldDefinitions(args);

        trainingList = sample.collectAsList();
        positivesList = positives.collectAsList();
    }

    @TearDown(Level.Trial)
    public void teardown() {
        if (spark != null) {
            spark.stop();
            spark = null;
        }
    }

    /** Precomputed field values + early exit + deferred buildDupeRemaining. */
    @Benchmark
    public Tree<Canopy<Row>> preComputedFieldValuesEarlyExitAlgoTree() throws Exception, ZinggClientException {
        Canopy<Row> root = new Canopy<>(new ArrayList<>(trainingList), new ArrayList<>(positivesList));
        SparkBlock block = new SparkBlock(sample, positives, hashFunctions, blockSize,
                new DefaultFieldDefinitionStrategy<>(), HashUtility.CACHED);
        return block.getBlockingTree(null, null, root, fieldDefinitions);
    }

    /** Precomputed values passed to buildDupeRemaining, avoiding re-extraction on the winner. */
    @Benchmark
    public Tree<Canopy<Row>> precomputedWithBuildDupeAlgoTree() throws Exception, ZinggClientException {
        Canopy<Row> root = new Canopy<>(new ArrayList<>(trainingList), new ArrayList<>(positivesList));
        SparkBlock block = new SparkBlock(sample, positives, hashFunctions, blockSize,
                new DefaultFieldDefinitionStrategy<>(), HashUtility.CACHED);
        block.setGetBestNodeStrategy(new GetBestNode<Dataset<Row>, Row, Column, DataType>() {
            @Override
            public Canopy<Row> getBestNode(Block<Dataset<Row>, Row, Column, DataType> b,
                    Tree<Canopy<Row>> tree, Canopy<Row> parent, Canopy<Row> node,
                    List<FieldDefinition> fields) throws Exception {
                return precomputedBuildDupeRemaining(b, tree, parent, node, fields);
            }
        });
        return block.getBlockingTree(null, null, root, fieldDefinitions);
    }

    /** Probe-reuse: one shared probe Canopy per getBestNode call; real Canopy only on new best. */
    @Benchmark
    public Tree<Canopy<Row>> probeReuseAlgoTree() throws Exception, ZinggClientException {
        Canopy<Row> root = new Canopy<>(new ArrayList<>(trainingList), new ArrayList<>(positivesList));
        SparkBlock block = new SparkBlock(sample, positives, hashFunctions, blockSize,
                new DefaultFieldDefinitionStrategy<>(), HashUtility.CACHED);
        block.setGetBestNodeStrategy(new GetBestNode<Dataset<Row>, Row, Column, DataType>() {
            @Override
            public Canopy<Row> getBestNode(Block<Dataset<Row>, Row, Column, DataType> b,
                    Tree<Canopy<Row>> tree, Canopy<Row> parent, Canopy<Row> node,
                    List<FieldDefinition> fields) throws Exception {
                return probeReuse(b, tree, parent, node, fields);
            }
        });
        return block.getBlockingTree(null, null, root, fieldDefinitions);
    }

    /** Original getBestNode: calls estimateElimCount() per candidate, builds dupeRemaining for all. */
    @Benchmark
    public Tree<Canopy<Row>> originalAlgoTree() throws Exception, ZinggClientException {
        Canopy<Row> root = new Canopy<>(new ArrayList<>(trainingList), new ArrayList<>(positivesList));
        SparkBlock block = new SparkBlock(sample, positives, hashFunctions, blockSize,
                new DefaultFieldDefinitionStrategy<>(), HashUtility.CACHED);
        block.setGetBestNodeStrategy(new GetBestNode<Dataset<Row>, Row, Column, DataType>() {
            @Override
            public Canopy<Row> getBestNode(Block<Dataset<Row>, Row, Column, DataType> b,
                    Tree<Canopy<Row>> tree, Canopy<Row> parent, Canopy<Row> node,
                    List<FieldDefinition> fields) throws Exception {
                return original(b, tree, parent, node, fields);
            }
        });
        return block.getBlockingTree(null, null, root, fieldDefinitions);
    }

    private List<FieldDefinition> getActiveFieldDefinitions(IArguments args) {
        List<FieldDefinition> active = new ArrayList<>();
        for (FieldDefinition def : args.getFieldDefinition()) {
            if (!(def.getMatchType() == null || def.getMatchType().contains(MatchTypes.DONT_USE))) {
                active.add(def);
            }
        }
        return active;
    }

}
