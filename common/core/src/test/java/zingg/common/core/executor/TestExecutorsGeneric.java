package zingg.common.core.executor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;

public abstract class TestExecutorsGeneric<S, D, R, C, T> {

	public static final Log LOG = LogFactory.getLog(TestExecutorsGeneric.class);

	protected S session;
	protected String modelId;
	protected List<ExecutorTester<S, D, R, C, T>> executorTesterList = new ArrayList<ExecutorTester<S, D, R, C, T>>();
	protected String zinggDir;
	
	public TestExecutorsGeneric() {
					
	}
	
	public TestExecutorsGeneric(S s) throws ZinggClientException, IOException {
		init(s);					
	}

	public void init(S s) throws ZinggClientException, IOException {
		this.session = s;
		modelId = "j_"+System.currentTimeMillis();
	}

	protected abstract DFObjectUtil<S,D,R,C> getDFObjectUtil();

	public abstract List<ExecutorTester<S, D, R, C, T>> getExecutors() throws ZinggClientException, IOException;

	//public abstract void tearDown();	

	 @Test
	public void testExecutors() throws ZinggClientException, IOException {
		 // set zingg Dir
		 // to be cleaned up after run
		 setZinggDir();
		 try {
			 List<ExecutorTester<S, D, R, C, T>> executorTesterList = getExecutors();
			 for (ExecutorTester<S, D, R, C, T> executorTester : executorTesterList) {
				 executorTester.setupArgs();
				 executorTester.initAndExecute(session);
				 executorTester.validateResults();
			 }
		 } catch (Throwable throwable) {
			 throw new ZinggClientException("Exception occurred while running one or more test executors, " + throwable.getMessage());
		 } finally {
			 //clear zingg directory created
			 //during tests run
			 clearZinggDir();
		 }

	}
	private void clearZinggDir() {
		 try {
			 File index = new File(this.zinggDir);
			 String[] entries = index.list();
			 assert entries != null;
			 for (String s : entries) {
				 File currentFile = new File(index.getPath(), s);
				 currentFile.delete();
			 }
			 index.delete();
		 } catch (Exception exception) {
			 LOG.error("Exception occurred while deleting Zingg directory");
		 }
	}

	protected abstract void setZinggDir() throws ZinggClientException;
}
