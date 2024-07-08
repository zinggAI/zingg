package zingg.common.client.schema;

public class SchemaCluster {
    public final Integer z_zid;
    public final Integer z_cluster;
    public final Integer z_score;
    public final String z_zsource;

    public SchemaCluster(Integer z_zid, Integer z_cluster, Integer z_score, String z_zsource) {
        this.z_zid = z_zid;
        this.z_cluster = z_cluster;
        this.z_score = z_score;
        this.z_zsource = z_zsource;
    }
}
