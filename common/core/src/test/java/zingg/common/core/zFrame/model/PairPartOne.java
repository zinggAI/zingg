package zingg.common.core.zFrame.model;

public class PairPartOne {
    public final Long z_zid;
    public final String z_cluster;
    public final Double z_score;
    public final String z_zsource;

    public PairPartOne(Long z_zid, String z_cluster, Double z_score, String z_zsource) {
        this.z_zid = z_zid;
        this.z_cluster = z_cluster;
        this.z_score = z_score;
        this.z_zsource = z_zsource;
    }
}
