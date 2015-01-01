package jajimenez.workpage.logic;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.SharedPreferences;

import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class ApplicationLogic {
    public static final String PREFERENCES_FILE = "workpage_preferences";
    public static final String CURRENT_TASK_CONTEXT_ID_PREF_KEY = "current_task_context_id";
    public static final String CURRENT_VIEW_PREF_KEY = "current_view";
    public static final String CURRENT_FILTER_TAGS_PREF_KEY = "current_filter_tags";

    private Context appContext;
    private DataManager dataManager;

    public ApplicationLogic(Context appContext) {
        this.appContext = appContext;
        this.dataManager = new DataManager(appContext);
    }

    public List<TaskContext> getAllTaskContexts() {
        return dataManager.getAllTaskContexts();
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

    // Returning Null or an empty list means that all tasks from the current view must be shown.
    // Otherwise, only the tasks from the current view that have any of the given task tags must be shown.
    public List<TaskTag> getCurrentFilterTags() {
        TaskContext currentContext = getCurrentTaskContext();

        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        // The Set object returned by "getStringSet" must not be mofified.
        Set<String> prefFilterTagNames = preferences.getStringSet(CURRENT_FILTER_TAGS_PREF_KEY, new TreeSet<String>());

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

    // Setting "filterTags" to Null or to an empty list means that all tasks from the current view must be shown.
    // Otherwise, only the tasks from the current view that have any of the given task tags must be shown.
    public void setCurrentFilterTags(List<TaskTag> filterTags) {
        LinkedList<String> filterTagNames = new LinkedList<String>();
        
        if (filterTags != null) {
            for (TaskTag tag : filterTags) filterTagNames.add(tag.getName());
        }

        TreeSet<String> prefFilterTagNames = new TreeSet<String>(filterTagNames);

        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putStringSet(CURRENT_FILTER_TAGS_PREF_KEY, prefFilterTagNames);
        editor.commit();
    }

    public TaskContext getTaskContext(long id) {
        return dataManager.getTaskContext(id);
    }

    public List<TaskTag> getAllTaskTags(TaskContext context) {
        return dataManager.getAllTaskTags(context);
    }

    public void deleteTaskTags(List<TaskTag> tags) {
        dataManager.deleteTaskTags(tags);
    }

    public List<Task> getDoableTodayTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getDoableTodayTasks(context, filterTags);
    }

    public List<Task> getOpenTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getTasks(context, false, filterTags);
    }

    public List<Task> getClosedTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getTasks(context, true, filterTags);
    }

    public long getTaskCount(boolean done, TaskTag tag) {
        return dataManager.getTaskCount(done, tag);
    }

    public Task getTask(long id) {
        return dataManager.getTask(id);
    }

    public void saveTask(Task task) {
        dataManager.saveTask(task);
    }

    public void deleteTasks(List<Task> tasks) {
        dataManager.deleteTasks(tasks);
    }
}
