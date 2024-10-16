package zingg.spark.core.export;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.deploy.PythonRunner;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataType;
import zingg.common.client.Client;
import zingg.common.client.ClientOptions;
import zingg.common.client.ZinggClientException;
import zingg.common.core.executor.ZinggBase;

import java.util.ArrayList;
import java.util.List;

public class SparkModelExporter extends ZinggBase<SparkSession, Dataset<Row>, Row, Column, DataType> {
    /*
        //TODO - class needs to be refactored along with Client class
     */

    public static String name = "zingg.spark.core.export.SparkModelExporter";
    public static final Log LOG = LogFactory.getLog(Client.class);

    @Override
    public void execute() throws ZinggClientException {
        try {
            LOG.info("Generic Python phase starts");
            List<String> pyArgs = new ArrayList<String>();
            pyArgs.add("python/phases/" + clientOptions.get(ClientOptions.PHASE).getValue() + ".py");
            pyArgs.add("");
            for (String c: clientOptions.getCommandLineArgs()) {
                pyArgs.add(c);
            }
            PythonRunner.main(pyArgs.toArray(new String[pyArgs.size()]));

            LOG.info("Generic Python phase ends");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ZinggClientException(e.getMessage());
        }
    }
}
