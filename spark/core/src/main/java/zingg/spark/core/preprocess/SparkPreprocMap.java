package zingg.spark.core.preprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import zingg.common.core.preprocess.IPreprocMap;
import zingg.common.core.preprocess.IPreprocType;
import zingg.common.core.preprocess.IPreprocTypes;
import zingg.common.core.preprocess.IPreprocessor;
import zingg.spark.core.preprocess.casenormalize.SparkCaseNormalizer;
import zingg.spark.core.preprocess.stopwords.SparkStopWordsRemover;

public class SparkPreprocMap implements IPreprocMap<SparkSession,Dataset<Row>,Row,Column,DataType> {

    protected Map<IPreprocType,Class<? extends IPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType>>> sparkPreprocMap;

    public SparkPreprocMap(){
        sparkPreprocMap = new HashMap<IPreprocType,Class<? extends IPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType>>>();
        sparkPreprocMap.put(IPreprocTypes.STOPWORDS, SparkStopWordsRemover.class);
        sparkPreprocMap.put(IPreprocTypes.LOWERCASE, SparkCaseNormalizer.class);
    }

    @Override
    public void put(IPreprocType t, Class<? extends IPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType>> p) {
        this.sparkPreprocMap.put(t,p);
    }

    @Override
    public Class<? extends IPreprocessor<SparkSession, Dataset<Row>, Row, Column, DataType>> get(IPreprocType t) {
        return this.sparkPreprocMap.get(t);
    }
    
}
