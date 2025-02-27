package zingg.spark.core.preprocess;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.preprocess.INeedsPreprocMap;
import zingg.common.core.preprocess.IPreprocMap;
public interface ISparkPreprocMapSupplier extends INeedsPreprocMap<SparkSession,Dataset<Row>,Row,Column,DataType> {

    default IPreprocMap<SparkSession,Dataset<Row>,Row,Column,DataType> getPreprocMap(){
        return new SparkPreprocMap();
    }
    
}
