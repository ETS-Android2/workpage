package jajimenez.workpage.logic;

import java.util.Calendar;
import java.text.DateFormat;

public class DateTimeTool {
    // This function only considers the date of "calendar" (year, month and day),
    // ignoring its time (hour, minute, second, millisecond).
    public String getIso8601DateTime(Calendar calendar) {
        String date = null;

        if (calendar != null) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            date = String.format("%d-%02d-%02d 00:00:00.000", year, month, day);
        }

        return date; 
    }

    public Calendar getCalendar(String iso8601DateTime) {
        Calendar calendar = null;
        
        if (iso8601DateTime != null && !iso8601DateTime.equals("")) {
            calendar = Calendar.getInstance();

            int year = Integer.valueOf(iso8601DateTime.substring(0, 4));
            int month = Integer.valueOf(iso8601DateTime.substring(5, 7));
            int day = Integer.valueOf(iso8601DateTime.substring(8, 10));

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar;
    }

    public String getInterfaceFormattedDate(Calendar calendar) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateFormat.format(calendar.getTime());
    }
}
