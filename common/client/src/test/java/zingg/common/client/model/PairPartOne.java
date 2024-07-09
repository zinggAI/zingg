package zingg.common.client.model;

public class PairPartOne extends Pair {
    public final Long z_zid;

    public PairPartOne(Long z_zid, String z_cluster, Double z_score, String z_zsource) {
        super(z_cluster, z_score, z_zsource);
        this.z_zid = z_zid;
    }
}
