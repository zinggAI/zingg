
package zingg.spark.core.block.model;

import java.util.List;

public class Customer {
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

    public Customer(String... arguments){
        List<String> argumentsList = List.of(arguments);

        this.id = argumentsList.get(0);
        this.fname = argumentsList.get(1);
        this.lname = argumentsList.get(2);
        this.stNo = argumentsList.get(3);
        this.add1 = argumentsList.get(4);
        this.add2 = argumentsList.get(5);
        this.city = argumentsList.get(6);
        this.areacode = argumentsList.get(7);
        this.state = argumentsList.get(8);
        this.dob = argumentsList.get(9);
        this.ssn = argumentsList.get(10);
    }
}