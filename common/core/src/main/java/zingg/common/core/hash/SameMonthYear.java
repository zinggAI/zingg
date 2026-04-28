package zingg.common.core.hash;

import java.util.Date;

public class SameMonthYear extends BaseHash<Date, Integer>{
    public SameMonthYear() {
        setName("sameMonthYear");
    }

    @Override
    public Integer call(Date date) {
        if(date == null) {
            return null;
        }
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        return year * 100 + month;
    }
}
