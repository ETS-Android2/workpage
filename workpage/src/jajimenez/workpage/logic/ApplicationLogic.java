package jajimenez.workpage.logic;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
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

import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskReminder;

public class ApplicationLogic {
    private static final String PREFERENCES_FILE = "workpage_preferences";
    private static final String CURRENT_TASK_CONTEXT_ID_PREF_KEY = "current_task_context_id";
    private static final String CURRENT_VIEW_PREF_KEY = "current_view";
    private static final String INCLUDE_TASKS_WITH_NO_TAG = "include_tasks_with_no_tag";
    private static final String CURRENT_FILTER_TAGS_PREF_KEY = "current_filter_tags";

    private static final String CSV_TASK_CONTEXT_TO_EXPORT_PREF_KEY = "csv_task_context_to_export";
    private static final String CSV_TASKS_TO_EXPORT_PREF_KEY = "csv_tasks_to_export";
    private static final String CSV_FIELD_NAMES_PREF_KEY = "csv_field_names";
    private static final String CSV_UNIX_TIME_PREF_KEY = "csv_unix_time";
    private static final String CSV_ID_PREF_KEY = "csv_id";
    private static final String CSV_DESCRIPTION_PREF_KEY = "csv_description";
    private static final String CSV_TAGS_PREF_KEY = "csv_tags";

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
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        long currentTaskContextId = preferences.getLong(CURRENT_TASK_CONTEXT_ID_PREF_KEY, 1);

        return dataManager.getTaskContext(currentTaskContextId);
    }

    public String getCurrentView() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getString(CURRENT_VIEW_PREF_KEY, "open");
    }

    public boolean getIncludeTasksWithNoTag() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(INCLUDE_TASKS_WITH_NO_TAG, true);
    }

    public List<TaskTag> getCurrentFilterTags() {
        TaskContext currentContext = getCurrentTaskContext();

        // Default tag names: all.
        List<TaskTag> allTags = getAllTaskTags(currentContext);
        TreeSet<String> allTagNames = new TreeSet<String>();
        for (TaskTag tag : allTags) allTagNames.add(tag.getName());

        // Get current settings.
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        // The Set object returned by "getStringSet" must not be mofified.
        Set<String> prefFilterTagNames = preferences.getStringSet(CURRENT_FILTER_TAGS_PREF_KEY, allTagNames);

        LinkedList<String> filterTagNames = new LinkedList<String>(prefFilterTagNames);
        List<TaskTag> filterTags = dataManager.getTaskTagsByNames(currentContext, filterTagNames);

        return filterTags;
    }

    public long getCsvTaskContextToExport() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getLong(CSV_TASK_CONTEXT_TO_EXPORT_PREF_KEY, 1);
    }

    public int getCsvTasksToExport() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getInt(CSV_TASKS_TO_EXPORT_PREF_KEY, ALL_TASKS);
    }

    public boolean getCsvFieldNames() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(CSV_FIELD_NAMES_PREF_KEY, true);
    }

    public boolean getCsvUnixTime() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(CSV_UNIX_TIME_PREF_KEY, false);
    }

    public boolean getCsvId() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(CSV_ID_PREF_KEY, false);
    }

    public boolean getCsvDescription() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(CSV_DESCRIPTION_PREF_KEY, false);
    }

    public boolean getCsvTags() {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return preferences.getBoolean(CSV_TAGS_PREF_KEY, false);
    }

    public void setCurrentTaskContext(TaskContext context) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CURRENT_TASK_CONTEXT_ID_PREF_KEY, context.getId());
        editor.commit();
    }

    public void setCurrentView(String view) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CURRENT_VIEW_PREF_KEY, view);
        editor.commit();
    }

    public void setIncludeTasksWithNoTag(boolean include) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(INCLUDE_TASKS_WITH_NO_TAG, include);
        editor.commit();
    }

    public void setCurrentFilterTags(List<TaskTag> filterTags) {
        TreeSet<String> filterTagNames = new TreeSet<String>();
        
        if (filterTags != null) {
            for (TaskTag tag : filterTags) filterTagNames.add(tag.getName());
        }

        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(CURRENT_FILTER_TAGS_PREF_KEY, filterTagNames);
        editor.commit();
    }

    public void setCsvTaskContextToExport(long taskContextId) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CSV_TASK_CONTEXT_TO_EXPORT_PREF_KEY, taskContextId);
        editor.commit();
    }

    public void setCsvTasksToExport(int tasksToExport) {
        if (tasksToExport < 0 || tasksToExport > 2) tasksToExport = ALL_TASKS;

        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(CSV_TASKS_TO_EXPORT_PREF_KEY, tasksToExport);
        editor.commit();
    }

    public void setCsvFieldNames(boolean csvFieldNames) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_FIELD_NAMES_PREF_KEY, csvFieldNames);
        editor.commit();
    }

    public void setCsvUnixTime(boolean csvUnixTime) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_UNIX_TIME_PREF_KEY, csvUnixTime);
        editor.commit();
    }

    public void setCsvId(boolean csvId) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_ID_PREF_KEY, csvId);
        editor.commit();
    }

    public void setCsvDescription(boolean csvDescription) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_DESCRIPTION_PREF_KEY, csvDescription);
        editor.commit();
    }

    public void setCsvTags(boolean csvTags) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(CSV_TAGS_PREF_KEY, csvTags);
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
        // Cancel reminder alarms.
        for (TaskContext c : contexts) updateAllOpenTaskReminderAlarms(c, true);

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
        // If the tag is new, it must be added to the tag filtering of the current view.
        TaskContext context = getTaskContext(tag.getContextId());
        List<String> tagNames = new LinkedList<String>();
        tagNames.add(tag.getName());

        List<TaskTag> dbTags = dataManager.getTaskTagsByNames(context, tagNames);

        if (dbTags == null || dbTags.size() == 0) {
            // The tag is new.
            List<TaskTag> filterTags = getCurrentFilterTags();
            filterTags.add(tag);
            setCurrentFilterTags(filterTags);
        }

        dataManager.saveTaskTag(tag);
    }

    public void deleteTaskTags(List<TaskTag> tags) {
        // The tags must be removed from the tag filtering of the current view.
        List<TaskTag> filterTags = getCurrentFilterTags();
        filterTags.removeAll(tags);
        setCurrentFilterTags(filterTags);

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

        // 4. Update tag filtering.
        setCurrentFilterTags(filterTags);

        // 5. Update reminder alarms.
        updateAllReminderAlarms(task, false);
    }

    // Updates all the alarms of all open tasks of all contexts.
    public void updateAllOpenTaskReminderAlarms(boolean tasksDeleted) {
        List<TaskContext> contexts = getAllTaskContexts();

        for (TaskContext c : contexts) updateAllOpenTaskReminderAlarms(c, tasksDeleted);
    }

    // Updates all the alarms of all open tasks of a given context.
    private void updateAllOpenTaskReminderAlarms(TaskContext context, boolean tasksDeleted) {
        boolean includeTasksWithNoTag = true;

        List<TaskTag> tags = getAllTaskTags(context); // All context tags.
        List<Task> tasks = getOpenTasksByTags(context, includeTasksWithNoTag, tags); // All context open tasks.

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
