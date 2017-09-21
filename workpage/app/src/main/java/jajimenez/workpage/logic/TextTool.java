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
    public final static int SINGLE = 0;
    public final static int START = 1;
    public final static int END = 2;

    public final static int SHORT = 0;
    public final static int LONG = 1;

    public String getFormattedDate(Calendar date, boolean deviceLocalTimeZone) {
        String formattedDate = "";

        if (date != null) {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
            TimeZone timeZone;

            if (deviceLocalTimeZone) {
                // Device local time zone
                timeZone = (Calendar.getInstance()).getTimeZone();
            } else {
                // Date's time zone
                timeZone = date.getTimeZone();
            }

            dateFormat.setTimeZone(timeZone);
            formattedDate = dateFormat.format(date.getTime());
        }

        return formattedDate;
    }

    public String getFormattedTime(Calendar date, boolean deviceLocalTimeZone) {
        String formattedTime = "";

        if (date != null) {
            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
            TimeZone timeZone;

            if (deviceLocalTimeZone) {
                // Device local time zone
                timeZone = (Calendar.getInstance()).getTimeZone();
            } else {
                // Date's time zone
                timeZone = date.getTimeZone();
            }

            dateFormat.setTimeZone(timeZone);
            formattedTime = dateFormat.format(date.getTime());
        }

        return formattedTime;
    }

    public String getTaskDateText(Context context, Task task, boolean withTitle, int dateType, boolean deviceLocalTimeZone) {
        String text = "";

        String date;
        String time;

        switch (dateType) {
            case SINGLE:
                Calendar single = task.getSingle();

                if (single != null) {
                    // In this case, we ignore "withTitle", as "Single" is always shown without any title.
                    date = getFormattedDate(single, deviceLocalTimeZone);
                    time = getFormattedTime(single, deviceLocalTimeZone);

                    boolean ignoreSingleTime = task.getIgnoreSingleTime();

                    if (!ignoreSingleTime) text = context.getString(R.string.task_datetime, date, time);
                    else text = context.getString(R.string.task_date, date);
                }

                break;
            case START:
                Calendar start = task.getStart();

                if (start != null) {
                    date = getFormattedDate(start, deviceLocalTimeZone);
                    time = getFormattedTime(start, deviceLocalTimeZone);

                    boolean ignoreStartTime = task.getIgnoreStartTime();

                    if (!ignoreStartTime && withTitle) text = context.getString(R.string.task_start_datetime, date, time);
                    else if (!ignoreStartTime && !withTitle) text = context.getString(R.string.task_datetime, date, time);
                    else if (ignoreStartTime && withTitle) text = context.getString(R.string.task_start_date, date);
                    else text = context.getString(R.string.task_date, date);
                }

                break;
            case END:
                Calendar end = task.getEnd();

                if (end != null) {
                    date = getFormattedDate(end, deviceLocalTimeZone);
                    time = getFormattedTime(end, deviceLocalTimeZone);

                    boolean ignoreEndTime = task.getIgnoreEndTime();

                    if (!ignoreEndTime && withTitle) text = context.getString(R.string.task_end_datetime, date, time);
                    else if (!ignoreEndTime && !withTitle) text = context.getString(R.string.task_datetime, date, time);
                    else if (ignoreEndTime && withTitle) text = context.getString(R.string.task_end_date, date);
                    else text = context.getString(R.string.task_date, date);
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
        String formattedDate = getFormattedDate(date, true);
        String formattedTime = getFormattedTime(date, true);

        return context.getString(R.string.time_zone_information, formattedDate, formattedTime);
    }
}