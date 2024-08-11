package zingg.common.core.block;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import zingg.common.client.ArgumentsUtil;
import zingg.common.client.FieldDefinition;
import zingg.common.client.IArguments;
import zingg.common.client.MatchType;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.DFObjectUtil;
import zingg.common.core.util.BlockingTreeUtil;
import zingg.common.core.util.HashUtil;
import zingg.common.core.model.Event;
import zingg.common.core.model.EventPair;
import zingg.common.core.data.TestData;

public abstract class TestBlockBase<S, D, R, C, T> {

	public ArgumentsUtil argumentsUtil = new ArgumentsUtil();
	public final DFObjectUtil<S, D, R, C> dfObjectUtil;
	public final HashUtil<S, D, R, C, T> hashUtil;
	public final BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil;

	public TestBlockBase(DFObjectUtil<S, D, R, C> dfObjectUtil, HashUtil<S, D, R, C, T> hashUtil, BlockingTreeUtil<S, D, R, C, T> blockingTreeUtil) {
		this.dfObjectUtil = dfObjectUtil;
		this.hashUtil = hashUtil;
		this.blockingTreeUtil = blockingTreeUtil;
	}

	@Test
	public void testTree() throws Throwable {

		// form tree
		ZFrame<D, R, C> zFrameEvent = dfObjectUtil.getDFFromObjectList(TestData.createSampleEventData(), Event.class);
		ZFrame<D, R, C> zFrameEventCluster = dfObjectUtil.getDFFromObjectList(TestData.createSampleClusterEventData(), EventPair.class);
		IArguments args = getArguments();

		Tree<Canopy<R>> blockingTree = blockingTreeUtil.createBlockingTreeFromSample(zFrameEvent, zFrameEventCluster, 0.5, -1,
				args, hashUtil.getHashFunctionList());
				
		// primary deciding is unique year so identityInteger should have been picked
		Canopy<R> head = blockingTree.getHead();
		assertEquals("identityInteger", head.getFunction().getName());
		blockingTree.toString();
	}

	private IArguments getArguments() throws ZinggClientException {
		String configFilePath = Objects.requireNonNull(getClass().getResource("../../../../testFebrl/config.json")).getFile();

		IArguments args = argumentsUtil.createArgumentsFromJSON(configFilePath, "trainMatch");

		List<FieldDefinition> fdList = getFieldDefList();

		args.setFieldDefinition(fdList);
		return args;
	}

	private List<FieldDefinition> getFieldDefList() {
		List<FieldDefinition> fdList = new ArrayList<FieldDefinition>(4);

		FieldDefinition idFD = new FieldDefinition();
		idFD.setDataType("integer");
		idFD.setFieldName("id");
		ArrayList<MatchType> matchTypelistId = new ArrayList<MatchType>();
		matchTypelistId.add(MatchType.DONT_USE);
		idFD.setMatchType(matchTypelistId);
		fdList.add(idFD);
		
		ArrayList<MatchType> matchTypelistFuzzy = new ArrayList<MatchType>();
		matchTypelistFuzzy.add(MatchType.FUZZY);

		
		FieldDefinition yearFD = new FieldDefinition();
		yearFD.setDataType("integer");
		yearFD.setFieldName("year");
		yearFD.setMatchType(matchTypelistFuzzy);
		fdList.add(yearFD);
		
		FieldDefinition eventFD = new FieldDefinition();
		eventFD.setDataType("string");
		eventFD.setFieldName("event");
		eventFD.setMatchType(matchTypelistFuzzy);
		fdList.add(eventFD);
		
		FieldDefinition commentFD = new FieldDefinition();
		commentFD.setDataType("string");
		commentFD.setFieldName("comment");
		commentFD.setMatchType(matchTypelistFuzzy);
		fdList.add(commentFD);
		return fdList;
	}

}
