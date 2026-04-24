package zingg.spark.client.util.reader.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrameReader;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.common.client.ZFrame;
import zingg.common.client.ZinggClientException;
import zingg.common.client.pipe.FilePipe;
import zingg.common.client.pipe.Pipe;
import zingg.common.client.util.reader.IDFReader;
import zingg.common.client.util.reader.ReadStrategy;
import zingg.spark.client.SparkFrame;
import zingg.spark.client.util.SparkDFReader;

public class UnityCatalogReadStrategy implements ReadStrategy<Dataset<Row>, Row, Column> {
    private static final Log LOG = LogFactory.getLog(UnityCatalogReadStrategy.class);

    @Override
    public ZFrame<Dataset<Row>, Row, Column> read(IDFReader<Dataset<Row>, Row, Column> idfReader, Pipe<Dataset<Row>, Row, Column> pipe) throws ZinggClientException {
        LOG.info("Reading data using unity catalog");
        DataFrameReader dataFrameReader = ((SparkDFReader)idfReader).getDataFrameReader();
        return new SparkFrame(dataFrameReader.table(pipe.get(FilePipe.TABLE)));
    }
}