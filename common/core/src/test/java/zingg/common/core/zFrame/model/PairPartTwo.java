package zingg.common.core.zFrame.model;

public class PairPartTwo {
    public final Long z_z_zid;
    public final String z_cluster;
    public final Double z_score;
    public final String z_zsource;

    public PairPartTwo(Long z_z_zid, String z_cluster, Double z_score, String z_zsource) {
        this.z_z_zid = z_z_zid;
        this.z_cluster = z_cluster;
        this.z_score = z_score;
        this.z_zsource = z_zsource;
    }
}