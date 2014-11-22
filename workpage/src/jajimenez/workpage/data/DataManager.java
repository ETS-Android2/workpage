package jajimenez.workpage.data;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jajimenez.workpage.R;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.Task;

public class DataManager extends SQLiteOpenHelper {
    private Context context;

    public static final String DB_NAME = "workpage.db";
    public static final int DB_VERSION = 1;

    public DataManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON;");

        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String taskContextsTableSql = "CREATE TABLE task_contexts (" +
            "id         INTEGER PRIMARY KEY, " +
            "name       TEXT NOT NULL, " +
            "list_order INTEGER NOT NULL DEFAULT 0" +
            ");";

        String initialTaskContextPersonalSql = "INSERT INTO task_contexts(name, list_order) " +
            "VALUES ('" + context.getString(R.string.initial_task_context_personal) + "', 0);";

        String initialTaskContextWorkSql = "INSERT INTO task_contexts(name, list_order) " +
            "VALUES ('" + context.getString(R.string.initial_task_context_work) + "', 1);";

        String taskTagsTableSql = "CREATE TABLE task_tags (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER, " +
            "name            TEXT NOT NULL, " +
            "list_order      INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";

        String tasksTableSql = "CREATE TABLE tasks (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER, " +
            "title           TEXT NOT NULL, " +
            "description     TEXT, " +
            "start_datetime  TEXT, " +
            "end_datetime    TEXT, " +
            "done            INTEGER NOT NULL DEFAULT 0, " +
            "done_datetime   TEXT, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";

        String taskTagsRelationshipsTableSql = "CREATE TABLE task_tag_relationships (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER, " +
            "task_tag_id INTEGER, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";
        
        String subtasksTableSql = "CREATE TABLE subtasks (" +
            "id             INTEGER PRIMARY KEY, " +
            "parent_task_id INTEGER, " +
            "child_task_id  INTEGER, " +
            
            "UNIQUE (parent_task_id, child_task_id), " +
            "FOREIGN KEY (parent_task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (child_task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";

        String taskRequirementsTableSql = "CREATE TABLE task_requirements (" +
            "id               INTEGER PRIMARY KEY, " +
            "task_id          INTEGER, " +
            "required_task_id INTEGER, " +

            "UNIQUE (task_id, required_task_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (required_task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";
            
        db.execSQL(taskContextsTableSql);
        db.execSQL(initialTaskContextPersonalSql);
        db.execSQL(initialTaskContextWorkSql);
        db.execSQL(taskTagsTableSql);
        db.execSQL(tasksTableSql);
        db.execSQL(taskTagsRelationshipsTableSql);
        db.execSQL(subtasksTableSql);
        db.execSQL(taskRequirementsTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String tasksRequirementsTableSql     = "DROP TABLE IF EXISTS task_requirements;";
        String subtasksTableSql              = "DROP TABLE IF EXISTS subtasks;";
        String taskTagsRelationshipsTableSql = "DROP TABLE IF EXISTS task_tag_relationships;";
        String tasksTableSql                 = "DROP TABLE IF EXISTS tasks;";
        String taskTagsTableSql              = "DROP TABLE IF EXISTS task_tags;";
        String taskContextsTableSql          = "DROP TABLE IF EXISTS task_contexts;";
        
        db.execSQL(tasksRequirementsTableSql);
        db.execSQL(subtasksTableSql);
        db.execSQL(taskTagsRelationshipsTableSql);
        db.execSQL(tasksTableSql);
        db.execSQL(taskTagsTableSql);
        db.execSQL(taskContextsTableSql);

        onCreate(db);
    }

    public TaskContext getTaskContext(long taskContextId) {
        TaskContext taskContext = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT name, list_order FROM task_contexts WHERE id = ?;", new String[] { String.valueOf(taskContextId) });

            // New TaskContext object, setting its ID, Name and Order.
            if (cursor.moveToFirst()) taskContext = new TaskContext(taskContextId, cursor.getString(0), cursor.getLong(1));
        } finally {
            db.close();
        }

