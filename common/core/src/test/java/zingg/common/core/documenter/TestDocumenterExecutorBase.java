package zingg.common.core.documenter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NoSuchObjectException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import zingg.common.client.ClientOptions;
import zingg.common.client.arguments.ArgumentServiceImpl;
import zingg.common.client.arguments.IArgumentService;
import zingg.common.client.arguments.model.Arguments;
import zingg.common.client.arguments.model.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.executor.Documenter;

/**
 * Tests the Documenter executor end to end: running {@link Documenter#execute()}
 * against a prepared model fixture should produce both the model and data HTML
 * documents on disk.
 */
public abstract class TestDocumenterExecutorBase<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(TestDocumenterExecutorBase.class);

    protected Context<S, D, R, C, T> context;
    protected IArguments docArguments = new Arguments();

    public TestDocumenterExecutorBase() {
    }

    public void initialize(Context<S, D, R, C, T> context) throws ZinggClientException {
        this.context = context;
    }

    /** each engine (Spark, etc.) supplies its own initialised executor */
    protected abstract Documenter<S, D, R, C, T> getDocumenter() throws ZinggClientException;

    @BeforeEach
    public void setUp() throws NoSuchObjectException, ZinggClientException {
        String configPath = getClass().getResource("../../../../documenter/config.json").getFile();
        IArgumentService<Arguments> argsUtil = new ArgumentServiceImpl<>(Arguments.class);
        docArguments = argsUtil.loadArguments(configPath);
        // point zinggDir at the prepared model fixture under test resources
        String zinggDirPath = getClass().getResource("../../../../" + docArguments.getZinggDir()).getFile();
        docArguments.setZinggDir(zinggDirPath);
        // point the input data at the test csv so the data document is generated
        Pipe[] dataPipeArr = docArguments.getData();
        String dataFile = getClass().getResource("../../../../documenter/test.csv").getFile();
        for (int i = 0; i < dataPipeArr.length; i++) {
            dataPipeArr[i].setProp(FilePipe.PATH, dataFile);
        }
    }

    @Test
    public void testExecuteGeneratesModelAndDataDocuments() throws Throwable {
        Documenter<S, D, R, C, T> documenter = getDocumenter();

        String modelDocFile = documenter.getModelHelper().getZinggModelDocFile(docArguments);
        String dataDocFile = documenter.getModelHelper().getZinggDataDocFile(docArguments);

        // start clean so a stale file from a previous run cannot make the test lie
        Files.deleteIfExists(Paths.get(modelDocFile));
        Files.deleteIfExists(Paths.get(dataDocFile));

        try {
            documenter.execute();

            assertTrue(Files.exists(Paths.get(modelDocFile)), "model document was not generated");
            assertTrue(Files.exists(Paths.get(dataDocFile)), "data document was not generated");
        } finally {
            // do not leave generated documents behind in the source tree
            Files.deleteIfExists(Paths.get(modelDocFile));
            Files.deleteIfExists(Paths.get(dataDocFile));
        }
    }
}
