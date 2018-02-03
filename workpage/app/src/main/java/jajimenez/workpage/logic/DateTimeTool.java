package jajimenez.workpage.logic;

import java.util.Calendar;

public class DateTimeTool {
    // Used only for upgrading the database.
    public Calendar getCalendar(String iso8601DateTime) {
        Calendar calendar = null;
        
        if (iso8601DateTime != null && !iso8601DateTime.equals("")) {
            calendar = Calendar.getInstance();
            calendar.clear(); // Clear all the time fields, setting them to 0.

            int year = Integer.valueOf(iso8601DateTime.substring(0, 4));
            int month = Integer.valueOf(iso8601DateTime.substring(5, 7));
            int day = Integer.valueOf(iso8601DateTime.substring(8, 10));

            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
        }

        return calendar;
    }

    // Sets to zero all the time fields (hour, minute, second and millisecond).
    public void clearTimeFields(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public Calendar getNoTimeCopy(Calendar calendar) {
        Calendar copy = null;

        if (calendar != null) {
            copy = Calendar.getInstance();
            copy.setTimeInMillis(calendar.getTimeInMillis());
            clearTimeFields(copy);
        }

        return copy;
    }
}
