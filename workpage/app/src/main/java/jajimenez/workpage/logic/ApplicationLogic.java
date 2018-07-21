package jajimenez.workpage.logic;

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
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import jajimenez.workpage.R;
import jajimenez.workpage.TaskReminderAlarmReceiver;
import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.JsonDataTool;
import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;

public class ApplicationLogic {
    public static final String ACTION_DATA_CHANGED = "jajimenez.workpage.action.DATA_CHANGED";

    public static final int NO_DATE = 0;
    public static final int SINGLE_DATE = 1;
    public static final int DATE_RANGE = 2;

    public static final int EXPORT_DATA = 4;
    public static final int IMPORT_DATA = 5;

    public static final String APP_MIME_TYPE = "*/*";

    private static final String CURRENT_TASK_CONTEXT_ID_KEY = "current_task_context_id";

    private static final String VIEW_STATE_FILTER_KEY_START = "view_state_filter_state_context_id_";
    private static final String VIEW_TAG_FILTER_NO_TAG_KEY_START = "view_tag_filter_notag_context_id_";
    private static final String VIEW_TAG_FILTER_KEY_START = "view_tag_filter_tag_id_";

    public static final String EXPORT_DATA_CONTEXT_KEY_START = "export_data_context_id_";
    public static final String EXPORT_DATA_TASK_STATE_KEY_START = "export_data_state_context_id_";
    public static final String EXPORT_DATA_NOTAG_CONTEXT_KEY_START = "export_data_notag_context_id_";
    public static final String EXPORT_DATA_TAG_KEY_START = "export_data_tag_id_";

    public static final String IMPORT_DATA_DATA = "import_data_data";
    public static final String IMPORT_DATA_CONTEXT_KEY_START = "import_data_context_id_";
    public static final String IMPORT_DATA_TASK_STATE_KEY_START = "import_data_state_context_id_";
    public static final String IMPORT_DATA_NOTAG_CONTEXT_KEY_START = "import_data_notag_context_id_";
    public static final String IMPORT_DATA_TAG_KEY_START = "import_data_tag_id_";

    private static final String INTERFACE_MODE_KEY_START = "interface_mode_context_id_";
    private static final String WEEK_START_DAY_KEY = "week_start_day";

    public static final int INTERFACE_MODE_LIST = 0;
    public static final int INTERFACE_MODE_CALENDAR = 1;

    // Constants for the data export and import
    public static final int OPEN_TASKS = 0;
    public static final int CLOSED_TASKS = 1;
    public static final int ALL_TASKS = 2;

    private static final int SINGLE = 0;
    private static final int START = 1;
    private static final int END = 2;

    private Context appContext;
    private DataManager dataManager;
    private boolean notifyDataChanges;

    public ApplicationLogic(Context appContext) {
        this(appContext, true);
    }

    public ApplicationLogic(Context appContext, boolean notifyDataChanges) {
        this.appContext = appContext;
        this.dataManager = new DataManager(appContext);
        this.notifyDataChanges = notifyDataChanges;
    }

    public void setNotifyDataChanges(boolean notifyDataChanges) {
        this.notifyDataChanges = notifyDataChanges;
    }

