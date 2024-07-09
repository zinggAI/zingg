package zingg.common.client.model;

public class Pair {
    public final String z_cluster;
    public final Double z_score;
    public final String z_zsource;

    public Pair(String z_cluster, Double z_score, String z_zsource) {
        this.z_cluster = z_cluster;
        this.z_score = z_score;
        this.z_zsource = z_zsource;
    }
}
