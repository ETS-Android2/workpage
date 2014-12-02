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

    public void setCurrentTaskContext(TaskContext context) {
        SharedPreferences preferences = appContext.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CURRENT_TASK_CONTEXT_ID_PREF_KEY, context.getId());
        editor.commit();
    }

    public List<Task> getAllCurrentOpenTasks(TaskContext context) {
        return dataManager.getAllCurrentOpenTasks(context);
    }

    public TaskContext getTaskContext(long id) {
        return dataManager.getTaskContext(id);
    }

    public List<TaskTag> getAllTaskTags(TaskContext context) {
        return dataManager.getAllTaskTags(context);
    }

    public void saveTask(Task task) {
        dataManager.saveTask(task);
    }

    public void deleteTasks(List<Task> tasks) {
        dataManager.deleteTasks(tasks);
    }
}
