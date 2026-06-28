package zingg.spark.core.similarity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.DataTypes;

import org.apache.spark.sql.SparkSession;
import zingg.spark.core.util.SparkFnRegistrar;


public class SparkTransformer extends SparkBaseTransformer {
	private static final long serialVersionUID = 1L;

	protected SparkSimFunction function;
	
	public static final Log LOG = LogFactory.getLog(SparkTransformer.class);

    public SparkTransformer(){
        
    }

    public SparkTransformer(String uid) {
        super(uid);
    }
	
	

    public SparkTransformer(String inputCol, SparkSimFunction function, String outputCol) {
        super(inputCol, outputCol, function.getName() + "_" + inputCol);
        this.function = function;
    }

   

	 
    public void register(SparkSession spark) {

        SparkFnRegistrar.registerUDF2(spark, getUid(), function, DataTypes.DoubleType);
    }
   

}

