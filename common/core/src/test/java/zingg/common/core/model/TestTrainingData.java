package zingg.common.core.model;

public class TestTrainingData {
    public final String z_cluster;
    public final String z_isMatch;
    public final String field1;
    public final String z_prediction;
    public final String z_score;

    public TestTrainingData(String z_cluster, String z_isMatch, String field1, String z_prediction, String z_score) {
        this.z_cluster = z_cluster;
        this.z_isMatch = z_isMatch;
        this.field1 = field1;
        this.z_prediction = z_prediction;
        this.z_score = z_score;
    }
    
}
