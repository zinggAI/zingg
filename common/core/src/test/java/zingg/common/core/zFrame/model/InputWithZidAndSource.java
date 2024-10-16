package zingg.common.core.zFrame.model;

public class InputWithZidAndSource {
    public final Long z_zid;
    public final String fname;
    public final String z_zsource;

    public InputWithZidAndSource(Long z_zid, String fname, String z_zsource) {
        this.z_zid = z_zid;
        this.fname = fname;
        this.z_zsource = z_zsource;
    }
}