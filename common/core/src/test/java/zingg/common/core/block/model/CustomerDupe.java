package zingg.common.core.block.model;

public class CustomerDupe {
    String id;
    String fname;
    String lname;
    String stNo;
    String add1;
    String add2;
    String city;
    String areacode;
    String state;
    String dob;
    String ssn;
    String z_zid;
    String z_fname;
    String z_lname;
    String z_stNo;
    String z_add1;
    String z_add2;
    String z_city;
    String z_areacode;
    String z_state;
    String z_dob;
    String z_ssn;

    public CustomerDupe(String... arguments) {
        this.id = arguments[0];
        this.fname = arguments[1];
        this.lname = arguments[2];
        this.stNo = arguments[3];
        this.add1 = arguments[4];
        this.add2 = arguments[5];
        this.city = arguments[6];
        this.state = arguments[7];
        this.areacode = arguments[8];
        this.dob = arguments[9];
        this.ssn = arguments[10];
        this.z_zid = arguments[11];
        this.z_fname = arguments[12];
        this.z_lname = arguments[13];
        this.z_stNo = arguments[14];
        this.z_add1 = arguments[15];
        this.z_add2 = arguments[16];
        this.z_city = arguments[17];
        this.z_areacode = arguments[18];
        this.z_state = arguments[19];
        this.z_dob = arguments[20];
        this.z_ssn = arguments[21];
    }
}