package zingg.common.core.hash;
import java.util.Date;
import java.time.LocalDate;
public class IdentityDate extends BaseHash<Date, Integer> {

    public IdentityDate() {
        setName("identityDate");
    }

    @Override
    public Integer call(Date date) {
        if(date == null) {
            return null;
        }
        int year = date.getYear() + 1900;
        int month = date.getMonth() + 1;
        int day = date.getDate();
        return year * 10000 + month * 100 + day;
    }
}
