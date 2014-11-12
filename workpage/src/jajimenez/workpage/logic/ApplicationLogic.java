package jajimenez.workpage.logic;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;

public class ApplicationLogic {
    public static final String PREFERENCES_FILE = "jajimenez_workpage_preferences";
    public static final String CURRENT_TASK_CONTEXT_ID_PREF_KEY = "current_task_context_id";

    private Context context;
    private DataManager dataManager;

    public ApplicationLogic(Context context) {
        this.context = context;
        this.dataManager = new DataManager(context);
    }

    public List<TaskContext> getAllTaskContexts() {
        return dataManager.getAllTaskContexts();
    }

    public TaskContext getCurrentTaskContext() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        long currentTaskContextId = preferences.getLong(CURRENT_TASK_CONTEXT_ID_PREF_KEY, 1);

        return dataManager.getTaskContext(currentTaskContextId);
    }

    public void setCurrentTaskContext(TaskContext taskContext) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(CURRENT_TASK_CONTEXT_ID_PREF_KEY, taskContext.getId());
        editor.commit();
    }
}