        return taskContext;
    }

    // Returns all task contexts.
    public List<TaskContext> getAllTaskContexts() {
        List<TaskContext> taskContexts = new LinkedList<TaskContext>(); 
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM task_contexts ORDER BY list_order;", null);

            if (cursor.moveToFirst()) {
                do {
                    // New TaskContext object, setting its ID, Name and Order.
                    taskContexts.add(new TaskContext(cursor.getLong(0), cursor.getString(1), cursor.getLong(2)));
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        return taskContexts;
    }

    // Creates or updates a task context in the database.
    // If the ID of the task context is less than 0, it
    // inserts a new row in the "task_contexts" table
    // ignoring that ID. Otherwise, it updates the row
    // of the given ID.
    public void saveTaskContext(TaskContext taskContext) {
        SQLiteDatabase db = null;
        long id = taskContext.getId();

        ContentValues values = new ContentValues();
        values.put("name", taskContext.getName());
        values.put("list_order", taskContext.getOrder());

        try {
            db = getWritableDatabase();
            
            if (id < 0) db.insert("task_contexts", null, values);
            else db.update("task_contexts", values, "id = ?", new String[] { String.valueOf(id) });
        }
        finally {
            db.close();
        }
    }

    // Returns all open tasks that belong to a given task context and
    // that could be done at the current moment.
    //
    // A current open task means one of the following cases:
    // 1) It's due for the current day.
    // 2) It's due for a range of dates that includes the current day.
    // 3) It doesn't have any date set (that means it could be done
    //    in the current day or as soon as possible).
    // 4) It was due for a day or range of days before the current day
    //    but it's not done yet (it's delayed).
    //
    // Every returned task is incomplete because this method is used
    // to get a list of tasks, without displaying every task's details.
    public List<Task> getAllCurrentOpenTasks(TaskContext taskContext) {
        List<Task> tasks = new LinkedList<Task>();
        long taskContextId = taskContext.getId();
        String currentDay = getIso8601FormattedDate(Calendar.getInstance());
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT tasks.id, tasks.title " +
                "FROM tasks " +
                "WHERE tasks.task_context_id = ? AND " +
                "(tasks.start_datetime IS NULL OR tasks.start_datetime = '' OR tasks.start_datetime <= ?) AND " +
                "tasks.done = 0;", new String[] { String.valueOf(taskContextId), currentDay });

            if (cursor.moveToFirst()) {
                do {
                    // New Task object setting its ID, Task Context ID and Title.
                    tasks.add(new Task(cursor.getLong(0), taskContextId, cursor.getString(1), null, null, null, false, null, null, null, null));
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        return tasks;
    }

    // Creates or updates a task in the database.
    // If the ID of the task is less than 0, it
    // inserts a new row in the "tasks" table
    // ignoring that ID. Otherwise, it updates
    // the row of the given ID.
    public void saveTask(Task task) {
        SQLiteDatabase db = null;
        long id = task.getId();

        ContentValues values = new ContentValues();
        values.put("task_context_id", task.getTaskContextId());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("start_datetime", getIso8601FormattedDate(task.getStartDateTime()));
        values.put("end_datetime", getIso8601FormattedDate(task.getEndDateTime()));

        if (task.isDone()) values.put("done", 1);
        else values.put("done", 0);
            
        values.put("done_datetime", getIso8601FormattedDate(task.getDoneDateTime()));

        try {
            db = getWritableDatabase();
            
            if (id < 0) db.insert("tasks", null, values);
            else db.update("tasks", values, "id = ?", new String[] { String.valueOf(id) });
        }
        finally {
            db.close();
        }
    }

    private String getIso8601FormattedDate(Calendar calendar) {
        String date = null;

        if (calendar != null) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            date = String.format("%d-%02d-%02d 00:00:00.000", year, month, day);
        }

        return date; 
    }
}
