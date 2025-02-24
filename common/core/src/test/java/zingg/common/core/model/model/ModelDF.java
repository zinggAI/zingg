package zingg.common.core.model.model;

import java.sql.Date;

public class ModelDF {

    public final Integer z_zid;
    public final String z_cluster;
    public final String name;
    public final String event;
    public final String comment;
    public final Integer year;
    public final Date dob;
    public final Integer z_isMatch;
    public final Integer z_z_zid;
    public final String z_z_cluster;
    public final String z_name;
    public final String z_event;
    public final String z_comment;
    public final Integer z_year;
    public final Date z_dob;
    public final Integer z_z_isMatch;

    public ModelDF(Integer z_zid,String z_cluster,String name,String event,String comment,Integer year,Date dob,Integer z_isMatch,
        Integer z_z_zid,String z_z_cluster,String z_name,String z_event,String z_comment,Integer z_year,Date z_dob,Integer z_z_isMatch){
            this.z_zid = z_zid;
            this.z_cluster = z_cluster;
            this.name = name;
            this.event = event;
            this.comment = comment;
            this.year = year;
            this.dob = dob;
            this.z_isMatch = z_isMatch;
            this.z_z_zid = z_z_zid;
            this.z_z_cluster = z_z_cluster;
            this.z_name = z_name;
            this.z_event = z_event;
            this.z_comment = z_comment;
            this.z_year = z_year;
            this.z_dob = z_dob;
            this.z_z_isMatch = z_z_isMatch;
    }
    
}