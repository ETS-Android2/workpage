package jajimenez.workpage.logic;

import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;

import android.content.Context;
import android.content.res.Resources;

import jajimenez.workpage.R;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class TextTool {
    public final static int WHEN = 0;
    public final static int START = 1;
    public final static int DEADLINE = 2;

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

    public String getFormattedDate(Calendar calendar) {
        String date = "";

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        if (calendar != null) date = dateFormat.format(calendar.getTime());

        return date;
    }

    public String getFormattedTime(Calendar calendar) {
        String time = "";

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        if (calendar != null) time = dateFormat.format(calendar.getTime());

        return time;
    }

    public String getTaskDateText(Context context, Task task, boolean withTitle, int dateType) {
        String text = "";

        String date = null;
        String time = null;

        switch (dateType) {
            case WHEN:
                Calendar when = task.getWhen();

                if (when != null) {
                    // In this case, we ignore "withTitle", as "When" is always shown without any title.
                    date = getFormattedDate(when);

                    if (task.getIgnoreWhenTime()) {
                        text = context.getString(R.string.task_date, date);
                    }
                    else {
                        time = getFormattedTime(when);
                        text = context.getString(R.string.task_datetime, date, time);
                    }
                }

                break;
            case START:
                Calendar start = task.getStart();

                if (start != null) {
                    date = getFormattedDate(start);

                    if (task.getIgnoreStartTime()) {
                        if (withTitle) text = context.getString(R.string.task_start_notime, date);
                        else text = context.getString(R.string.task_date, date);
                    }
                    else {
                        time = getFormattedTime(start);

                        if (withTitle) text = context.getString(R.string.task_start, date, time);
                        else text = context.getString(R.string.task_datetime, date, time);
                    }
                }

                break;
            case DEADLINE:
                Calendar deadline = task.getDeadline();

                if (deadline != null) {
                    date = getFormattedDate(deadline);

                    if (task.getIgnoreDeadlineTime()) {
                        if (withTitle) text = context.getString(R.string.task_deadline_notime, date);
                        else text = context.getString(R.string.task_date, date);
                    }
                    else {
                        time = getFormattedTime(deadline);

                        if (withTitle) text = context.getString(R.string.task_deadline, date, time);
                        else text = context.getString(R.string.task_datetime, date, time);
                    }
                }
                
                break;
        }

        return text;
    }

    // Sets to zero all the time fields (hour, minute, second and millisecond).
    public void clearTimeFields(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public String getTaskReminderText(Context context, TaskReminder reminder) {
        String text = "";

        if (reminder != null) {
            Resources resources = context.getResources();
            long minutes = reminder.getMinutes();

            if (minutes == 0) {
                text = context.getString(R.string.on_time);
            }
            else if (minutes < 60) {
                text = context.getString(R.string.x_minutes, minutes);
            }
            else if (minutes >= 60 && minutes < 1440) {
                long hours = minutes/60;
                text = resources.getQuantityString(R.plurals.x_hours, (int) hours, hours);
            }
            else if (minutes >= 1440 && minutes < 10080) {
                long days = minutes/1440;
                text = resources.getQuantityString(R.plurals.x_days, (int) days, days);
            }
            else { // (minutes >= 10080)
                long weeks = minutes/10080;
                text = resources.getQuantityString(R.plurals.x_weeks, (int) weeks, weeks);
            }
        }

        return text;
    }

    public String getTagsText(Context context, Task task) {
        String text = "";

        List<TaskTag> tags = task.getTags();

        int tagCount = 0;
        if (tags != null) tagCount = tags.size();

        for (int i = 0; i < tagCount; i++) {
            text += (tags.get(i)).getName();
            if (i < (tagCount - 1)) text += context.getString(R.string.separator);
        }

        return text;
    }
}
