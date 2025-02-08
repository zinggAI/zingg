package zingg.common.core.documenter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import zingg.common.client.Arguments;
import zingg.common.client.ArgumentsUtil;
import zingg.common.client.ClientOptions;
import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;

public abstract class TestDocumenterBase<S,D,R,C,T> {

    public static final Log LOG = LogFactory.getLog(TestDocumenterBase.class);
	protected final Context<S, D, R, C, T> context;
    private final String TEST_DOC_TEMPLATE = "documenter/testDocumenterTemplate.ftlh";
	public ArgumentsUtil<Arguments> argsUtil = new ArgumentsUtil<Arguments>(Arguments.class);
	private IArguments docArguments = new Arguments();
    private final IArguments args = new Arguments();

    public TestDocumenterBase(Context<S, D, R, C, T> context) throws ZinggClientException {
		this.context = context;
	}

    public abstract DocumenterBase<S,D,R,C,T> getDocumenter(IContext<S,D,R,C,T> context, IArguments args, ClientOptions options);

    @DisplayName ("Test Column is a Z column or not")
	@Test
	public void testIfColumnIsZColumn() throws Throwable {
		DocumenterBase<S,D,R,C,T> base = getDocumenter(context, docArguments, new ClientOptions());
		String aZColumn = "z_sampleColumn";
		
		assertTrue(base.isZColumn(aZColumn), "Column is not a Z column");
		String aNonZColumn = "sampleColumn";
		assertFalse(base.isZColumn(aNonZColumn), "Column is a Z column");

	}
	
	@DisplayName ("Test if a directory already exists else it is created")
	@Test
	public void testIfDirectoryAlreadyExistsElseCreate() throws Throwable {
		DocumenterBase<S,D,R,C,T> base = getDocumenter(context, args, new ClientOptions());
		base.checkAndCreateDir(args.getZinggDir());
		assertTrue(Files.exists(Paths.get(args.getZinggDir())), "The directory doesn't exist");
		base.checkAndCreateDir("/an/invalid/dir");
		assertFalse(Files.exists(Paths.get("/a/invalid/dir")), "The directory does exist");
	}

	@DisplayName ("Test process Template to make document")
	@Test
	public void testProcessTemplateToMakeDocument() throws Throwable {
		
		DocumenterBase<S,D,R,C,T> base = getDocumenter(context, args, new ClientOptions());
		base.checkAndCreateDir(args.getZinggDir());

		Map<String, Object> root = new HashMap<String, Object>();
		root.put(TemplateFields.TITLE, "template test");
		root.put(TemplateFields.MODEL_ID, "100");
		List<String> aList = Arrays.asList("welcome", "to", "zingg");
		root.put(TemplateFields.CLUSTERS, aList);
		root.put(TemplateFields.NUM_COLUMNS, 2);
		root.put(TemplateFields.FIELD_DEFINITION_COUNT, 2); 
		root.put(TemplateFields.ISMATCH_COLUMN_INDEX, 0);
		root.put(TemplateFields.CLUSTER_COLUMN_INDEX, 1);; 
		String fileName = args.getZinggDir() + "/testDoc.html";
		base.writeDocument(TEST_DOC_TEMPLATE, root, fileName);
		
		String content = Files.lines(Paths.get(fileName), StandardCharsets.UTF_8)
							.collect(Collectors.joining(System.lineSeparator()));
		assertTrue(content.contains("100"));
		assertTrue(content.contains("welcome"));
		assertTrue(content.contains("zingg"));
		assertTrue(content.contains("template test"));
	}
    
}
