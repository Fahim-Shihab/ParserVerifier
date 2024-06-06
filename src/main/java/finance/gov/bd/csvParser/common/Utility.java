package finance.gov.bd.csvParser.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static Date convertStringToDate(String startDateString) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = null;
        try {
            startDate = df.parse(startDateString);
            String newDateString = df.format(startDate);
            //System.out.println(newDateString);
        } catch (ParseException e) {
            //e.printStackTrace();
            return null;
        }

        return startDate;

    }
}