    public void notifyDataChange() {
        if (notifyDataChanges) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(appContext);
            manager.sendBroadcast(new Intent(ACTION_DATA_CHANGED));
        }
    }

    public TaskContext getCurrentTaskContext() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        long currentTaskContextId = preferences.getLong(CURRENT_TASK_CONTEXT_ID_KEY, 1);

        TaskContext newCurrentContext = dataManager.getTaskContext(currentTaskContextId);

        // In case of any problem, we get the first existing current context.
        if (newCurrentContext == null) {
            // Disable the internal notifications
            boolean not = notifyDataChanges;
            notifyDataChanges = false;

            newCurrentContext = (getAllTaskContexts()).get(0);
            setCurrentTaskContext(newCurrentContext);

            // Restore the original value of "notifyDataChanges".
            notifyDataChanges = not;
        }

        return newCurrentContext;
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
            String key = VIEW_TAG_FILTER_KEY_START + t.getId();
            boolean value = preferences.getBoolean(key, true);

            if (value) filterTags.add(t);
        }

        return filterTags;
    }

    public boolean isContextToExport(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = EXPORT_DATA_CONTEXT_KEY_START + context.getId();
        return preferences.getBoolean(key, true);
    }

    public void setContextToExport(TaskContext context, boolean forExport) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(EXPORT_DATA_CONTEXT_KEY_START + context.getId(), forExport);
        editor.commit();
    }

    public List<TaskContext> getContextsToExport() {
        List<TaskContext> allContexts = getAllTaskContexts();
        List<TaskContext> contextsForExport = new ArrayList<>(allContexts.size());

        for (TaskContext c: allContexts) {
            if (isContextToExport(c)) contextsForExport.add(c);
        }

        return contextsForExport;
    }

    public int getTaskStateToExport(TaskContext context) {
        int state;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = EXPORT_DATA_TASK_STATE_KEY_START + context.getId();
        String value = preferences.getString(key, "all");

        switch (value) {
            case "open":
                state = OPEN_TASKS;
                break;
            case "closed":
                state = CLOSED_TASKS;
                break;
            default:
                state = ALL_TASKS;
        }

        return state;
    }

    public boolean getNoTagToExport(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = EXPORT_DATA_NOTAG_CONTEXT_KEY_START + context.getId();
        return preferences.getBoolean(key, true);
    }

    public List<TaskTag> getTagsToExport(TaskContext context) {
        List<TaskTag> allTags = getAllTaskTags(context);
        List<TaskTag> tags = new ArrayList<>(allTags.size());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        for (TaskTag t: allTags) {
            String key = EXPORT_DATA_TAG_KEY_START + t.getId();
            if (preferences.getBoolean(key, true)) tags.add(t);
        }

        return tags;
    }

    public List<Task> getTasksToExport(TaskContext context) {
        List<Task> tasks = new LinkedList<>();

        int state = getTaskStateToExport(context);
        boolean noTag = getNoTagToExport(context);
        List<TaskTag> tags = getTagsToExport(context);

        if (state == OPEN_TASKS || state == ALL_TASKS) {
            tasks.addAll(getOpenTasksByTags(context, noTag, tags));
        }

        if (state == CLOSED_TASKS || state == ALL_TASKS) {
            tasks.addAll(getClosedTasksByTags(context, noTag, tags));
        }

        return tasks;
    }

    public String getDataToImport() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return preferences.getString(IMPORT_DATA_DATA, "");
    }

    public void setDataToImport(String data) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(IMPORT_DATA_DATA, data);
        editor.commit();
    }

    public boolean isContextToImport(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = IMPORT_DATA_CONTEXT_KEY_START + context.getId();
        return preferences.getBoolean(key, true);
    }

    public void setContextToImport(TaskContext context, boolean forImport) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IMPORT_DATA_CONTEXT_KEY_START + context.getId(), forImport);
        editor.commit();
    }

    public int getTaskStateToImport(TaskContext context) {
        int state;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = IMPORT_DATA_TASK_STATE_KEY_START + context.getId();
        String value = preferences.getString(key, "all");

        switch (value) {
            case "open":
                state = OPEN_TASKS;
                break;
            case "closed":
                state = CLOSED_TASKS;
                break;
            default:
                state = ALL_TASKS;
        }

        return state;
    }

    private boolean getNoTagToImport(TaskContext context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = IMPORT_DATA_NOTAG_CONTEXT_KEY_START + context.getId();
        return preferences.getBoolean(key, true);
    }

    private List<TaskTag> getTagsToImport(TaskContext context) {
        List<TaskTag> allTags = getAllTaskTags(context);
        List<TaskTag> tags = new ArrayList<>(allTags.size());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);

        for (TaskTag t: allTags) {
            String key = IMPORT_DATA_TAG_KEY_START + t.getId();
            if (preferences.getBoolean(key, true)) tags.add(t);
        }

        return tags;
    }

    public List<Task> getTasksToImport(TaskContext context, List<Task> allContextTasks) {
        List<Task> contextTasks = new LinkedList<>();

        int state = getTaskStateToImport(context);
        boolean noTag = getNoTagToImport(context);
        List<TaskTag> contextTags = getTagsToImport(context);

        long contextId = context.getId();

        for (Task t: allContextTasks) {
            t.setContextId(contextId);

            List<TaskTag> taskTags = t.getTags();
            boolean done = t.isDone();

            if (((state == OPEN_TASKS && !done) || (state == CLOSED_TASKS && done) || state == ALL_TASKS) &&
                ((noTag && taskTags.size() == 0) || anyTagInList(taskTags, contextTags))) {

                contextTasks.add(t);
            }
        }

        return contextTasks;
    }

    // Returns True if any element of the list "a" is in the list "b", or False otherwise.
    private boolean anyTagInList(List<TaskTag> a, List<TaskTag> b) {
        boolean contains = false;
        int count = a.size();

        for (int i = 0; i < count && !contains; i++) {
            contains = b.contains(a.get(i));
        }

        return contains;
    }

    public void clearImportPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        Set<String> keys = (preferences.getAll()).keySet();

        for (String k: keys) {
            if ((k.substring(0, 12)).equals("import_data_")) editor.remove(k);
        }

        editor.commit();
    }

    public int getInterfaceMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        String key = INTERFACE_MODE_KEY_START + (getCurrentTaskContext()).getId();

        return preferences.getInt(key, INTERFACE_MODE_LIST);
    }

    public int getWeekStartDay() {
        String defaultDay = (appContext.getResources()).getString(R.string.week_start_default_key);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        return Integer.parseInt(preferences.getString(WEEK_START_DAY_KEY, defaultDay));
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

            String stateViewKey = VIEW_STATE_FILTER_KEY_START + id;
            String noTagViewKey = VIEW_TAG_FILTER_NO_TAG_KEY_START + id;
            String contextExportKey = EXPORT_DATA_CONTEXT_KEY_START + id;
            String taskStateContextKey = EXPORT_DATA_TASK_STATE_KEY_START + id;
            String noTagContextExportKey = EXPORT_DATA_NOTAG_CONTEXT_KEY_START + id;

            editor.remove(stateViewKey);
            editor.remove(noTagViewKey);
            editor.remove(contextExportKey);
            editor.remove(taskStateContextKey);
            editor.remove(noTagContextExportKey);

            List<TaskTag> tags = getAllTaskTags(c);

            for (TaskTag t: tags) {
                long tagId = t.getId();

                String tagViewKey = VIEW_TAG_FILTER_KEY_START + tagId;
                String tagExportKey = EXPORT_DATA_TAG_KEY_START + tagId;

                editor.remove(tagViewKey);
                editor.remove(tagExportKey);
            }

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
            String viewKey = VIEW_TAG_FILTER_KEY_START + t.getId();
            String exportKey = EXPORT_DATA_TAG_KEY_START + t.getId();

            editor.remove(viewKey);
            editor.remove(exportKey);

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

        return year + month + day + "_workpage_data.json";
    }

    // Returns "false" if the operation was successful
    // or "true" if there was any error.
    public boolean exportData(Uri output) {
        boolean error = false;

        if (output != null) {
            try {
                // Input
                String data = (getJsonDataFromDb()).toString();
                InputStream inputStr = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

                // Output
                ParcelFileDescriptor desc = (appContext.getContentResolver()).openFileDescriptor(output, "w");
                OutputStream outputStr = new FileOutputStream(desc.getFileDescriptor());

                copyData(inputStr, outputStr);

                outputStr.close();
                desc.close();
                inputStr.close();
            }
            catch (Exception e) {
                error = true;
            }
        }

        return error;
    }

    private JSONObject getJsonDataFromDb() throws JSONException {
        JsonDataTool tool = new JsonDataTool();
        JSONObject data = new JSONObject();

        // Add contexts
        JSONArray contextArray = new JSONArray();
        List<TaskContext> contexts = getContextsToExport();

        for (TaskContext context : contexts) {
            JSONObject contextObj = tool.getContextJson(context);

            // Add context tags
            List<TaskTag> contextTags = getTagsToExport(context);
            if (contextTags.size() > 0) contextObj.put("tags", tool.getContextTagsJson(contextTags));

            // Add context tasks
            List<Task> contextTasks = getTasksToExport(context);

            // For each of the context's tasks, if any of the tags of the task
            // is not supposed to be exported, we remove the tag from the task.
            for (Task task: contextTasks) {
                List<TaskTag> oldTaskTags = task.getTags();
                List<TaskTag> newTaskTags = new ArrayList<>(oldTaskTags);

                for (TaskTag tag: oldTaskTags) {
                    if (!contextTags.contains(tag)) newTaskTags.remove(tag);
                }

                task.setTags(newTaskTags);
            }

            if (contextTasks.size() > 0) contextObj.put("tasks", tool.getContextTasksJson(contextTasks));

            contextArray.put(contextObj);
        }

        data.put("contexts", contextArray);

        return data;
    }

    public JSONObject loadData(Uri input) throws IOException, JSONException {
        // Input
        InputStream inputStr = (appContext.getContentResolver()).openInputStream(input);

        // Output
        ByteArrayOutputStream outputStr = new ByteArrayOutputStream();

        copyData(inputStr, outputStr);
        String dataStr = outputStr.toString("UTF-8");

        outputStr.close();
        inputStr.close();

        return new JSONObject(dataStr);
    }

    public void importData(JSONObject data) throws JSONException {
        // Cancel reminder alarms of current tasks
        updateAllOpenTaskReminderAlarms(true);

        // Delete current data and import new data
        deleteCurrentData();
        importJsonDataIntoDb(data);

        // Clear settings
        clearSettings();

        // Set reminder alarms for new tasks
        updateAllOpenTaskReminderAlarms(false);

        setNotifyDataChanges(true);
        notifyDataChange();
    }

    public List<Pair<TaskContext, List<TaskTag>>> getContextsFromJson(JSONObject data) {
        List<Pair<TaskContext, List<TaskTag>>> contexts = new LinkedList<>();
        JsonDataTool tool = new JsonDataTool();

        try {
            JSONArray contextArray = data.getJSONArray("contexts");
            int contextCount = contextArray.length();

            int nextTagId = 1;

            for (int i = 0; i < contextCount; i++) {
                JSONObject contextObj = contextArray.getJSONObject(i);

                TaskContext context = tool.getContext(contextObj);
                context.setId(i + 1); // Temporal ID

                List<TaskTag> contextTags;

                if (contextObj.has("tags")) {
                    contextTags = tool.getContextTags(contextObj.getJSONArray("tags"));
                    int contextTagCount = contextTags.size();

                    for (int j = 0; j < contextTagCount; j++) {
                        TaskTag t = contextTags.get(j);

                        t.setId(nextTagId); // Temporal ID
                        t.setContextId(i);

                        nextTagId++;
                    }
                } else {
                    contextTags = new LinkedList<>();
                }

                contexts.add(new Pair<>(context, contextTags));
            }
        } catch (JSONException e) {
            contexts = new LinkedList<>();
        }

        return contexts;
    }

    // "dataObj" is assumed to be a valid object
    private void importJsonDataIntoDb(JSONObject dataObj) throws JSONException {
        JsonDataTool tool = new JsonDataTool();

        JSONArray contextArray = dataObj.getJSONArray("contexts");
        int contextCount = contextArray.length();

        for (int i = 0; i < contextCount; i++) {
            // Context
            JSONObject contextObj = contextArray.getJSONObject(i);
            TaskContext context = tool.getContext(contextObj);

            saveTaskContext(context);

            // After saving the new context, it has an ID.
            long contextId = context.getId();

            // Context tags
            // Note: We need to set the Context ID of a given tag in order to save it.
            if (contextObj.has("tags")) {
                List<TaskTag> contextTags = tool.getContextTags(contextObj.getJSONArray("tags"));

                for (TaskTag tag : contextTags) {
                    tag.setContextId(contextId);
                    saveTaskTag(tag);
                }
            }

            // Context tasks
            // Note: In order to save a task:
            //           1. We need to set its Context ID.
            //           2. We don't need to set neither the IDs nor the Context IDs of its tags, as
            //              soon as the names of its tags are names of existing tags in the context.
            //       See the "saveTask" method of this class.
            //       "getTasksToImport" sets the Context ID of the tasks.
            if (contextObj.has("tasks")) {
                List<Task> allContextTasks = tool.getContextTasks(contextObj.getJSONArray("tasks"));
                List<Task> tasks = getTasksToImport(context, allContextTasks);

                for (Task task: tasks) {
                    saveTask(task);
                }
            }
        }
    }

    private void copyData(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;

        while ((bytesRead = input.read(buffer)) > 0) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void deleteCurrentData() {
        deleteTaskContexts(getAllTaskContexts());
    }

    private void clearSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        SharedPreferences.Editor editor = preferences.edit();

        Map<String, ?> keys = preferences.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            String key = entry.getKey();

            if (!key.equals(WEEK_START_DAY_KEY)) {
                editor.remove(key);
            }
        }

        editor.commit();
    }
}
