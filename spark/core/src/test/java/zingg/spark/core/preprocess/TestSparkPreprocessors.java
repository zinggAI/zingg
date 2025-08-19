package zingg.spark.core.preprocess;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;

import org.junit.jupiter.api.extension.ExtendWith;
import zingg.spark.core.TestSparkBase;
import zingg.common.client.arguments.model.IZArgs;
import zingg.common.client.ZinggClientException;
import zingg.common.client.util.IWithSession;
import zingg.common.client.util.WithSession;
import zingg.common.core.context.Context;
import zingg.common.core.context.IContext;
import zingg.common.core.preprocess.IPreprocessors;
import zingg.common.core.preprocess.TestPreprocessors;
import zingg.spark.client.util.SparkDFObjectUtil;
import zingg.spark.core.context.ZinggSparkContext;

@ExtendWith(TestSparkBase.class)
public class TestSparkPreprocessors extends TestPreprocessors<SparkSession, Dataset<Row>, Row, Column, DataType> {

    public static IWithSession<SparkSession> iWithSession = new WithSession<SparkSession>();
    public static ZinggSparkContext zsCTX = new ZinggSparkContext();

    public TestSparkPreprocessors(SparkSession sparkSession) throws ZinggClientException{
        super(new SparkDFObjectUtil(iWithSession), zsCTX);
        iWithSession.setSession(sparkSession);
        zsCTX.init(sparkSession);
    }

    @Override
    public IPreprocessors<SparkSession, Dataset<Row>, Row, Column, DataType> getPreprocessors(Context<SparkSession, Dataset<Row>, Row,Column,DataType> c) {
        return new TestSparkPrecos(zsCTX);
    }

    public class TestSparkPrecos implements IPreprocessors<SparkSession, Dataset<Row>, Row, Column, DataType>, ISparkPreprocMapSupplier{

        IContext<SparkSession, Dataset<Row>, Row, Column, DataType> c;
        IZArgs args;

        TestSparkPrecos(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> c){
            setContext(c);
        }

        @Override
        public void setContext(IContext<SparkSession, Dataset<Row>, Row, Column, DataType> c) {
           this.c = c;
        }

        @Override
        public IContext<SparkSession, Dataset<Row>, Row, Column, DataType> getContext() {
           return c;
        }

        @Override
        public IZArgs getArgs() {
            return this.args;
        }

        @Override
        public void setArgs(IZArgs args) {
           this.args = args;
        }
        
    }
    
}
