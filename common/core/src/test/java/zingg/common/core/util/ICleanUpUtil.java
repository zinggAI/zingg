package zingg.common.core.util;

public interface ICleanUpUtil<S> {

    boolean performCleanup(S session, TestType testType, String modelId);

}
