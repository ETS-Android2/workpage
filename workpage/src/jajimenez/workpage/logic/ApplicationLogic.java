package jajimenez.workpage.logic;

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

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskReminder;

public class ApplicationLogic {
    private static final String CURRENT_TASK_CONTEXT_ID_KEY = "current_task_context_id";
    private static final String VIEW_STATE_FILTER_KEY_START = "view_state_filter_state_context_";
    private static final String VIEW_TAG_FILTER_NO_TAG_KEY_START = "view_tag_filter_notag_context_";

    private static final String CSV_TASK_CONTEXT_TO_EXPORT_KEY = "csv_task_context_to_export";
    private static final String CSV_TASKS_TO_EXPORT_KEY = "csv_tasks_to_export";
    private static final String CSV_PROPERTIES_FIELD_NAMES_KEY = "csv_properties_field_names";
    private static final String CSV_PROPERTIES_UNIX_TIME_KEY = "csv_properties_unix_time";
    private static final String CSV_PROPERTIES_ID_KEY = "csv_properties_id";
    private static final String CSV_PROPERTIES_DESCRIPTION_KEY = "csv_properties_description";
    private static final String CSV_PROPERTIES_TAGS_KEY = "csv_properties_tags";

    public static final int ONLY_OPEN_TASKS = 0;
    public static final int ONLY_CLOSED_TASKS = 1;
    public static final int ALL_TASKS = 2;

    // Constants for the "importData" function.
    public static final int IMPORT_SUCCESS = 0;
    public static final int IMPORT_ERROR_OPENING_FILE = 1;
    public static final int IMPORT_ERROR_FILE_NOT_COMPATIBLE = 2;
    public static final int IMPORT_ERROR_DATA_NOT_VALID = 3;
    public static final int IMPORT_ERROR_IMPORTING_DATA = 4;

    private static final int WHEN = 0;
    private static final int START = 1;
    private static final int DEADLINE = 2;

    public static final int WORKPAGE_DATA = 0;
    public static final int CSV = 1;

    private Context appContext;
    private DataManager dataManager;

    public ApplicationLogic(Context appContext) {
        this.appContext = appContext;
        this.dataManager = new DataManager(appContext);
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

        List<TaskTag> filterTags = new LinkedList<TaskTag>();
        TaskContext currentContext = getCurrentTaskContext();
        List<TaskTag> tags = getAllTaskTags(currentContext);

        for (TaskTag t : tags) {
            String key = "view_tag_filter_tag_" + t.getId();
            boolean value = preferences.getBoolean(key, true);

            if (value) filterTags.add(t);
        }

        return filterTags;
    }

