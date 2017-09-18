package jajimenez.workpage.logic;

import java.util.Calendar;
import java.util.List;
import java.text.DateFormat;
import java.util.TimeZone;

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

    public final static int SHORT = 0;
    public final static int LONG = 1;

    public String getFormattedDate(Calendar calendar) {
        String date = "";

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        dateFormat.setTimeZone(calendar.getTimeZone());

        if (calendar != null) date = dateFormat.format(calendar.getTime());

        return date;
    }

    public String getFormattedTime(Calendar calendar) {
        String time = "";

        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
        dateFormat.setTimeZone(calendar.getTimeZone());

        if (calendar != null) time = dateFormat.format(calendar.getTime());

        return time;
    }

    public String getTaskDateText(Context context, Task task, boolean withTitle, int dateType, boolean includeOffset) {
        String text = "";

        String date = null;
        String time = null;
        String offset = null;

        switch (dateType) {
            case WHEN:
                Calendar when = task.getWhen();

                if (when != null) {
                    // In this case, we ignore "withTitle", as "When" is always shown without any title.
                    date = getFormattedDate(when);
                    time = getFormattedTime(when);
                    offset = getFormattedTimeZone(context, when.getTimeZone(), when, TextTool.SHORT);

                    boolean ignoreWhenTime = task.getIgnoreWhenTime();

                    if (!ignoreWhenTime && includeOffset) text = context.getString(R.string.task_datetime_1, date, time, offset);
                    else if (!ignoreWhenTime && !includeOffset) text = context.getString(R.string.task_datetime_2, date, offset);
                    else if (ignoreWhenTime && includeOffset) text = context.getString(R.string.task_date_1, date, offset);
                    else text = context.getString(R.string.task_date_2, date);
                }

                break;
            case START:
                Calendar start = task.getStart();

                if (start != null) {
                    date = getFormattedDate(start);
                    time = getFormattedTime(start);
                    offset = getFormattedTimeZone(context, start.getTimeZone(), start, TextTool.SHORT);

                    boolean ignoreStartTime = task.getIgnoreWhenTime();

                    if (withTitle) {
                        if (!ignoreStartTime && includeOffset) text = context.getString(R.string.task_start_datetime_1, date, time, offset);
                        else if (!ignoreStartTime && !includeOffset) text = context.getString(R.string.task_start_datetime_2, date, time);
                        else if (ignoreStartTime && includeOffset) text = context.getString(R.string.task_start_date_1, date, offset);
                        else text = context.getString(R.string.task_start_date_2, date);
                    } else {
                        if (!ignoreStartTime && includeOffset) text = context.getString(R.string.task_datetime_1, date, time, offset);
                        else if (!ignoreStartTime && !includeOffset) text = context.getString(R.string.task_datetime_2, date, time);
                        else if (ignoreStartTime && includeOffset) text = context.getString(R.string.task_date_1, date, offset);
                        else text = context.getString(R.string.task_date_2, date);
                    }
                }

                break;
            case DEADLINE:
                Calendar deadline = task.getDeadline();

                if (deadline != null) {
                    date = getFormattedDate(deadline);
                    time = getFormattedTime(deadline);
                    offset = getFormattedTimeZone(context, deadline.getTimeZone(), deadline, TextTool.SHORT);

                    boolean ignoreDeadlineTime = task.getIgnoreWhenTime();

                    if (withTitle) {
                        if (!ignoreDeadlineTime && includeOffset) text = context.getString(R.string.task_deadline_datetime_1, date, time, offset);
                        else if (!ignoreDeadlineTime && !includeOffset) text = context.getString(R.string.task_deadline_datetime_2, date, time);
                        else if (ignoreDeadlineTime && includeOffset) text = context.getString(R.string.task_deadline_date_1, date, offset);
                        else text = context.getString(R.string.task_deadline_date_2, date);
                    } else {
                        if (!ignoreDeadlineTime && includeOffset) text = context.getString(R.string.task_datetime_1, date, time, offset);
                        else if (!ignoreDeadlineTime && !includeOffset) text = context.getString(R.string.task_datetime_2, date, time);
                        else if (ignoreDeadlineTime && includeOffset) text = context.getString(R.string.task_date_1, date, offset);
                        else text = context.getString(R.string.task_date_2, date);
                    }
                }
                
                break;
        }

        return text;
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

    public String getTimeZoneName(TimeZone timeZone, Calendar date) {
        boolean daylight = timeZone.inDaylightTime(date.getTime());
        return timeZone.getDisplayName(daylight, TimeZone.LONG);
    }

    public String getFormattedTimeZone(Context context, TimeZone timeZone, Calendar date, int length) {
        String result;

        String timeZoneShortName = timeZone.getDisplayName(timeZone.inDaylightTime(date.getTime()), TimeZone.SHORT);
        String offsetName;

        if (length == SHORT) {
            result = context.getString(R.string.time_zone_short, timeZoneShortName);
        }
        else {
            long offset = timeZone.getOffset(date.getTimeInMillis());
            long totalMilliseconds = Math.abs(offset);

            long millisecondsIn1hour = (1000 * 60 * 60);
            long millisecondsIn1Minute = (1000 * 60);

            long hours = totalMilliseconds / millisecondsIn1hour; // Number of hours
            long rest = totalMilliseconds % millisecondsIn1hour; // The rest, in milliseconds
            long minutes = rest / millisecondsIn1Minute; // The rest, in minutes

            StringBuilder formattedOffsetMinutes = new StringBuilder(String.valueOf(minutes));
            if (formattedOffsetMinutes.length() == 1) formattedOffsetMinutes.insert(0, "0");

            StringBuilder formattedOffsetHours = new StringBuilder(String.valueOf(hours));
            if (formattedOffsetHours.length() == 1) formattedOffsetHours.insert(0, "0");

            if (offset < 0) {
                //result = context.getString(R.string.time_zone_long_1, timeZoneName, formattedOffsetHours, formattedOffsetMinutes);
                offsetName = context.getString(R.string.offset_1, formattedOffsetHours, formattedOffsetMinutes);
            } else {
                //result = context.getString(R.string.time_zone_long_2, timeZoneName, formattedOffsetHours, formattedOffsetMinutes);
                offsetName = context.getString(R.string.offset_2, formattedOffsetHours, formattedOffsetMinutes);
            }

            if (timeZoneShortName.equals(offsetName)) {
                result = context.getString(R.string.time_zone_short, timeZoneShortName);
            } else {
                result = context.getString(R.string.time_zone_long, timeZoneShortName, offsetName);
            }
        }

        return result;
    }

    public String getTimeZoneInformation(Context context, Calendar date) {
        String formattedDate = getFormattedDate(date);
        String formattedTime = getFormattedTime(date);

        return context.getString(R.string.time_zone_information, formattedDate, formattedTime);
    }
}
