package jajimenez.workpage.logic;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    // Constants for the "importData" function.
    public static final int IMPORT_SUCCESS = 0;
    public static final int IMPORT_ERROR_OPENING_FILE = 1;
    public static final int IMPORT_ERROR_FILE_NOT_COMPATIBLE = 2;
    public static final int IMPORT_ERROR_DATA_NOT_VALID = 3;
    public static final int IMPORT_ERROR_IMPORTING_DATA = 4;

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

    public List<TaskTag> getAllTaskTags(TaskContext context) {
        return dataManager.getAllTaskTags(context);
    }

    public void saveTaskTag(TaskTag tag) {
        dataManager.saveTaskTag(tag);
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
        dataManager.saveTask(task);
    }

    public void markTask(long taskId, boolean done) {
        dataManager.markTask(taskId, done);
    }

    public void deleteTasks(List<Task> tasks) {
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
                    File dbFile = dataManager.getDatabaseFile();
                    copyFile(from, dbFile);
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
