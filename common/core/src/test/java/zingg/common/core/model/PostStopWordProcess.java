package zingg.common.core.model;

public class PostStopWordProcess {
    public final String z_cluster;
    public final String z_zid;
    public final String z_prediction;
    public final String z_score;
    public final String z_isMatch;
    public final String field1;
    public final String field2;
    public final String field3;
    public final String z_zsource;

    public PostStopWordProcess(String z_cluster, String z_zid, String z_prediction, String z_score, String z_isMatch,
                               String field1, String field2, String field3, String z_zsource) {
        this.z_cluster = z_cluster;
        this.z_zid = z_zid;
        this.z_prediction = z_prediction;
        this.z_score = z_score;
        this.z_isMatch = z_isMatch;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.z_zsource = z_zsource;
    }
}
