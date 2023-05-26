package zingg.common.client.util;

public interface ColValues {

    public static final int MATCH_TYPE_UNKNOWN = -1;
    public static final int MATCH_TYPE_NOT_A_MATCH = 0;
    public static final int MATCH_TYPE_MATCH = 1;
    public static final int MATCH_TYPE_NOT_SURE = 2;
    
    public static final double IS_MATCH_PREDICTION = 1.0;
    public static final double IS_NOT_A_MATCH_PREDICTION = 0.0;
    public static final double IS_NOT_KNOWN_PREDICTION = -1.0;
    public static final double IS_NOT_SURE_PREDICTION = 2.0;
    
    public static final double ZERO_SCORE = 0.0;
    public static final double FULL_MATCH_SCORE = 1.0;
    
}
