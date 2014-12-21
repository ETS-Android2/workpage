package jajimenez.workpage.logic;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class ApplicationLogic {
    public static final String PREFERENCES_FILE = "jajimenez_workpage_preferences";
    public static final String CURRENT_TASK_CONTEXT_ID_PREF_KEY = "current_task_context_id";
    public static final String CURRENT_VIEW_PREF_KEY = "current_view";

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
        return preferences.getString(CURRENT_VIEW_PREF_KEY, "now");
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

    public List<Task> getDoableNowTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getDoableNowTasks(context, filterTags);
    }

    public List<Task> getOpenTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getTasks(context, false, filterTags);
    }

    public List<Task> getClosedTasks(TaskContext context, List<TaskTag> filterTags) {
        return dataManager.getTasks(context, true, filterTags);
    }

    public TaskContext getTaskContext(long id) {
        return dataManager.getTaskContext(id);
    }

    public List<TaskTag> getAllTaskTags(TaskContext context) {
        return dataManager.getAllTaskTags(context);
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
