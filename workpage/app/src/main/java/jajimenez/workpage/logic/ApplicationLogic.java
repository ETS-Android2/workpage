package jajimenez.workpage.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collections;
import java.util.Calendar;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskReminder;

public class ApplicationLogic {
    public static final String ACTION_DATA_CHANGED = "jajimenez.workpage.action.DATA_CHANGED";

    public static final int NO_DATE = 0;
    public static final int SINGLE_DATE = 1;
    public static final int DATE_RANGE = 2;

    public static final int EXPORT_DATA = 4;
    public static final int IMPORT_DATA = 5;

    public static final String APP_MIME_TYPE = "*/*";

    private static final String CURRENT_TASK_CONTEXT_ID_KEY = "current_task_context_id";
    private static final String VIEW_STATE_FILTER_KEY_START = "view_state_filter_state_context_";
    private static final String VIEW_TAG_FILTER_NO_TAG_KEY_START = "view_tag_filter_notag_context_";
    private static final String INTERFACE_MODE_KEY_START = "interface_mode_context_";
    private static final String WEEK_START_DAY_KEY = "week_start_day";

    public static final int INTERFACE_MODE_LIST = 0;
    public static final int INTERFACE_MODE_CALENDAR = 1;

    // Constants for the "importData" function
    public static final int IMPORT_SUCCESS = 0;
    public static final int IMPORT_ERROR_OPENING_FILE = 1;
    public static final int IMPORT_ERROR_FILE_NOT_COMPATIBLE = 2;
    public static final int IMPORT_ERROR_DATA_NOT_VALID = 3;
    public static final int IMPORT_ERROR_IMPORTING_DATA = 4;

    private static final int SINGLE = 0;
    private static final int START = 1;
    private static final int END = 2;

    private Context appContext;
    private DataManager dataManager;

    public ApplicationLogic(Context appContext) {
        this.appContext = appContext;
        this.dataManager = new DataManager(appContext);
    }

