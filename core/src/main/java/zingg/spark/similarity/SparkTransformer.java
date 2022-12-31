package zingg.spark.similarity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.ml.Transformer;
import org.apache.spark.ml.param.Param;
import org.apache.spark.ml.param.ParamMap;
import org.apache.spark.ml.param.shared.HasInputCol;
import org.apache.spark.ml.param.shared.HasOutputCol;
import org.apache.spark.ml.util.Identifiable$;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;

import zingg.client.util.ColName;


public class SparkTransformer extends Transformer implements HasInputCol, HasOutputCol {
	
	Param<String> inputcol = new Param<String>(this, "inputCol", "input column name");
	Param<String> outputcol = new Param<String>(this, "outputCol", "output column name");
	//String inputColumn;
	//String outputColumn;
	protected String name;
	protected String uid;
	
	public static final Log LOG = LogFactory.getLog(SparkTransformer.class);
	
	public String getName() {
		return name;
	}

	public void setName(String n) {
		this.name = n;
	}
	
	
	@Override
	public String getInputCol() {
		return get(inputcol).get();
	}
	
	public void setInputCol(String inputColumn) {
		set(inputcol, inputColumn);
		//this.inputColumn = inputColumn;
	}

	@Override
	public String getOutputCol() {
		return get(outputcol).get();
	}

	public void setOutputCol(String outputColumn) {
		set(outputcol, outputColumn);
	}
	
	@Override	
	public Dataset<Row> transform(Dataset<?> ds){
		//LOG.debug("transforming dataset for " + uid);
		transformSchema(ds.schema());
		return ds.withColumn(getOutputCol(), 
				functions.callUDF(this.uid, ds.col(getInputCol()), 
						ds.col(ColName.COL_PREFIX + getInputCol())));
	}
	
	@Override
	public StructType transformSchema(StructType structType) {
		//LOG.debug("transforming schema for " + uid);
		return structType.add(getOutputCol(),DataTypes.DoubleType,true);
	}
	
	
	@Override
    public SparkTransformer copy(ParamMap paramMap) {
	  LOG.debug("Copying params for " + this.uid);
	  paramMap.put(inputcol, get(inputcol).get());
	  paramMap.put(outputcol, get(outputcol).get());
      return defaultCopy(paramMap);
    }
	
    @Override
    public String uid() {
      return getUid();
    } 
    
    public String getUid() {
    	if (uid == null) {
    		uid = Identifiable$.MODULE$.randomUID(name);
    	}
    	return uid;
    }
    
    
    public Param<String> inputCol() {
    	return inputcol;
    }
    
   
    public Param<String> outputCol() {
    	return outputcol;
    }
    
    public void org$apache$spark$ml$param$shared$HasInputCol$_setter_$inputCol_$eq(org.apache.spark.ml.param.Param param) {
    	inputcol = param;
    }
    
    public void org$apache$spark$ml$param$shared$HasOutputCol$_setter_$outputCol_$eq(org.apache.spark.ml.param.Param param) {
    	outputcol = param;
    }
    
	
	public SparkTransformer() {
		
	}

	public SparkTransformer(String name) {
		this.name = name;
	}
	
	 
    public void register(SparkSession spark) {
    	spark.udf().register(uid, (UDF2) this, DataTypes.DoubleType);
    }
   

}

