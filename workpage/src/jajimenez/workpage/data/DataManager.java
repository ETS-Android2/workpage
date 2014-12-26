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
    public static final String DB_NAME = "workpage.db";
    public static final int DB_VERSION = 1;

    private Context context;

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
        
        db.execSQL(taskContextsTableSql);
        db.execSQL(initialTaskContextPersonalSql);
        db.execSQL(initialTaskContextWorkSql);
        db.execSQL(taskTagsTableSql);
        db.execSQL(tasksTableSql);
        db.execSQL(taskTagsRelationshipsTableSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String taskTagsRelationshipsTableSql = "DROP TABLE IF EXISTS task_tag_relationships;";
        String tasksTableSql                 = "DROP TABLE IF EXISTS tasks;";
        String taskTagsTableSql              = "DROP TABLE IF EXISTS task_tags;";
        String taskContextsTableSql          = "DROP TABLE IF EXISTS task_contexts;";
        
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
                }
                while (cursor.moveToNext());
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
            Cursor cursor = db.rawQuery("SELECT name, list_order FROM task_contexts WHERE id = ?;",
                new String[] { String.valueOf(id) });

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

    // Returns all the task tags that belong to a task context.
    public List<TaskTag> getAllTaskTags(TaskContext context) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        long contextId = context.getId();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, list_order FROM task_tags " +
                "WHERE task_context_id = ? " +
                "ORDER BY list_order;", new String[] { String.valueOf(contextId) });

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    long order = cursor.getLong(2);

                    tags.add(new TaskTag(id, contextId, name, order));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return tags;
    }

    // Returns all the task tags, given its names, that belong to a task context.
    public List<TaskTag> getTaskTagsByNames(TaskContext context, List<String> tagNames) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        int tagCount = 0;
        if (tagNames != null) tagCount = tagNames.size();

        if (tagCount > 0) {
            long contextId = context.getId();
            SQLiteDatabase db = null;

            try {
                db = getReadableDatabase();
                String query = "SELECT id, name, list_order FROM task_tags " +
                    "WHERE task_context_id = ? " +
                    "AND (";

                for (int i = 0; i < tagCount; i++) {
                    String name = tagNames.get(i);
                    query += String.format("name = '%s' ", name);

                    if (i < (tagCount - 1)) query += "OR ";
                }

                query += ") " +
                    "ORDER BY list_order;";

                Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(contextId) });

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(0);
                        String name = cursor.getString(1);
                        long order = cursor.getLong(2);

                        tags.add(new TaskTag(id, contextId, name, order));
                    }
                    while (cursor.moveToNext());
                }
            }
            finally {
                db.close();
            }
        }

        return tags;
    }

    // Returns all the task tags related to a given task, using an already open DB connection.
    // This is an auxiliar method intended to be used only inside the "getTask" method.
    private List<TaskTag> getAllTaskTags(SQLiteDatabase db, long taskId) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        Cursor cursor = db.rawQuery("SELECT t.id, t.task_context_id, t.name, t.list_order FROM task_tags AS t, task_tag_relationships AS r " +
            "WHERE t.id = r.task_tag_id AND r.task_id = ? " +
            "ORDER BY t.list_order;", new String[] { String.valueOf(taskId) });

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long contextId = cursor.getLong(1);
                String name = cursor.getString(2);
                long order = cursor.getLong(3);

                tags.add(new TaskTag(id, contextId, name, order));
            }
            while (cursor.moveToNext());
        }

        return tags;
    }

    // This is an auxiliar method intended to be used only inside the "getTask" method.
    private TaskTag getTaskTag(SQLiteDatabase db, String name) {
        TaskTag tag = null;

        Cursor cursor = db.rawQuery("SELECT id, task_context_id, list_order FROM task_tags " +
            "WHERE name = ? " +
            "ORDER BY list_order;", new String[] { name });

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            long contextId = cursor.getLong(1);
            long order = cursor.getLong(2);

            tag = new TaskTag(id, contextId, name, order);
        }

        return tag;
    }

    private void saveTaskTag(SQLiteDatabase db, TaskTag tag) {
        long id = tag.getId();

        ContentValues values = new ContentValues();
        values.put("task_context_id", tag.getContextId());
        values.put("name", tag.getName());
        values.put("list_order", tag.getOrder());

        if (id < 0) {
            long newId = db.insert("task_tags", null, values);
            tag.setId(newId);
        }
        else {
            db.update("task_tags", values, "id = ?", new String[] { String.valueOf(id) });
        }
    }
    
    public void saveTaskTag(TaskTag tag) {
        SQLiteDatabase db = null;

        try {
            db = getWritableDatabase();
            saveTaskTag(db, tag);    
        }
        finally {
            db.close();
        }
    }

    // Returns all open tasks that belong to a given task context and
    // that could be done at the current day ("doable-today" tasks)
    // and have any of the given task tags. Returns all "doable-today"
    // tasks of the context if "filterTags" is null or empty.
    //
    // A doable-today task means that the task is not done plus one of
    // the following cases:
    //   1) The task has no start date.
    //   2) The task has a start date and the date is the current day
    //      or before.
    //
    // Every returned task is incomplete because this method is used
    // to get a list of tasks, without displaying every task's details.
    public List<Task> getDoableTodayTasks(TaskContext context, List<TaskTag> filterTags) {
        List<Task> tasks = new LinkedList<Task>();
        long contextId = context.getId();

        int tagCount = 0;
        if (filterTags != null) tagCount = filterTags.size();

        DateTimeTool tool = new DateTimeTool();
        String currentDay = tool.getIso8601DateTime(Calendar.getInstance());

        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            String query = "";

            if (tagCount == 0) {
                query += "SELECT id, title, start_datetime, deadline_datetime " +
                    "FROM tasks " +
                    "WHERE task_context_id = ? " +
                    "AND (start_datetime IS NULL OR start_datetime = '' OR start_datetime <= ?) " +
                    "AND done = 0 " +
                    "ORDER BY deadline_datetime;";
            }
            else {
                query += "SELECT DISTINCT tasks.id, tasks.title, tasks.start_datetime, tasks.deadline_datetime " +
                    "FROM tasks, task_tag_relationships, task_tags " +
                    "WHERE tasks.task_context_id = ? " +
                    "AND tasks.id = task_tag_relationships.task_id AND task_tag_relationships.task_tag_id = task_tags.id " +
                    "AND (tasks.start_datetime IS NULL OR tasks.start_datetime = '' OR tasks.start_datetime <= ?) " +
                    "AND tasks.done = 0 " +
                    "AND (";

                    for (int i = 0; i < tagCount; i++) {
                        TaskTag tag = filterTags.get(i);
                        query += String.format("task_tags.name = '%s' ", tag.getName());

                        if (i < (tagCount - 1)) query += "OR ";
                    }

                    query += ") " +
                        "ORDER BY tasks.deadline_datetime;";
            }

            Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(contextId), currentDay });

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    Calendar start = tool.getCalendar(cursor.getString(2));
                    Calendar deadline = tool.getCalendar(cursor.getString(3));
                    List<TaskTag> tags = getAllTaskTags(db, id);

                    tasks.add(new Task(id, contextId, title, null, start, deadline, false, tags));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return tasks;
    }

    // Returns all tasks that belong to a given context, given its state and any of its tags.
    // It returns all tasks of the context with the given state if "filterTags" is null or
    // empty.
    //
    // Every returned task is incomplete because this method is used
    // to get a list of tasks, without displaying every task's details.
    public List<Task> getTasks(TaskContext context, boolean done, List<TaskTag> filterTags) {
        List<Task> tasks = new LinkedList<Task>();
        long contextId = context.getId();

        int tagCount = 0;
        if (filterTags != null) tagCount = filterTags.size();

        DateTimeTool tool = new DateTimeTool();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            String query = "";

            if (tagCount == 0) {
                query += "SELECT id, title, start_datetime, deadline_datetime " +
                    "FROM tasks " +
                    "WHERE task_context_id = ? ";

                if (done) query += "AND done = 1 ";
                else query += "AND done = 0 ";

                query += "ORDER BY deadline_datetime;";
            }
            else {
                query += "SELECT DISTINCT tasks.id, tasks.title, tasks.start_datetime, tasks.deadline_datetime " +
                    "FROM tasks, task_tag_relationships, task_tags " +
                    "WHERE tasks.task_context_id = ? " +
                    "AND tasks.id = task_tag_relationships.task_id AND task_tag_relationships.task_tag_id = task_tags.id ";

                if (done) query += "AND tasks.done = 1 ";
                else query += "AND tasks.done = 0 ";

                query += "AND (";

                for (int i = 0; i < tagCount; i++) {
                    TaskTag tag = filterTags.get(i);
                    query += String.format("task_tags.name = '%s' ", tag.getName());

                    if (i < (tagCount - 1)) query += "OR ";
                }

                query += ") " +
                    "ORDER BY tasks.deadline_datetime;";
            }

            Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(contextId) });

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String title = cursor.getString(1);
                    Calendar start = tool.getCalendar(cursor.getString(2));
                    Calendar deadline = tool.getCalendar(cursor.getString(3));
                    List<TaskTag> tags = getAllTaskTags(db, id);

                    tasks.add(new Task(id, contextId, title, null, start, deadline, done, tags));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            db.close();
        }

        return tasks;
    }

    public Task getTask(long id) {
        Task task = null;

        DateTimeTool tool = new DateTimeTool();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT task_context_id, title, description, start_datetime, deadline_datetime, done " +
                "FROM tasks WHERE id = ?;", new String[] { String.valueOf(id) });

            if (cursor.moveToFirst()) {
                long contextId = cursor.getLong(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                Calendar start = tool.getCalendar(cursor.getString(3));
                Calendar deadline = tool.getCalendar(cursor.getString(4));
                boolean done = (cursor.getInt(5) != 0);
                List<TaskTag> tags = getAllTaskTags(db, id);

                task = new Task(id, contextId, title, description, start, deadline, done, tags);
            }
        }
        finally {
            db.close();
        }

        return task;
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

        try {
            db = getWritableDatabase();
            
            if (id < 0) {
                long newId = db.insert("tasks", null, values);
                task.setId(newId);
            }
            else {
                db.update("tasks", values, "id = ?", new String[] { String.valueOf(id) });
            }

            updateTaskTagRelationships(db, task);
        }
        finally {
            db.close();
        }
    }

    // This is an auxiliar method intended to be used only inside the "saveTask" method.
    // "db" must represent an already open DB.
    private void updateTaskTagRelationships(SQLiteDatabase db, Task task) {
        // Delete old tag relationships
        long taskId = task.getId();

        List<TaskTag> oldTags = getAllTaskTags(db, taskId); // Every old tag has a valid ID in the DB, as they come from the DB.
        List<TaskTag> newTags = task.getTags();             // Every new task can have or not a valid ID in the DB, as they come from the application.

        // The comparison to know if a task is contained in a list is through the
        // name of the task, not the ID (see method "equals" in TaskTag class. 
        for (TaskTag t : oldTags) {
            if (!newTags.contains(t)) {
                long oldTagId = t.getId();
                db.delete("task_tag_relationships", "task_id = ? AND task_tag_id = ?", new String[] { String.valueOf(taskId), String.valueOf(oldTagId) });
            }
        }

        // Save new tags and tag relationships
        for (TaskTag tag : newTags) {
            long tagId = -1;
            // Note: "tag" has a not valid ID yet.

            // Check if the tag already exists in the DB. If not, we save it
            // (this is needed before saving the task tag relationship).
            //
            // A tag is considered to exist if there is a tag in the DB that has its same name.
            TaskTag dbTag = getTaskTag(db, tag.getName());

            // If "dbTag" is null, it means that the tag does not exist in the DB yet. In that case,
            // we save the "tag" object. After saving the "tag" object, it will contain its actual
            // ID in the DB, set by the "saveTaskTag" function.
            //
            // If "dbTag" is not null, as it comes from the DB, it has a valid ID.
            if (dbTag == null) {
                saveTaskTag(db, tag);
                tagId = tag.getId();
            }
            else {
                tagId = dbTag.getId();
            }

            // Check if relationship already exists. If not, we create it.
            Cursor cursor = db.rawQuery("SELECT * FROM task_tag_relationships " +
                "WHERE task_id = ? AND task_tag_id = ?;", new String[] { String.valueOf(taskId), String.valueOf(tagId)});

            if (!cursor.moveToFirst()) {
                ContentValues tagRelValues = new ContentValues();
                tagRelValues.put("task_id", taskId);
                tagRelValues.put("task_tag_id", tagId);

                db.insert("task_tag_relationships", null, tagRelValues);
            }
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