    public void notifyDataChange() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(appContext);
        manager.sendBroadcast(new Intent(ACTION_DATA_CHANGED));
    }

    public TaskContext getCurrentTaskContext() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        long currentTaskContextId = preferences.getLong(CURRENT_TASK_CONTEXT_ID_KEY, 1);

        return dataManager.getTaskContext(currentTaskContextId);
    }

    public String getViewStateFilter() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = VIEW_STATE_FILTER_KEY_START + (getCurrentTaskContext()).getId();

        return preferences.getString(key, "open");
    }

    public boolean getIncludeTasksWithNoTag() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = VIEW_TAG_FILTER_NO_TAG_KEY_START + (getCurrentTaskContext()).getId();

        return preferences.getBoolean(key, true);
    }

    public List<TaskTag> getCurrentFilterTags() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        List<TaskTag> filterTags = new LinkedList<>();
        TaskContext currentContext = getCurrentTaskContext();
        List<TaskTag> tags = getAllTaskTags(currentContext);

        for (TaskTag t : tags) {
            String key = "view_tag_filter_tag_" + t.getId();
            boolean value = preferences.getBoolean(key, true);

            if (value) filterTags.add(t);
        }

        return filterTags;
    }

    public int getInterfaceMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = INTERFACE_MODE_KEY_START + (getCurrentTaskContext()).getId();

        return preferences.getInt(key, INTERFACE_MODE_LIST);
    }

    public int getWeekStartDay() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return Integer.parseInt(preferences.getString(WEEK_START_DAY_KEY, "0"));
    }

    public void setCurrentTaskContext(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CURRENT_TASK_CONTEXT_ID_KEY, context.getId());
        editor.commit();

        notifyDataChange();
    }

    public void setInterfaceMode(int mode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = INTERFACE_MODE_KEY_START + (getCurrentTaskContext()).getId();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, mode);
        editor.commit();

        notifyDataChange();
    }

    public List<TaskContext> getAllTaskContexts() {
        return dataManager.getAllTaskContexts();
    }

    public TaskContext getTaskContext(long id) {
        return dataManager.getTaskContext(id);
    }

    public void saveTaskContext(TaskContext context) {
        dataManager.saveTaskContext(context);
        notifyDataChange();
    }

    public void deleteTaskContexts(List<TaskContext> contexts) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        for (TaskContext c : contexts) {
            // Cancel reminder alarms.
            updateAllOpenTaskReminderAlarms(c, true);

            // The context's settings must be removed from the tag filtering of the view.
            long id = c.getId();

            String stateKey = "view_state_filter_state_context_" + id;
            String noTagKey = "view_tag_filter_notag_context_" + id;

            editor.remove(stateKey);
            editor.remove(noTagKey);

            editor.commit();
        }

        // Delete contexts
        dataManager.deleteTaskContexts(contexts);

        // Notify the application about the changes
        notifyDataChange();
    }

    public List<TaskReminder> getAllTaskReminders() {
        return dataManager.getAllTaskReminders();
    }

    public TaskReminder getTaskReminder(long id) {
        return dataManager.getTaskReminder(id);
    }

    public int getTaskTagCount(TaskContext context) {
        return dataManager.getTaskTagCount(context);
    }

    public List<TaskTag> getAllTaskTags(TaskContext context) {
        return dataManager.getAllTaskTags(context);
    }

    public TaskTag getTaskTag(long id) {
        return dataManager.getTaskTag(id);
    }

    public void saveTaskTag(TaskTag tag) {
        dataManager.saveTaskTag(tag);
        notifyDataChange();
    }

    public void deleteTaskTags(List<TaskTag> tags) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        for (TaskTag t : tags) {
            // The tag settings must be removed from the tag filtering of the view.
            String key = "view_tag_filter_tag_" + t.getId();
            editor.remove(key);
            editor.commit();
        }

        dataManager.deleteTaskTags(tags);
        notifyDataChange();
    }

    public List<Task> getDoableTodayTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        List<Task> tasks = dataManager.getDoableTodayTasksByTags(context, includeTasksWithNoTag, tags);

        // We sort the tasks.
        Collections.sort(tasks, new TaskComparator()); 

        return tasks;
    }

    public List<Task> getOpenTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        List<Task> tasks = dataManager.getTasksByTags(context, false, includeTasksWithNoTag, tags);

        // We sort the tasks.
        Collections.sort(tasks, new TaskComparator());

        return tasks;
    }

    public List<Task> getClosedTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        List<Task> tasks = dataManager.getTasksByTags(context, true, includeTasksWithNoTag, tags);

        // We sort the tasks inversely.
        Collections.sort(tasks, new TaskComparator());
        Collections.reverse(tasks);

        return tasks;
    }

    public List<Task> getDateTasks(List<Task> tasks, Calendar date) {
        List<Task> dateTasks = new LinkedList<>();

        if (tasks != null && date != null) {
            DateTimeTool tool = new DateTimeTool();
            long dateTime = date.getTimeInMillis();

            for (Task t: tasks) {
                if (t != null) {
                    Calendar single = tool.getNoTimeCopy(t.getSingle());
                    Calendar start = tool.getNoTimeCopy(t.getStart());
                    Calendar end = tool.getNoTimeCopy(t.getEnd());

                    if (single != null && single.getTimeInMillis() == dateTime) dateTasks.add(t);

                    if (start != null &&
                            end != null &&
                            start.getTimeInMillis() <= dateTime &&
                            end.getTimeInMillis() >= dateTime) {
                        dateTasks.add(t);
                    } else if (start != null && start.getTimeInMillis() == dateTime) {
                        dateTasks.add(t);
                    } else if (end != null && end.getTimeInMillis() == dateTime) {
                        dateTasks.add(t);
                    }
                }
            }
        }

        return dateTasks;
    }

    public int getTaskCount(boolean done, TaskContext context) {
        return dataManager.getTaskCount(done, context);
    }

    public int getTaskCount(boolean done, TaskTag tag) {
        return dataManager.getTaskCount(done, tag);
    }

    public Task getTask(long id) {
        return dataManager.getTask(id);
    }

    public void saveTask(Task task) {
        // 1. Update reminder fields of the task object.
        if (task.isDone()) {
            task.setSingleReminder(null);
            task.setStartReminder(null);
            task.setEndReminder(null);
        }

        // 2. Before saving the task, we figure out which
        //    tags are new. If any tag is new, it must be
        //    added to the tag filtering of the current view.
        List<TaskTag> filterTags = getCurrentFilterTags();

        TaskContext context = getTaskContext(task.getContextId());
        List<TaskTag> taskTags = task.getTags();
        List<String> tagNames;

        for (TaskTag tag : taskTags) {
            tagNames = new LinkedList<>();
            tagNames.add(tag.getName());

            List<TaskTag> dbTags = dataManager.getTaskTagsByNames(context, tagNames);

            if (dbTags == null || dbTags.size() == 0) {
                // The tag is new.
                filterTags.add(tag);
            }
        }

        // 3. Save the task. The ID attribute of the task
        //    object will be updated with the new value
        //    if the task is a new one.
        dataManager.saveTask(task);

        // 4. Update reminder alarms.
        updateAllReminderAlarms(task, false);

        // 5. Notify the application about the changes.
        notifyDataChange();
    }

    // Updates all the alarms of all open tasks of all contexts.
    public void updateAllOpenTaskReminderAlarms(boolean tasksDeleted) {
        List<TaskContext> contexts = getAllTaskContexts();
        for (TaskContext c : contexts) updateAllOpenTaskReminderAlarms(c, tasksDeleted);
    }

    // Updates all the alarms of all open tasks of a given context.
    private void updateAllOpenTaskReminderAlarms(TaskContext context, boolean tasksDeleted) {
        List<TaskTag> tags = getAllTaskTags(context); // All context tags.
        List<Task> tasks = getOpenTasksByTags(context, true, tags); // All context open tasks.

        for (Task t : tasks) updateAllReminderAlarms(t, tasksDeleted);
    }

    // Updates all the alarms of a given task.
    private void updateAllReminderAlarms(Task task, boolean taskDeleted) {
        updateReminderAlarm(task, SINGLE, taskDeleted);
        updateReminderAlarm(task, START, taskDeleted);
        updateReminderAlarm(task, END, taskDeleted);
    }

    private void updateReminderAlarm(Task task, int reminderType, boolean taskDeleted) {
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        long taskId = task.getId();
        boolean done = task.isDone();

        StringBuilder builder = new StringBuilder();
        builder.append(taskId);
        builder.append(reminderType);
        int reminderId = Integer.parseInt(builder.toString());

        Calendar calendar;
        TaskReminder reminder;

        switch (reminderType) {
            case SINGLE:
                calendar = task.getSingle();
                reminder = task.getSingleReminder();
                break;

            case START:
                calendar = task.getStart();
                reminder = task.getStartReminder();
                break;

            default: // END
                calendar = task.getEnd();
                reminder = task.getEndReminder();
                break;
        }

        Intent intent = new Intent(appContext, TaskReminderAlarmReceiver.class);
        intent.putExtra("task_reminder_id", reminderId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, reminderId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (!done && calendar != null && reminder != null && !taskDeleted) {
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTimeInMillis(calendar.getTimeInMillis());
            reminderTime.add(Calendar.MINUTE, (((int) reminder.getMinutes())*(-1)));

            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), alarmIntent);
        }
        else {
            alarmManager.cancel(alarmIntent);
        }

        if (done || taskDeleted) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(appContext);
            notificationManager.cancel(reminderId);
        }
    }

    public void deleteTasks(List<Task> tasks) {
        // Cancel reminder alarms
        for (Task task : tasks) updateAllReminderAlarms(task, true);

        // Delete tasks
        dataManager.deleteTasks(tasks);

        // Notify the application about the changes
        notifyDataChange();
    }

    public List<Country> getAllCountries() {
        return dataManager.getAllCountries();
    }

    public Country getCountry(long id) {
        return dataManager.getCountry(id);
    }

    public Country getCountry(String timeZoneCode) {
        return dataManager.getCountry(timeZoneCode);
    }

    public List<TimeZone> getTimeZones(Country country) {
        List<String> codes = dataManager.getTimeZoneCodes(country);
        List<TimeZone> timeZones = new ArrayList<>(codes.size());

        for (String c : codes) {
            TimeZone t = TimeZone.getTimeZone(c);

            if (!containsTimeZone(timeZones, t)) timeZones.add(t);
        }

        return timeZones;
    }

    private boolean containsTimeZone(List<TimeZone> list, TimeZone timeZone) {
        boolean found = false;
        int count = list.size();

        TextTool tool = new TextTool();
        Calendar now = Calendar.getInstance();

        for (int i = 0; i < count && !found; i++) {
            TimeZone t = list.get(i);

            String name1 = tool.getTimeZoneName(t, now);
            String name2 = tool.getTimeZoneName(timeZone, now);

            if (name1.equals(name2)) found = true;
        }

        return found;
    }

    public static String getProposedExportDataFileName() {
        Calendar calendar = Calendar.getInstance();

        String year = String.valueOf(calendar.get(Calendar.YEAR));

        String month = String.valueOf(calendar.get(Calendar.MONTH) + 1); // The Month value is zero based
        if (month.length() == 1) month = "0" + month;

        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day.length() == 1) month = "0" + day;

        return year + month + day + "_workpage_data.db";
    }

    // Returns "false" if the operation was successful
    // or "true" if there was any error.
    public boolean exportData(Uri output) {
        boolean error = false;

        if (output != null) {
            try {
                // Input file (database)
                File databaseFile = dataManager.getDatabaseFile();
                InputStream inputStr = new FileInputStream(databaseFile);

                // Output file
                ParcelFileDescriptor desc = (appContext.getContentResolver()).openFileDescriptor(output, "w");
                OutputStream outputStr = new FileOutputStream(desc.getFileDescriptor());

                copyData(inputStr, outputStr);

                inputStr.close();
                outputStr.close();
                desc.close();
            }
            catch (Exception e) {
                error = true;
            }
        }

        return error;
    }

    public int importData(Uri input) {
        int importResult;
        int compatible;

        try {
            // Make temporal copy of the file to import and check if it is compatible
            File tempDbFile = dataManager.getTemporalDatabaseFile(); // This file does not exist yet
            copyFile(input, tempDbFile);
            compatible = DataManager.isDatabaseCompatible(tempDbFile);

            // Delete temporal copy
            tempDbFile.delete();
        }
        catch (Exception e) {
            compatible = DataManager.ERROR_OPENING_DB;
        }

        switch (compatible) {
            case DataManager.COMPATIBLE:
                try {
                    // Cancel reminder alarms of old tasks.
                    updateAllOpenTaskReminderAlarms(true);

                    // Import file
                    File dbFile = dataManager.getDatabaseFile();
                    copyFile(input, dbFile);

                    // Clear settings.
                    clearSettings();

                    // Set reminder alarms for new tasks.
                    updateAllOpenTaskReminderAlarms(false);

                    importResult = IMPORT_SUCCESS;
                }
                catch (Exception e) {
                    importResult = IMPORT_ERROR_IMPORTING_DATA;
                }

                break;

            case DataManager.ERROR_OPENING_DB:
                importResult = IMPORT_ERROR_OPENING_FILE;
                break;

            case DataManager.ERROR_DB_NOT_COMPATIBLE:
                importResult = IMPORT_ERROR_FILE_NOT_COMPATIBLE;
                break;

            default:
                importResult = IMPORT_ERROR_DATA_NOT_VALID;
        }

        notifyDataChange();

        return importResult;
    }

    private void copyFile(Uri input, File output) throws IOException {
        // Input file
        InputStream inputStr = (appContext.getContentResolver()).openInputStream(input);

        // Output file
        OutputStream outputStr = new FileOutputStream(output);

        copyData(inputStr, outputStr);
        inputStr.close();
        outputStr.close();
    }

    private void copyData(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void clearSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        Map<String, ?> keys = preferences.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String key = entry.getKey();
            editor.remove(key);
        }

        editor.commit();
    }
}
