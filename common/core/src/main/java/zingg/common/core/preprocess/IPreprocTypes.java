package zingg.common.core.preprocess;

public interface IPreprocTypes {

    public final static IPreprocType STOPWORDS = new PreprocType("stopwords", ProcessingType.SINGLE);
    public final static IPreprocType LOWERCASE = new PreprocType("lowercase", ProcessingType.MULTI);
    public final static IPreprocType TRIM = new PreprocType("trim", ProcessingType.MULTI);
}
