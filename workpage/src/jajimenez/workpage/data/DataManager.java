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
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
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
            "id                INTEGER PRIMARY KEY, " +
            "task_context_id   INTEGER, " +
            "title             TEXT NOT NULL, " +
            "description       TEXT, " +
            "start_datetime    TEXT, " +
            "deadline_datetime TEXT, " +
            "done              INTEGER NOT NULL DEFAULT 0, " +
            "done_datetime     TEXT, " +

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

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Returns all task contexts.
    public List<TaskContext> getAllTaskContexts() {
        List<TaskContext> contexts = new LinkedList<TaskContext>(); 
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM task_contexts ORDER BY list_order;", null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    long order = cursor.getLong(2);

                    contexts.add(new TaskContext(id, name, order));
                } while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return contexts;
    }

    public TaskContext getTaskContext(long id) {
        TaskContext context = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT name, list_order FROM task_contexts WHERE id = ?;", new String[] { String.valueOf(id) });

            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                long order = cursor.getLong(1);
                context = new TaskContext(id, name, order);
            }
        }
        finally {
            db.close();
        }

        return context;
    }

    // Creates or updates a task context in the database.
    // If the ID of the task context is less than 0, it
    // inserts a new row in the "task_contexts" table
    // ignoring that ID and updates the ID attribute of
    // the TaskContext given object. Otherwise, it updates the row
    // of the given ID.
    public void saveTaskContext(TaskContext context) {
        SQLiteDatabase db = null;
        long id = context.getId();

        ContentValues values = new ContentValues();
        values.put("name", context.getName());
        values.put("list_order", context.getOrder());

        try {
            db = getWritableDatabase();
            
            if (id < 0) {
                long newId = db.insert("task_contexts", null, values);
                context.setId(newId);
            }
            else {
                db.update("task_contexts", values, "id = ?", new String[] { String.valueOf(id) });
            }
        }
        finally {
            db.close();
        }
    }

    // Returns all the task tags that belong to a given task context.
    public List<TaskTag> getAllTaskTags(TaskContext context) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        long contextId = context.getId();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM task_tags ORDER BY list_order;", null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    long order = cursor.getLong(2);

                    tags.add(new TaskTag(id, contextId, name, order));
                } while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return tags;
    }

    // Creates or updates a task tag in the database.
    // If the ID of the task tag is less than 0, it
    // inserts a new row in the "task_tags" table
    // ignoring that ID and updates the ID attribute
    // of the given TaskTag object. Otherwise, it updates
    // the row of the given ID.
    public void saveTaskTag(TaskTag tag) {
        SQLiteDatabase db = null;
        long id = tag.getId();

        ContentValues values = new ContentValues();
        values.put("task_context_id", tag.getContextId());
        values.put("name", tag.getName());
        values.put("list_order", tag.getOrder());

        try {
            db = getWritableDatabase();
            
            if (id < 0) {
                long newId = db.insert("task_tags", null, values);
                tag.setId(newId);
            }
            else {
                db.update("task_tags", values, "id = ?", new String[] { String.valueOf(id) });
            }
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
    public List<Task> getAllCurrentOpenTasks(TaskContext context) {
        List<Task> tasks = new LinkedList<Task>();
        long contextId = context.getId();

        DateTimeTool tool = new DateTimeTool();
        String currentDay = tool.getIso8601DateTime(Calendar.getInstance());

        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, title, start_datetime, deadline_datetime " +
                "FROM tasks WHERE task_context_id = ? AND " +
                "(start_datetime IS NULL OR start_datetime = '' OR start_datetime <= ?) AND " +
                "tasks.done = 0 " +
                "ORDER BY deadline_datetime;", new String[] { String.valueOf(contextId), currentDay });

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    Calendar start = tool.getCalendar(cursor.getString(2));
                    Calendar deadline = tool.getCalendar(cursor.getString(3));
                    List<Long> tags = new LinkedList<Long>();
                    List<Long> subtasks = new LinkedList<Long>();
                    List<Long> requiredTasks = new LinkedList<Long>();

                    tasks.add(new Task(id, contextId, title, null, start, deadline, false, null, tags, subtasks, requiredTasks));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return tasks;
    }

    // Creates or updates a task in the database.
    // If the ID of the task is less than 0, it
    // inserts a new row in the "tasks" table
    // ignoring that ID and updates the ID attribute
    // of the given Task object. Otherwise, it updates
    // the row of the given ID.
    public void saveTask(Task task) {
        SQLiteDatabase db = null;
        long id = task.getId();

        DateTimeTool tool = new DateTimeTool();

        ContentValues values = new ContentValues();
        values.put("task_context_id", task.getContextId());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());
        values.put("start_datetime", tool.getIso8601DateTime(task.getStart()));
        values.put("deadline_datetime", tool.getIso8601DateTime(task.getDeadline()));

        if (task.isDone()) values.put("done", 1);
        else values.put("done", 0);
            
        values.put("done_datetime", tool.getIso8601DateTime(task.getDoneTime()));

        try {
            db = getWritableDatabase();
            
            if (id < 0) {
                long newId = db.insert("tasks", null, values);
                task.setId(newId);
            }
            else {
                db.update("tasks", values, "id = ?", new String[] { String.valueOf(id) });
            }
        }
        finally {
            db.close();
        }
    }

    public void deleteTasks(List<Task> tasks) {
        SQLiteDatabase db = null;
        
        try {
            db = getWritableDatabase();
            for (Task t : tasks) db.delete("tasks", "id = ?", new String[] { String.valueOf(t.getId()) });
        }
        finally {
            db.close();
        }
    }
}