package zingg.domain.model.datasets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import zingg.client.util.ColName;
import zingg.domain.model.labels.Labeller;

public class Join implements DatasetCombiner{
    public static final Log LOG = LogFactory.getLog(Labeller.class);

    @Override
    public Dataset<Row> combine(Dataset<Row> jdbc,Dataset<Row> file)  {
        file = file.drop(ColName.MATCH_FLAG_COL);
        file.printSchema();
        file.show();
        jdbc = jdbc.select(jdbc.col(ColName.ID_COL), jdbc.col(ColName.SOURCE_COL),jdbc.col(ColName.MATCH_FLAG_COL),
                jdbc.col(ColName.CLUSTER_COLUMN));
        String[] cols = jdbc.columns();
        for (int i=0; i < cols.length; ++i) {
            cols[i] = ColName.COL_PREFIX + cols[i];
        }
        jdbc = jdbc.toDF(cols).cache();
        jdbc = jdbc.withColumnRenamed(ColName.COL_PREFIX + ColName.MATCH_FLAG_COL, ColName.MATCH_FLAG_COL);
        jdbc.printSchema();
        jdbc.show();
        LOG.warn("Building labels ");
        Dataset<Row> pairs = file.join(jdbc, file.col(ColName.ID_COL).equalTo(
                        jdbc.col(ColName.COL_PREFIX + ColName.ID_COL))
                .and(file.col(ColName.SOURCE_COL).equalTo(
                        jdbc.col(ColName.COL_PREFIX + ColName.SOURCE_COL)))
                .and(file.col(ColName.CLUSTER_COLUMN).equalTo(
                        jdbc.col(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN))));
        LOG.warn("Pairs are " + pairs.count());
        //in training, we only need that record matches only with lines bigger than itself
        //in the case of normal as well as in the case of linking
        pairs = pairs.drop(ColName.COL_PREFIX + ColName.SOURCE_COL);
        pairs = pairs.drop(ColName.COL_PREFIX + ColName.ID_COL);
        pairs = pairs.drop(ColName.COL_PREFIX + ColName.CLUSTER_COLUMN);

        return pairs;
    }

}
