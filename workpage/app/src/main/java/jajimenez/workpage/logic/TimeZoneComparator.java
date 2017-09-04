package jajimenez.workpage.logic;

import java.util.Calendar;
import java.util.Comparator;
import java.util.TimeZone;

public class TimeZoneComparator implements Comparator<TimeZone> {
    public int compare(TimeZone a, TimeZone b) {
        int result = -1;

        if (a != null && b != null) {
            Calendar now = Calendar.getInstance();
            long nowTime = now.getTimeInMillis();

            result = a.getOffset(nowTime) - b.getOffset(nowTime);
        }

        return result;
    }
}