    public long getCsvTaskContextToExport() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getLong(CSV_TASK_CONTEXT_TO_EXPORT_KEY, 1);
    }

    public int getCsvTasksToExport() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getInt(CSV_TASKS_TO_EXPORT_KEY, ALL_TASKS);
    }

    public boolean getCsvFieldNames() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(CSV_PROPERTIES_FIELD_NAMES_KEY, true);
    }

    public boolean getCsvUnixTime() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(CSV_PROPERTIES_UNIX_TIME_KEY, false);
    }

    public boolean getCsvId() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(CSV_PROPERTIES_ID_KEY, false);
    }

    public boolean getCsvDescription() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(CSV_PROPERTIES_DESCRIPTION_KEY, false);
    }

    public boolean getCsvTags() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getBoolean(CSV_PROPERTIES_TAGS_KEY, false);
    }

    public void setCurrentTaskContext(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CURRENT_TASK_CONTEXT_ID_KEY, context.getId());
        editor.commit();
    }

    public void setCsvTaskContextToExport(long taskContextId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CSV_TASK_CONTEXT_TO_EXPORT_KEY, taskContextId);
        editor.commit();
    }

    public void setCsvTasksToExport(int tasksToExport) {
        if (tasksToExport < 0 || tasksToExport > 2) tasksToExport = ALL_TASKS;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CSV_TASKS_TO_EXPORT_KEY, tasksToExport);
        editor.commit();
    }

    public void setCsvFieldNames(boolean csvFieldNames) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_PROPERTIES_FIELD_NAMES_KEY, csvFieldNames);
        editor.commit();
    }

    public void setCsvUnixTime(boolean csvUnixTime) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_PROPERTIES_UNIX_TIME_KEY, csvUnixTime);
        editor.commit();
    }

    public void setCsvId(boolean csvId) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_PROPERTIES_ID_KEY, csvId);
        editor.commit();
    }

    public void setCsvDescription(boolean csvDescription) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_PROPERTIES_DESCRIPTION_KEY, csvDescription);
        editor.commit();
    }

    public void setCsvTags(boolean csvTags) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_PROPERTIES_TAGS_KEY, csvTags);
        editor.commit();
    }

    public List<TaskContext> getAllTaskContexts() {
        return dataManager.getAllTaskContexts();
    }

    public TaskContext getTaskContext(long id) {
        return dataManager.getTaskContext(id);
    }

    public void saveTaskContext(TaskContext context) {
        dataManager.saveTaskContext(context);
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

        // Delete contexts.
        dataManager.deleteTaskContexts(contexts);
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

    public void saveTaskTag(TaskTag tag) {
        dataManager.saveTaskTag(tag);
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
            task.setWhenReminder(null);
            task.setStartReminder(null);
            task.setDeadlineReminder(null);
        }

        // 2. Before saving the task, we figure out which
        //    tags are new. If any tag is new, it must be
        //    added to the tag filtering of the current view.
        List<TaskTag> filterTags = getCurrentFilterTags();

        TaskContext context = getTaskContext(task.getContextId());
        List<TaskTag> taskTags = task.getTags();
        List<String> tagNames = null;

        for (TaskTag tag : taskTags) {
            tagNames = new LinkedList<String>();
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
        updateReminderAlarm(task, WHEN, taskDeleted);
        updateReminderAlarm(task, START, taskDeleted);
        updateReminderAlarm(task, DEADLINE, taskDeleted);
    }

    private void updateReminderAlarm(Task task, int reminderType, boolean taskDeleted) {
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);

        long taskId = task.getId();
        boolean done = task.isDone();

        StringBuilder builder = new StringBuilder();
        builder.append(taskId);
        builder.append(reminderType);
        int reminderId = Integer.parseInt(builder.toString());

        Calendar calendar = null;
        TaskReminder reminder = null;

        switch (reminderType) {
            case WHEN:
                calendar = task.getWhen();
                reminder = task.getWhenReminder();
                break;

            case START:
                calendar = task.getStart();
                reminder = task.getStartReminder();
                break;

            default: // DEADLINE
                calendar = task.getDeadline();
                reminder = task.getDeadlineReminder();
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
            NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(reminderId);
        }
    }

    public void deleteTasks(List<Task> tasks) {
        // Cancel reminder alarms.
        for (Task task : tasks) updateAllReminderAlarms(task, true);

        // Delete tasks.
        dataManager.deleteTasks(tasks);
    }
    
    // Returns "false" if the operation was successful
    // or "true" if there was any error.
    public boolean exportData(File to, int format) {
        boolean error = false;

        if (to != null) {
            try {
                File databaseFile = dataManager.getDatabaseFile();

                switch (format) {
                    case WORKPAGE_DATA:
                        copyFile(databaseFile, to);
                        break;
                    case CSV:
                        TaskContext contextToExport = getTaskContext(getCsvTaskContextToExport());
                        int tasksToExport = getCsvTasksToExport();
                        boolean fieldNames = getCsvFieldNames();
                        boolean unixTime = getCsvUnixTime();
                        boolean id = getCsvId();
                        boolean description = getCsvDescription();
                        boolean tags = getCsvTags();

                        List<Task> tasks = null;
                        List<TaskTag> contextTags = getAllTaskTags(contextToExport);

                        if (tasksToExport == ONLY_OPEN_TASKS) {
                            tasks = getOpenTasksByTags(contextToExport, true, contextTags);
                        }
                        else if (tasksToExport == ONLY_CLOSED_TASKS) {
                            tasks = getClosedTasksByTags(contextToExport, true, contextTags);
                        }
                        else { // ALL_TASKS
                            tasks = getOpenTasksByTags(contextToExport, true, contextTags);
                            tasks.addAll(getClosedTasksByTags(contextToExport, true, contextTags));

                            // We sort the tasks.
                            Collections.sort(tasks, new TaskComparator()); 
                        }

                        CsvExporter exporter = new CsvExporter(appContext);
                        exporter.export(contextToExport, tasks, fieldNames, unixTime, id, description, tags, to);

                        break;
                }
            }
            catch (Exception e) {
                error = true;
            }
        }

        return error;
    }

    // Format is always Workpage Data.
    public int importData(File from) {
        int importResult;
        int compatible = DataManager.isDatabaseCompatible(from);

        switch (compatible) {
            case DataManager.COMPATIBLE:
                try {
                    // Cancel reminder alarms of old tasks.
                    updateAllOpenTaskReminderAlarms(true);

                    File dbFile = dataManager.getDatabaseFile();
                    copyFile(from, dbFile);

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


        return importResult;
    }

    public void clearSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        Map<String, ?> keys = preferences.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String key = entry.getKey();

            if (!key.equals("reminder_type")
                && !key.equals("notifications_sound")
                && !key.equals("notifications_vibrate")
                && !key.equals("notifications_light")) {

                editor.remove(key);
            }
        }

        editor.commit();
    }

    private void copyFile(File from, File to) throws IOException {
        InputStream in = new FileInputStream(from);
        OutputStream out = new FileOutputStream(to);

        byte[] buffer = new byte[1024];
        int bytesRead = 0;

        while ((bytesRead = in.read(buffer)) > 0) out.write(buffer, 0, bytesRead);

        in.close();
        out.close();
    }
}
