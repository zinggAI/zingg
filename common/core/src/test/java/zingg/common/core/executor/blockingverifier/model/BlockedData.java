package zingg.common.core.executor.blockingverifier.model;

public class BlockedData {

    public final String z_zid;
    public final String rec_id;
    public final String fname;
    public final String lname;
    public final String add1;
    public final String city;
    public final String state;
    public final String ssn;
    public final String z_source;
    public final String z_hash;

    public BlockedData(String z_zid,String rec_id,String fname,String lname,String add1,
    String city,String state,String ssn,String z_source,String z_hash){
        this.z_zid = z_zid;
        this.rec_id = rec_id;
        this.fname = fname;
        this.lname = lname;
        this.add1 = add1;
        this.city = city;
        this.state = state;
        this.ssn = ssn;
        this.z_source = z_source;
        this.z_hash = z_hash;
    }
    
}


