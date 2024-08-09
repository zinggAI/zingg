package zingg.common.client.model;

public class ClusterZScore {
    public final Long z_zid;
    public final String z_cluster;
    public final Double z_score;

    public ClusterZScore(Long z_zid, String z_cluster, Double z_score) {
        this.z_zid = z_zid;
        this.z_cluster = z_cluster;
        this.z_score = z_score;
    }
}
