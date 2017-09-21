package jajimenez.workpage.logic;

import java.util.Comparator;
import java.util.Calendar;

import jajimenez.workpage.data.model.Task;

public class TaskComparator implements Comparator<Task> {
    public int compare(Task a, Task b) {
        int result = -1;

        if (a != null && b != null) {
            DateTimeTool tool = new DateTimeTool();

            long aId = a.getId();
            Calendar aSingle = a.getSingle();
            Calendar aStart = a.getStart();
            Calendar aEnd = a.getEnd();

            long bId = b.getId();
            Calendar bSingle = b.getSingle();
            Calendar bStart = b.getStart();
            Calendar bEnd = b.getEnd();

            // Get the next day in Unix Time format.
            Calendar tomorrow = Calendar.getInstance(); // At this point, "tomorrow" is the current time.
            tool.clearTimeFields(tomorrow);             // Clear all the time fields, setting them to 0.
            tomorrow.add(Calendar.DAY_OF_MONTH, 1);     // Now, "tomorrow" is actually tomorrow's time.

            Calendar aCal = null;
            Calendar bCal = null;

            // See "task_comparator.ods" file in the project documentation.

            // Case 1.
            if (aSingle == null && aStart == null && aEnd == null && bSingle == null && bStart == null && bEnd == null) {
                if (aId == bId) result = 0;
                else if (aId < bId) result = -1;
                else result = 1;
            }
            // Cases 2-5.
            else if ((aSingle != null || aStart != null || aEnd != null) && bSingle == null && bStart == null && bEnd == null) {
                if (aSingle != null) aCal = aSingle;
                else if (aStart != null) aCal = aStart;
                else aCal = aEnd;

                if (aCal.getTimeInMillis() < tomorrow.getTimeInMillis()) result = -1;
                else result = 1;
            }
            // Cases 6, 11, 16 and 21.
            else if (aSingle == null && aStart == null && aEnd == null && (bSingle != null || bStart != null || bEnd != null)) {
                if (bSingle != null) bCal = bSingle;
                else if (bStart != null) bCal = bStart;
                else bCal = bEnd;

                if (bCal.getTimeInMillis() < tomorrow.getTimeInMillis()) result = 1;
                else result = -1;
            }
            // Cases 7-10, 12-15, 17-20 and 22-25.
            else if ((aSingle != null || aStart != null || aEnd != null) && (bSingle != null || bStart != null || bEnd != null)) {
                if (aSingle != null) aCal = aSingle;
                else if (aStart != null) aCal = aStart;
                else aCal = aEnd;

                if (bSingle != null) bCal = bSingle;
                else if (bStart != null) bCal = bStart;
                else bCal = bEnd;

                long aTime = aCal.getTimeInMillis();
                long bTime = bCal.getTimeInMillis();

                if (aTime == bTime) {
                    if (aId == bId) result = 0;
                    else if (aId < bId) result = -1;
                    else result = 1;
                }
                else if (aTime < bTime) {
                    result = -1;
                }
                else {
                    result = 1;
                }
            }
        }

        return result;
    }
}