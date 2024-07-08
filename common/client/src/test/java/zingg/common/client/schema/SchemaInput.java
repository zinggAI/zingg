package zingg.common.client.schema;

public class SchemaInput {
    public final Integer z_zid;
    public final String fname;
    public final String z_zsource;

    public SchemaInput(Integer z_zid, String fname, String z_zsource) {
        this.z_zid = z_zid;
        this.fname = fname;
        this.z_zsource = z_zsource;
    }
}