package zingg.common.client.util;

public interface ColName {

	public static final String COL_PREFIX = "z_";
	public static final String HASH_COL = COL_PREFIX + "hash";
	public static final String HASH_COL1= ColName.COL_PREFIX + ColName.HASH_COL;
	public static final String MATCH_FLAG_COL = COL_PREFIX + "isMatch";
	public static final String ID_COL = COL_PREFIX + "zid";
	public static final String ID_EXTERNAL_ORIG_COL = "id";
	public static final String ID_EXTERNAL_COL = COL_PREFIX + ID_EXTERNAL_ORIG_COL;
	public static final String CLUSTER_COLUMN = COL_PREFIX + "cluster"; 
	public static final String SIM_COL = COL_PREFIX + "sim";
	public static final String FEATURE_VECTOR_COL = COL_PREFIX + "featurevector";
	public static final String FEATURE_COL = COL_PREFIX + "feature";
	public static final String PROBABILITY_COL = COL_PREFIX + "probability";
	public static final String PREDICTION_COL = COL_PREFIX + "prediction";
	public static final String SCORE_COL = COL_PREFIX + "score";
	public static final String SCORE_MIN_COL = COL_PREFIX + "minScore";
	public static final String SCORE_MAX_COL = COL_PREFIX + "maxScore";
	public static final String SPARK_JOB_ID_COL = COL_PREFIX + "sparkJobId";
	public static final String SOURCE_COL = COL_PREFIX + "source";
	public static final String SCORE_KEY_COL = COL_PREFIX + "scorekey";
	public static final String DENSE_COL = COL_PREFIX + "dense";
	public static final String UPDATED_AT = COL_PREFIX + "updated";
	public static final String UPDATED_AT_REAL = COL_PREFIX + "updated_real";
	public static final String ACTION = COL_PREFIX + "action";
	public static final String USER = COL_PREFIX + "user";
	public static final String MODEL_ID_COL = COL_PREFIX + "modelId";
	public static final String RAW_PREDICTION="rawPrediction";
	public static final String COL_COUNT = COL_PREFIX + "count";
	public static final String COL_WORD = COL_PREFIX + "word";
	public static final String COL_SPLIT = COL_PREFIX + "split";
	
	
}