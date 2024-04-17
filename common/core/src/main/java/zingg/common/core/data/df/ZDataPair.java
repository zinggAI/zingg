package zingg.common.core.data.df;

import zingg.common.client.ZFrame;
import zingg.common.core.pairs.IPairBuilder;

public class ZDataPair<S, D, R, C, T> {

	protected ZFrame<D,R,C> data1;
	protected ZFrame<D,R,C> data2;
	protected ZFrame<D,R,C> pairs;
	protected IPairBuilder<S, D, R, C> iPairBuilder;
	
	public ZDataPair(ZFrame<D, R, C> data1, ZFrame<D, R, C> data2, IPairBuilder<S, D, R, C> iPairBuilder) {
		this.data1 = data1;
		this.data2 = data2;
		this.iPairBuilder = iPairBuilder;
	}

	public ZFrame<D, R, C> getData1() {
		return data1;
	}

	public ZFrame<D, R, C> getData2() {
		return data2;
	}

	public ZFrame<D, R, C> getPairs() throws Exception {
		if (pairs==null) {
			pairs = iPairBuilder.getPairs(data1, data2);
		}
		return pairs;
	}

	public IPairBuilder<S, D, R, C> getIPairBuilder() {
		return iPairBuilder;
	}
	
	

}
