package zingg.spark.core.executor;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import zingg.common.client.IArguments;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.PipeUtilBase;
import zingg.common.core.executor.verifyblocking.VerifyBlockingPipes;

public class SparkVerifyBlockingPipes extends VerifyBlockingPipes<SparkSession,Dataset<Row>,Row,Column>{

    public SparkVerifyBlockingPipes(PipeUtilBase<SparkSession,Dataset<Row>,Row,Column> pipeUtil, long timestamp){
        super(pipeUtil,timestamp);
    }

    @Override
    public Pipe<Dataset<Row>,Row,Column> getPipeForVerifyBlockingLocation(IArguments args, String type){
        Pipe<Dataset<Row>,Row,Column> p = new Pipe<Dataset<Row>,Row,Column>();
		p.setFormat(Pipe.FORMAT_PARQUET);
		p.setProp(FilePipe.LOCATION, getName(args,timestamp,type));
		p = pipeUtil.setOverwriteMode(p);
		return p;
    }

    private String getName(IArguments args, long timestamp, String type){
        return args.getZinggModelDir() + "/blocks/" + timestamp + "/" + type;
    }
    
    
}
