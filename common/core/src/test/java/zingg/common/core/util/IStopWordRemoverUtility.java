package zingg.common.core.util;

import zingg.common.client.IArguments;
import zingg.common.client.ZinggClientException;
import zingg.common.core.context.Context;
import zingg.common.core.preprocess.StopWordsRemover;

import java.util.List;

public interface IStopWordRemoverUtility<S, D, R, C, T> {

    List<StopWordsRemover<S, D, R, C, T>> getStopWordRemovers(Context<S, D, R, C, T> context, IArguments arguments) throws ZinggClientException;
}
