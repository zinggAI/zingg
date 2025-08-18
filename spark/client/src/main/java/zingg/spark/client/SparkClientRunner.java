package zingg.spark.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import zingg.common.client.*;
import zingg.common.client.arguments.model.IZArgs;

public class SparkClientRunner extends ClientRunner<SparkSession, Dataset<Row>, Row, Column> {
    public static final Log LOG = LogFactory.getLog(SparkClientRunner.class);

    public static void main(String[] args) {
        ClientRunner<SparkSession, Dataset<Row>, Row, Column> clientRunner = new SparkClientRunner();
        clientRunner.mainMethod(args);
    }

    @Override
    protected IZinggFactory getZinggFactory() throws ZinggClientException {
        return ZinggFactoryProvider.getZinggFactory("zingg.spark.core.executor.SparkZFactory");
    }

    @Override
    protected Client<SparkSession, Dataset<Row>, Row, Column> getClient(BannerPrinter bannerPrinter, IZingg<SparkSession, Dataset<Row>, Row, Column> zingg, ClientOptions clientOptions, IZArgs args) {
        SparkClient sparkClient = new SparkClient(bannerPrinter);
        sparkClient.setOptions(clientOptions);
        sparkClient.setArguments(args);
        sparkClient.setZingg(zingg);
        return sparkClient;
    }
}
