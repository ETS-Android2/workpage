package jajimenez.workpage.logic;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
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
    public static final String PREFERENCES_FILE = "workpage_preferences";
    public static final String CURRENT_TASK_CONTEXT_ID_PREF_KEY = "current_task_context_id";
    public static final String CURRENT_VIEW_PREF_KEY = "current_view";
    public static final String INCLUDE_TASKS_WITH_NO_TAG = "include_tasks_with_no_tag";
    public static final String CURRENT_FILTER_TAGS_PREF_KEY = "current_filter_tags";

    private Context appContext;
    private DataManager dataManager;

    // Constants for the "importData" function.
    public static final int IMPORT_SUCCESS = 0;
    public static final int IMPORT_ERROR_OPENING_FILE = 1;
    public static final int IMPORT_ERROR_FILE_NOT_COMPATIBLE = 2;
    public static final int IMPORT_ERROR_DATA_NOT_VALID = 3;
    public static final int IMPORT_ERROR_IMPORTING_DATA = 4;

    private static final int WHEN = 0;
    private static final int START = 1;
    private static final int DEADLINE = 2;

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
        return dataManager.getDoableTodayTasksByTags(context, includeTasksWithNoTag, tags);
    }

    public List<Task> getOpenTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        return dataManager.getTasksByTags(context, false, includeTasksWithNoTag, tags);
    }

    public List<Task> getClosedTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        return dataManager.getTasksByTags(context, true, includeTasksWithNoTag, tags);
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
        if (task.isDone()) {
            task.setWhenReminder(null);
            task.setStartReminder(null);
            task.setDeadlineReminder(null);
        }

        // If any tag is new, it must be added to the tag filtering of the current view.
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

        setCurrentFilterTags(filterTags);

        // Reminders.
        long taskId = task.getId();

        if (taskId < 1) {
            // It is a new task.
            updateAllReminderAlarms(task, false);
        }
        else {
            // The task already exists.

            // We get the current task on the database.
            Task oldTask = getTask(taskId);

            // We update each reminder alarm only if there is any
            // change on is date/time or or in the reminder.
            if (reminderDifference(oldTask, task, WHEN)) updateReminderAlarm(task, WHEN, false);
            if (reminderDifference(oldTask, task, START)) updateReminderAlarm(task, START, false);
            if (reminderDifference(oldTask, task, DEADLINE)) updateReminderAlarm(task, DEADLINE, false);
        }

        dataManager.saveTask(task);
    }

    // Returns "true" if there is some difference in a reminder
    // for 2 given tasks (with the same ID) and a given reminder
    // type.
    //
    // Note: If the value of "reminderType" is not valid,
    //       it always returns "false".
    private boolean reminderDifference(Task oldTask, Task newTask, int reminderType) {
        Calendar oldCalendar = null;
        TaskReminder oldReminder = null;

        Calendar newCalendar = null;
        TaskReminder newReminder = null;

        switch (reminderType) {
            case WHEN:
                oldCalendar = oldTask.getWhen();
                oldReminder = oldTask.getWhenReminder();
                
                newCalendar = newTask.getWhen();
                newReminder = newTask.getWhenReminder();

                break;
            case START:
                oldCalendar = oldTask.getStart();
                oldReminder = oldTask.getStartReminder();
                
                newCalendar = newTask.getStart();
                newReminder = newTask.getStartReminder();

                break;
            case DEADLINE:
                oldCalendar = oldTask.getDeadline();
                oldReminder = oldTask.getDeadlineReminder();
                
                newCalendar = newTask.getDeadline();
                newReminder = newTask.getDeadlineReminder();

                break;
        }

        boolean calendarChange = ((oldCalendar == null && newCalendar != null)
            || (oldCalendar != null && newCalendar == null)
            || (oldCalendar != null && newCalendar != null && oldCalendar.getTimeInMillis() != newCalendar.getTimeInMillis()));
        
        boolean reminderChange = ((oldReminder == null && newReminder != null)
            || (oldReminder != null && newReminder == null)
            || (oldReminder != null && newReminder != null && oldReminder.getId() != newReminder.getId()));

        return (calendarChange || reminderChange);
    }

    // Updates all the alarms of a given task.
    public void updateAllReminderAlarms(Task task, boolean removeAllAlarms) {
        updateReminderAlarm(task, WHEN, removeAllAlarms);
        updateReminderAlarm(task, START, removeAllAlarms);
        updateReminderAlarm(task, DEADLINE, removeAllAlarms);
    }

    // Updates all the alarms of all open tasks.
    public void updateAllOpenTaskReminderAlarms(boolean removeAllAlarms) {
        List<TaskContext> contexts = getAllTaskContexts();

        boolean includeTasksWithNoTag = true;
        List<TaskTag> tags = null;
        List<Task> tasks = null;

        for (TaskContext c : contexts) {
            tags = getAllTaskTags(c); // All context tags.
            tasks = getOpenTasksByTags(c, includeTasksWithNoTag, tags); // All context open tasks.

            for (Task t : tasks) updateAllReminderAlarms(t, removeAllAlarms);
        }
    }

    private void updateReminderAlarm(Task task, int reminderType, boolean removeAlarm) {
        AlarmManager alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
        NotificationManager notificationManager = (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

        long taskId = task.getId();
        boolean done = task.isDone();

        StringBuilder builder = new StringBuilder();
        builder.append(taskId);
        builder.append(reminderType);
        int reminderId = Integer.parseInt(builder.toString());

        Intent intent = new Intent(appContext, TaskReminderAlarmReceiver.class);
        intent.putExtra("reminder_id", reminderId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(appContext, reminderId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

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

        if (!done && calendar != null && reminder != null && !removeAlarm) {
            Calendar reminderTime = Calendar.getInstance();
            reminderTime.setTimeInMillis(calendar.getTimeInMillis());
            reminderTime.add(Calendar.MINUTE, (((int) reminder.getMinutes())*(-1)));

            alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(), alarmIntent);
        }
        else {
            alarmManager.cancel(alarmIntent);

            // Remove any existing notification (notification ID is the same as the task ID).
            notificationManager.cancel(reminderId);
        }
    }

    public void deleteTasks(List<Task> tasks) {
        // Remove all reminder alarms from all input tasks.
        for (Task task : tasks) updateAllReminderAlarms(task, true);

        // Delete all input tasks.
        dataManager.deleteTasks(tasks);
    }
    
    // Returns "false" if the operation was successful
    // or "true" if there was any error.
    public boolean exportData(File to) {
        boolean error = false;

        if (to != null) {
            try {
                File databaseFile = dataManager.getDatabaseFile();
                copyFile(databaseFile, to);
            }
            catch (Exception e) {
                error = true;
            }
        }

        return error;
    }

    public int importData(File from) {
        int importResult;
        int compatible = DataManager.isDatabaseCompatible(from);

        switch (compatible) {
            case DataManager.COMPATIBLE:
                try {
                    updateAllOpenTaskReminderAlarms(true);
                    File dbFile = dataManager.getDatabaseFile();
                    copyFile(from, dbFile);
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
