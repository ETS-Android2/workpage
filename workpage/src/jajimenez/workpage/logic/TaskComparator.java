package jajimenez.workpage.logic;

import java.util.Comparator;
import java.util.Calendar;

import jajimenez.workpage.data.model.Task;

public class TaskComparator implements Comparator<Task> {
    public int compare(Task a, Task b) {
        long result = -1;

        if (a != null && b != null) {
            long aId = a.getId();
            Calendar aWhen = a.getWhen();
            Calendar aStart = a.getStart();
            Calendar aDeadline = a.getDeadline();

            long bId = b.getId();
            Calendar bWhen = b.getWhen();
            Calendar bStart = b.getStart();
            Calendar bDeadline = b.getDeadline();

            Calendar now = Calendar.getInstance();
            Calendar c1 = null;
            Calendar c2 = null;

            // See "task_comparator.ods" file in the project documentation.

            // Case 1.
            if (aWhen == null && aStart == null && aDeadline == null && bWhen == null && bStart == null && bDeadline == null) {
                result = aId - bId;
            }
            // Cases 2-5.
            else if ((aWhen != null || aStart != null || aDeadline != null) && bWhen == null && bStart == null && bDeadline == null) {
                if (aWhen != null) c1 = aWhen;
                else if (aStart != null) c1 = aStart;
                else c1 = aDeadline;

                result = c1.getTimeInMillis() - (now.getTimeInMillis() + 1);
            }
            // Cases 6, 11, 16 and 21.
            else if (aWhen == null && aStart == null && aDeadline == null && (bWhen != null || bStart != null || bDeadline != null)) {
                if (bWhen != null) c1 = bWhen;
                else if (bStart != null) c1 = bStart;
                else c1 = bDeadline;

                result = (now.getTimeInMillis() + 1) - c1.getTimeInMillis();
            }
            // Cases 7-10, 12-15, 17-20 and 22-25.
            else if ((aWhen != null || aStart != null || aDeadline != null) && (bWhen != null && bStart != null || bDeadline != null)) {
                if (aWhen != null) c1 = aWhen;
                else if (aStart != null) c1 = aStart;
                else c1 = aDeadline;

                if (bWhen != null) c2 = bWhen;
                else if (bStart != null) c2 = bStart;
                else c2 = bDeadline;

                result = c1.getTimeInMillis() - c2.getTimeInMillis();
            }
        }

        return ((int) result);
    }
}
