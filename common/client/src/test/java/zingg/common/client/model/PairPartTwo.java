package zingg.common.client.model;

public class PairPartTwo extends Pair {
    public final Long z_z_zid;

    public PairPartTwo(Long z_z_zid, String z_cluster, Double z_score, String z_zsource) {
        super(z_cluster, z_score, z_zsource);
        this.z_z_zid = z_z_zid;
    }
}