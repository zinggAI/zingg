package zingg.common.core.executor.validate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.jupiter.api.Assertions;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.Matcher;

public class LinkerValidator<S, D, R, C, T> extends MatcherValidator<S, D, R, C, T> {

    public static final Log LOG = LogFactory.getLog(LinkerValidator.class);
	
	public LinkerValidator(Matcher<S, D, R, C, T> executor) {
		super(executor);
	}

	@Override
	public void validateResults() throws ZinggClientException {
		assessAccuracy();		
	}

    @Override
	protected void assessAccuracy() throws ZinggClientException {
		ZFrame<D, R, C> linkOutput = getOutputData();

		Assertions.assertEquals(11, linkOutput.count());
		/*
			candidate blake will be linked to one source record -> candidate + one source -> 2 records in cluster1
			candidate thomas will be linked to one source record -> candidate + one source -> 2 records in cluster2
			candidate jackson will be linked to two source records -> candidate + two source -> 3 records in cluster3
			candidate gianni 1st will be linked to one source record -> candidate + one source -> 2 records in cluster4
			candidate gianni 2nd will be linked to one source record -> candidate + one source -> 2 records in cluster5
			candidate takeisha has no source record

			total 2 + 2 + 3 + 2 + 2 = 11 records
		 */
		ZFrame<D, R, C> blakeCluster = linkOutput.filter(linkOutput.equalTo("fname", "blake"));
		ZFrame<D, R, C> thomasCluster = linkOutput.filter(linkOutput.equalTo("fname", "thomas"));
		ZFrame<D, R, C> jacksonCluster = linkOutput.filter(linkOutput.equalTo("fname", "jackson"));
		ZFrame<D, R, C> gianniCluster = linkOutput.filter(linkOutput.equalTo("fname", "gianni"));
		ZFrame<D, R, C> takeishaCluster = linkOutput.filter(linkOutput.equalTo("fname", "takeisha"));

		Assertions.assertEquals(2, blakeCluster.count());
		Assertions.assertEquals(2, thomasCluster.count());
		Assertions.assertEquals(3, jacksonCluster.count());
		Assertions.assertEquals(4, gianniCluster.count());
		Assertions.assertEquals(0, takeishaCluster.count());

	}

}
