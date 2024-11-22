package zingg.spark.core.executor;

import org.apache.spark.internal.config.R;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.IModelHelper;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.executor.blockingverifier.VerifyBlockingPipes;
import zingg.spark.client.pipe.SparkPipe;

public class SparkVerifyBlockingPipes extends VerifyBlockingPipes<SparkSession,Dataset<Row>,Row,Column>{

    public SparkVerifyBlockingPipes(PipeUtilBase<SparkSession,Dataset<Row>,Row,Column> pipeUtil, long timestamp, IModelHelper<Dataset<Row>,Row,Column> mh){
        super(pipeUtil,timestamp, mh);
    }

    @Override
    public Pipe<Dataset<Row>,Row,Column> getPipeForVerifyBlockingLocation(IArguments args, String type){
        SparkPipe p = new SparkPipe();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p.setOverwriteMode();
		return p;
    }

    private String getName(IArguments args, long timestamp, String type){
        return getModelHelper().getModel(args) + "/blocks/" + timestamp + "/" + type;
    }

    

    
    
    
}
