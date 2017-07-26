package jajimenez.workpage.data;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;
import java.io.File;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jajimenez.workpage.R;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class DataManager extends SQLiteOpenHelper {
    public static final String DB_NAME = "workpage.db";
    public static final String TEMP_DB_NAME = "temp.db";
    public static final int DB_VERSION = 3;

    // Constants for the "isDatabaseCompatible" function.
    public static final int COMPATIBLE = 0;
    public static final int ERROR_OPENING_DB = 1;
    public static final int ERROR_DB_NOT_COMPATIBLE = 2;
    public static final int ERROR_DATA_NOT_VALID = 3;

    private Context context;

    public DataManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDBVersion1(db);
        createDBVersion2(db);
        createDBVersion3(db);
    }

    private void createDBVersion1(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE task_contexts (" +
            "id         INTEGER PRIMARY KEY, " +
            "name       TEXT NOT NULL, " +
            "list_order INTEGER NOT NULL DEFAULT 0" +
            ");");

        ContentValues personalContextValues = new ContentValues();
        personalContextValues.put("name", context.getString(R.string.initial_task_context_personal));
        personalContextValues.put("list_order", 0);

        ContentValues workContextValues = new ContentValues();
        workContextValues.put("name", context.getString(R.string.initial_task_context_work));
        workContextValues.put("list_order", 0);

        db.insert("task_contexts", null, personalContextValues);
        db.insert("task_contexts", null, workContextValues);

        db.execSQL("CREATE TABLE task_tags (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER, " +
            "name            TEXT NOT NULL, " +
            "list_order      INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("CREATE TABLE tasks (" +
            "id                INTEGER PRIMARY KEY, " +
            "task_context_id   INTEGER, " +
            "title             TEXT NOT NULL, " +
            "description       TEXT, " +
            "start_datetime    TEXT, " +
            "deadline_datetime TEXT, " +
            "done              INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("CREATE TABLE task_tag_relationships (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER, " +
            "task_tag_id INTEGER, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");
    }

    private void createDBVersion2(SQLiteDatabase db) {
        // Update TaskTags table: Add a new column for color.
        db.execSQL("ALTER TABLE task_tags ADD COLUMN color TEXT;");
    }

    private void createDBVersion3(SQLiteDatabase db) {
        // 1. Update TaskTags table: Remove "list_order" and set foreign key column as Not Null.
        //
        //    Note: It's assumed that the old foreign-key columns don't contain null values,
        //    as the application didn't allowed it.
        updateTaskTagsTableDBVersion3(db);

        // 2. Create new table for reminders.
        createTaskRemindersTableDBVersion3(db);

        // 3. Update Tasks table:
        //    3.1. Set foreign-key column as Not Null.
        //
        //         Note: It's assumed that the old foreign-key columns
        //         don't contain null values, as the application didn't
        //         allowed it.
        //
        //    3.2. Add a new column for a single time (Unix Date/Time format)
        //         to Tasks, convert Start and Deadline to Unix Date/Time format.
        //
        //    3.3. Add fields to set if the time part (hour, minute...) of
        //         the date/time fields should be ignored or not.
        //
        //    3.4. Add new columns for reminders.
        updateTasksTableDBVersion3(db);

        // 4. Update TaskTagRelationships: Set foreign key columns as Not Null.
        //
        //    Note: It's assumed that the old foreign-key columns don't
        //    contain null values, as the application didn't allowed it.
        updateTaskTagRelationshipsTableDBVersion3(db);
    }

    private void updateTaskTagsTableDBVersion3(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL("CREATE TEMPORARY TABLE task_tags_temp (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER NOT NULL, " +
            "name            TEXT NOT NULL, " +
            "color           TEXT, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tags_temp SELECT id, task_context_id, name, color FROM task_tags;");
        db.execSQL("DROP TABLE task_tags;");

        db.execSQL("CREATE TABLE task_tags (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER NOT NULL, " +
            "name            TEXT NOT NULL, " +
            "color           TEXT, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tags SELECT id, task_context_id, name, color FROM task_tags_temp;");
        db.execSQL("DROP TABLE task_tags_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void createTaskRemindersTableDBVersion3(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE task_reminders (" +
            "id      INTEGER PRIMARY KEY, " +
            "minutes INTEGER NOT NULL, " +

            "UNIQUE (minutes)" +
            ");");

        ContentValues[] values = new ContentValues[14];

        // On time.
        values[0] = new ContentValues();
        values[0].put("minutes", 0);

        // 5 minutes.
        values[1] = new ContentValues();
        values[1].put("minutes", 5);

        // 10 minutes.
        values[2] = new ContentValues();
        values[2].put("minutes", 10);

        // 15 minutes.
        values[3] = new ContentValues();
        values[3].put("minutes", 15);

        // 30 minutes. 
        values[4] = new ContentValues();
        values[4].put("minutes", 30);

        // 45 minutes.
        values[5] = new ContentValues();
        values[5].put("minutes", 45);

        // 1 hour (60 minutes).
        values[6] = new ContentValues();
        values[6].put("minutes", 60);

        // 2 hours (120 minutes).
        values[7] = new ContentValues();
        values[7].put("minutes", 120);

        // 4 hours (240 minutes).
        values[8] = new ContentValues();
        values[8].put("minutes", 240);

        // 8 hours (480 minutes).
        values[9] = new ContentValues();
        values[9].put("minutes", 480);

        // 1 day (1440 minutes).
        values[10] = new ContentValues();
        values[10].put("minutes", 1440);

        // 2 days (2880 minutes).
        values[11] = new ContentValues();
        values[11].put("minutes", 2880);

        // 1 week (10080 minutes).
        values[12] = new ContentValues();
        values[12].put("minutes", 10080);

        // 2 weeks (20160 minutes).
        values[13] = new ContentValues();
        values[13].put("minutes", 20160);

        for (int i = 0; i < values.length; i++) {
            db.insert("task_reminders", null, values[i]);
        }
    }

    private void updateTasksTableDBVersion3(SQLiteDatabase db) {
        DateTimeTool tool = new DateTimeTool();
        db.beginTransaction();

        db.execSQL("CREATE TEMPORARY TABLE tasks_temp (" +
            "id                   INTEGER PRIMARY KEY, " +
            "task_context_id      INTEGER NOT NULL, " +
            "title                TEXT NOT NULL, " +
            "description          TEXT, " +

            "when_datetime        INTEGER, " +
            "ignore_when_time     INTEGER NOT NULL DEFAULT 0, " +
            "when_reminder_id     INTEGER, " +

            "start_datetime       INTEGER, " +
            "ignore_start_time    INTEGER NOT NULL DEFAULT 0, " +
            "start_reminder_id    INTEGER, " +

            "deadline_datetime    INTEGER, " +
            "ignore_deadline_time INTEGER NOT NULL DEFAULT 0, " +
            "deadline_reminder_id INTEGER, " +

            "done                 INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (when_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (start_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (deadline_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL" +
            ");");

        // Cursor for the old table.
        Cursor tasksCursor = db.rawQuery("SELECT id, task_context_id, title, description, start_datetime, deadline_datetime, done FROM tasks", null);

        // Values for the new table.
        ContentValues tasksValues = null;

        if (tasksCursor.moveToFirst()) {
            do {
                tasksValues = new ContentValues();
                tasksValues.put("id", tasksCursor.getLong(0));
                tasksValues.put("task_context_id", tasksCursor.getLong(1));
                tasksValues.put("title", tasksCursor.getString(2));
                tasksValues.put("description", tasksCursor.getString(3));

                // If "start" and "deadline" are the same date in the old table,
                // then we convert them into "when" in the new table.
                String start = null;
                String deadline = null;

                if (!tasksCursor.isNull(4)) start = tasksCursor.getString(4);
                if (!tasksCursor.isNull(5)) deadline = tasksCursor.getString(5);

                if (start != null && deadline != null && start.equals(deadline)) {
                    tasksValues.put("when_datetime", (tool.getCalendar(start)).getTimeInMillis());
                    tasksValues.put("ignore_when_time", 1);
                }
                else {
                    if (start != null) {
                        tasksValues.put("start_datetime", (tool.getCalendar(start)).getTimeInMillis());
                        tasksValues.put("ignore_start_time", 1);
                    }

                    if (deadline != null) {
                        tasksValues.put("deadline_datetime", (tool.getCalendar(deadline)).getTimeInMillis());
                        tasksValues.put("ignore_deadline_time", 1);
                    }
                }

                tasksValues.put("done", tasksCursor.getLong(6));

                db.insert("tasks_temp", null, tasksValues);
            }
            while (tasksCursor.moveToNext());
        }

        db.execSQL("DROP TABLE tasks;");

        db.execSQL("CREATE TABLE tasks (" +
            "id                   INTEGER PRIMARY KEY, " +
            "task_context_id      INTEGER NOT NULL, " +
            "title                TEXT NOT NULL, " +
            "description          TEXT, " +

            "when_datetime        INTEGER, " +
            "ignore_when_time     INTEGER DEFAULT 1, " +
            "when_reminder_id     INTEGER, " +

            "start_datetime       INTEGER, " +
            "ignore_start_time    INTEGER DEFAULT 1, " +
            "start_reminder_id    INTEGER, " +

            "deadline_datetime    INTEGER, " +
            "ignore_deadline_time INTEGER DEFAULT 1, " +
            "deadline_reminder_id INTEGER, " +

            "done                 INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (when_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (start_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (deadline_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL" +
            ");");

        db.execSQL("INSERT INTO tasks SELECT id, task_context_id, title, description, " +
            "when_datetime, ignore_when_time, when_reminder_id, " +
            "start_datetime, ignore_start_time, start_reminder_id, " +
            "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
            "done " +
            "FROM tasks_temp;");

        db.execSQL("DROP TABLE tasks_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateTaskTagRelationshipsTableDBVersion3(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL("CREATE TEMPORARY TABLE task_tag_relationships_temp (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER NOT NULL, " +
            "task_tag_id INTEGER NOT NULL, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tag_relationships_temp SELECT id, task_id, task_tag_id FROM task_tag_relationships;");
        db.execSQL("DROP TABLE task_tag_relationships;");

        db.execSQL("CREATE TABLE task_tag_relationships (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER NOT NULL, " +
            "task_tag_id INTEGER NOT NULL, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tag_relationships SELECT id, task_id, task_tag_id FROM task_tag_relationships_temp;");
        db.execSQL("DROP TABLE task_tag_relationships_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1) {
            createDBVersion2(db);
            createDBVersion3(db);
        }
        else if (oldVersion == 2) {
            createDBVersion3(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onConfigure(db);

        if (!db.isReadOnly()) db.setForeignKeyConstraintsEnabled(true);
    }

    public File getDatabaseFile() {
        return context.getDatabasePath(DB_NAME);
    }

    public File getTemporalDatabaseFile() {
        File dir = context.getFilesDir();
        return new File(dir, TEMP_DB_NAME);
    }

    public static int isDatabaseCompatible(File dbFile) {
        SQLiteDatabase db = null;

        try {
            db = SQLiteDatabase.openDatabase(dbFile.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);
        }
        catch (Exception e) {
            return ERROR_OPENING_DB;
        }

        // Check DB version.
        int dbVersion = db.getVersion();
        if (dbVersion < 1 || dbVersion > DB_VERSION) return ERROR_DB_NOT_COMPATIBLE;

        // Check that the tables are the expected ones:
        //     * Only TaskTags and Tasks are not the same in the 3 database versions.
        //     * TaskReminders is new in version 3.
        String taskContextsTableSql = "SELECT id, name, list_order FROM task_contexts LIMIT 1";
        String taskRemindersTableSql = "";
        String taskTagsTableSql = null;
        String tasksTableSql = null;
        String taskTagRelationshipsTableSql = "SELECT id, task_id, task_tag_id FROM task_tag_relationships LIMIT 1";

        if (dbVersion == 1) { 
            taskTagsTableSql = "SELECT id, task_context_id, name, list_order FROM task_tags LIMIT 1";
        }
        else if (dbVersion == 2) {
            taskTagsTableSql = "SELECT id, task_context_id, name, list_order, color FROM task_tags LIMIT 1";
        }
        else { // dbVersion = 3
            taskTagsTableSql = "SELECT id, task_context_id, name, color FROM task_tags LIMIT 1";
            taskRemindersTableSql = "SELECT id, minutes FROM task_reminders LIMIT 1";
        }

        if (dbVersion == 1 || dbVersion == 2) { 
            tasksTableSql = "SELECT id, task_context_id, title, description, start_datetime, deadline_datetime, done FROM tasks LIMIT 1";
        }
        else { // dbVersion = 3
            tasksTableSql = "SELECT id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done " +
                "FROM tasks LIMIT 1";
        }

        try {
            db.rawQuery(taskContextsTableSql, null);

            if (dbVersion == 3) db.rawQuery(taskRemindersTableSql, null);

            db.rawQuery(taskTagsTableSql, null);
            db.rawQuery(tasksTableSql, null);
            db.rawQuery(taskTagRelationshipsTableSql, null);
        }
        catch (Exception e) {
            return ERROR_DB_NOT_COMPATIBLE;
        }

        // Check DB integrity.
        if (!db.isDatabaseIntegrityOk()) return ERROR_DATA_NOT_VALID;

        db.close();

        return COMPATIBLE;
    }

    // Returns all task contexts.
    public List<TaskContext> getAllTaskContexts() {
        List<TaskContext> contexts = new LinkedList<TaskContext>(); 
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, list_order FROM task_contexts ORDER BY list_order", null);

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
            if (db != null) db.close();
        }

        return contexts;
    }

    public TaskContext getTaskContext(long id) {
        TaskContext context = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT name, list_order FROM task_contexts WHERE id = ?",
                new String[] { String.valueOf(id) });

            if (cursor.moveToFirst()) {
                String name = cursor.getString(0);
                long order = cursor.getLong(1);
                context = new TaskContext(id, name, order);
            }
        }
        finally {
            if (db != null) db.close();
        }

        return context;
    }

    // Creates or updates a task context in the database.
    // If the ID of the task context is less than 0, it
    // inserts a new row in the "task_contexts" table
    // ignoring that ID and updates the ID attribute of
    // the TaskContext given object. Otherwise, it updates
    // the row of the given ID.
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
            if (db != null) db.close();
        }
    }

    public void deleteTaskContexts(List<TaskContext> contexts) {
        SQLiteDatabase db = null;
        
        try {
            db = getWritableDatabase();
            for (TaskContext c : contexts) db.delete("task_contexts", "id = ?", new String[] { String.valueOf(c.getId()) });
        }
        finally {
            if (db != null) db.close();
        }
    }

    // Returns all task reminders.
    public List<TaskReminder> getAllTaskReminders() {
        List<TaskReminder> reminders = new LinkedList<TaskReminder>();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, minutes FROM task_reminders ORDER BY id", null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    long minutes = cursor.getLong(1);

                    reminders.add(new TaskReminder(id, minutes));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            if (db != null) db.close();
        }

        return reminders;
    }

    public TaskReminder getTaskReminder(long id) {
        TaskReminder reminder = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            reminder = getTaskReminder(db, id);
        }
        finally {
            if (db != null) db.close();
        }

        return reminder;
    }

    // This is an auxiliar method intended to be used only inside
    // the "getTaskReminder(id) and "getTask" methods.
    private TaskReminder getTaskReminder(SQLiteDatabase db, long id) {
        TaskReminder reminder = null;

        Cursor cursor = db.rawQuery("SELECT minutes FROM task_reminders " +
            "WHERE id = ?", new String[] { String.valueOf(id) });

        if (cursor.moveToFirst()) {
            long minutes = cursor.getLong(0);

            reminder = new TaskReminder(id, minutes);
        }

        return reminder;
    }

    public int getTaskTagCount(TaskContext context) {
        int count = 0;

        if (context != null) {
            SQLiteDatabase db = null;

            try {
                db = getReadableDatabase();
                String query = "SELECT count(id) FROM task_tags WHERE task_context_id = ?";
                Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(context.getId()) });

                if (cursor.moveToFirst()) count = cursor.getInt(0);
            }
            finally {
                if (db != null) db.close();
            }
        }

        return count;
    }

    // Returns all the task tags that belong to a task context.
    public List<TaskTag> getAllTaskTags(TaskContext context) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        long contextId = context.getId();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, name, color FROM task_tags " +
                "WHERE task_context_id = ? " +
                "ORDER BY name", new String[] { String.valueOf(contextId) });

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    String color = cursor.getString(2);

                    tags.add(new TaskTag(id, contextId, name, color));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            if (db != null) db.close();
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
                String query = "SELECT id, name, color FROM task_tags " +
                    "WHERE task_context_id = ? " +
                    "AND (";

                String[] selectionArgs = new String[1 + tagCount];
                selectionArgs[0] = String.valueOf(contextId);

                for (int i = 0; i < tagCount; i++) {
                    String name = tagNames.get(i);

                    query += "name = ? ";
                    if (i < (tagCount - 1)) query += "OR ";

                    selectionArgs[i + 1] = name;
                }

                query += ") " +
                    "ORDER BY name";

                Cursor cursor = db.rawQuery(query, selectionArgs);

                if (cursor.moveToFirst()) {
                    do {
                        long id = cursor.getLong(0);
                        String name = cursor.getString(1);
                        String color = cursor.getString(2);

                        tags.add(new TaskTag(id, contextId, name, color));
                    }
                    while (cursor.moveToNext());
                }
            }
            finally {
                if (db != null) db.close();
            }
        }

        return tags;
    }

    // Returns all the task tags related to a given task, using an already open DB connection.
    // This is an auxiliar method intended to be used only inside the "getTask" method.
    private List<TaskTag> getTaskTags(SQLiteDatabase db, long taskId) {
        List<TaskTag> tags = new LinkedList<TaskTag>();

        Cursor cursor = db.rawQuery("SELECT t.id, t.task_context_id, t.name, t.color  " +
            "FROM task_tags AS t, task_tag_relationships AS r " +
            "WHERE t.id = r.task_tag_id AND r.task_id = ? " +
            "ORDER BY t.name", new String[] { String.valueOf(taskId) });

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                long contextId = cursor.getLong(1);
                String name = cursor.getString(2);
                String color = cursor.getString(3);

                tags.add(new TaskTag(id, contextId, name, color));
            }
            while (cursor.moveToNext());
        }

        return tags;
    }

    public TaskTag getTaskTag(long id) {
        TaskTag tag = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT task_context_id, name, color FROM task_tags WHERE id = ?",
                    new String[] { String.valueOf(id) });

            if (cursor.moveToFirst()) {
                long contextId = cursor.getLong(0);
                String name = cursor.getString(1);
                String color = cursor.getString(2);

                tag = new TaskTag(id, contextId, name, color);
            }
        }
        finally {
            if (db != null) db.close();
        }

        return tag;
    }

    // This is an auxiliar method intended to be used only inside the "getTask" method.
    private TaskTag getTaskTag(SQLiteDatabase db, long taskContextId, String name) {
        TaskTag tag = null;

        Cursor cursor = db.rawQuery("SELECT id, color FROM task_tags " +
            "WHERE task_context_id = ?" +
            "AND name = ?", new String[] { String.valueOf(taskContextId), name });

        if (cursor.moveToFirst()) {
            long id = cursor.getLong(0);
            String color = cursor.getString(1);

            tag = new TaskTag(id, taskContextId, name, color);
        }

        return tag;
    }

    private void saveTaskTag(SQLiteDatabase db, TaskTag tag) {
        long id = tag.getId();

        ContentValues values = new ContentValues();
        values.put("task_context_id", tag.getContextId());
        values.put("name", tag.getName());
        values.put("color", tag.getColor());

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
            if (db != null) db.close();
        }
    }

    public void deleteTaskTags(List<TaskTag> tags) {
        SQLiteDatabase db = null;
        
        try {
            db = getWritableDatabase();
            for (TaskTag t : tags) db.delete("task_tags", "id = ?", new String[] { String.valueOf(t.getId()) });
        }
        finally {
            if (db != null) db.close();
        }
    }

    // Returns all open tasks that belong to a given task context and
    // that could be done at the current day ("doable-today" tasks)
    // and have any of the given task tags or have no tag if
    // "includeTasksWithNoTag" is "true".
    //
    // A doable-today task means that the task is not done plus one of
    // the following cases:
    //   1) The task has no start date.
    //   2) The task has a start date and the date is the current day
    //      or before.
    public List<Task> getDoableTodayTasksByTags(TaskContext context, boolean includeTasksWithNoTag, List<TaskTag> tags) {
        List<Task> tasks = new LinkedList<Task>();

        int tagCount = 0;
        if (tags != null) tagCount = tags.size();

        DateTimeTool tool = new DateTimeTool();
        long contextId = context.getId();

        // Get the next day in Unix Time format.
        Calendar tomorrow = Calendar.getInstance(); // At this point, "tomorrow" is the current time.
        tool.clearTimeFields(tomorrow);             // Clear all the time fields, setting them to 0.
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);     // Now, "tomorrow" is actually tomorrow's time.

        String tomorrowTime = String.valueOf(tomorrow.getTimeInMillis());
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            String query = "";
            Cursor cursor = null;

            if (includeTasksWithNoTag) {
                query = "SELECT tasks.id, tasks.title, tasks.description, " +
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_reminder_id, " +
                    "tasks.done " +
                    "FROM tasks LEFT JOIN task_tag_relationships ON tasks.id = task_tag_relationships.task_id " +
                    "WHERE task_tag_relationships.task_id IS NULL " +
                    "AND tasks.task_context_id = ? " +
                    "AND ((tasks.when_datetime IS NOT NULL AND tasks.when_datetime < ?) " +
                        "OR (tasks.when_datetime IS NULL AND tasks.start_datetime IS NULL) " +
                        "OR (tasks.when_datetime IS NULL AND tasks.start_datetime IS NOT NULL AND tasks.start_datetime < ?)) " +
                    "AND tasks.done = 0 " +
                    "ORDER BY tasks.id";

                cursor = db.rawQuery(query, new String[] { String.valueOf(contextId), tomorrowTime, tomorrowTime });
                tasks.addAll(getTasksFromCursor(db, cursor, contextId));
            }

            if (tagCount > 0) {
                query = "SELECT DISTINCT tasks.id, tasks.title, tasks.description, " +
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_reminder_id, " +
                    "tasks.done " +
                    "FROM tasks, task_tag_relationships, task_tags " +
                    "WHERE tasks.id = task_tag_relationships.task_id AND task_tag_relationships.task_tag_id = task_tags.id " +
                    "AND tasks.task_context_id = ? " +
                    "AND ((tasks.when_datetime IS NOT NULL AND tasks.when_datetime < ?) " +
                        "OR (tasks.when_datetime IS NULL AND tasks.start_datetime IS NULL) " +
                        "OR (tasks.when_datetime IS NULL AND tasks.start_datetime IS NOT NULL AND tasks.start_datetime < ?)) " +
                    "AND tasks.done = 0 " +
                    "AND (";

                    String[] selectionArgs = new String[3 + tagCount];

                    selectionArgs[0] = String.valueOf(contextId);
                    selectionArgs[1] = tomorrowTime;
                    selectionArgs[2] = tomorrowTime;

                    for (int i = 0; i < tagCount; i++) {
                        TaskTag tag = tags.get(i);

                        query += "task_tags.name = ? ";
                        if (i < (tagCount - 1)) query += "OR ";

                        selectionArgs[i + 3] = tag.getName();
                    }

                    query += ") ORDER BY tasks.id";

                cursor = db.rawQuery(query, selectionArgs);
                tasks.addAll(getTasksFromCursor(db, cursor, contextId));
            }
        }
        finally {
            if (db != null) db.close();
        }

        return tasks;
    }

    private List<Task> getTasksFromCursor(SQLiteDatabase db, Cursor cursor, long contextId) {
        List<Task> tasks = new LinkedList<Task>();

        if (cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);

                Calendar when = null;
                if (!cursor.isNull(3)) {
                    when = Calendar.getInstance();
                    when.setTimeInMillis(cursor.getLong(3));
                }

                boolean ignoreWhenTime = false;
                if (!cursor.isNull(4)) ignoreWhenTime = (cursor.getLong(4) != 0);

                TaskReminder whenReminder = null;
                if (!cursor.isNull(5)) {
                    long whenReminderId = cursor.getLong(5);
                    whenReminder = getTaskReminder(db, whenReminderId);
                }

                Calendar start = null;
                if (!cursor.isNull(6)) {
                    start = Calendar.getInstance();
                    start.setTimeInMillis(cursor.getLong(6));
                }

                boolean ignoreStartTime = false;
                if (!cursor.isNull(7)) ignoreStartTime = (cursor.getLong(7) != 0);

                TaskReminder startReminder = null;
                if (!cursor.isNull(8)) {
                    long startReminderId = cursor.getLong(8);
                    startReminder = getTaskReminder(db, startReminderId);
                }

                Calendar deadline = null;
                if (!cursor.isNull(9)) {
                    deadline = Calendar.getInstance();
                    deadline.setTimeInMillis(cursor.getLong(9));
                }

                boolean ignoreDeadlineTime = false;
                if (!cursor.isNull(10)) ignoreDeadlineTime = (cursor.getLong(10) != 0);

                TaskReminder deadlineReminder = null;
                if (!cursor.isNull(11)) {
                    long deadlineReminderId = cursor.getLong(11);
                    deadlineReminder = getTaskReminder(db, deadlineReminderId);
                }

                boolean done = (cursor.getLong(12) != 0);
                List<TaskTag> tags = getTaskTags(db, id);

                tasks.add(new Task(id, contextId, title, description,
                    when, ignoreWhenTime, whenReminder,
                    start, ignoreStartTime, startReminder,
                    deadline, ignoreDeadlineTime, deadlineReminder,
                    done, tags));
            }
            while (cursor.moveToNext());
        }

        return tasks;
    }

    // Returns all tasks that belong to a given context, given its state and any of its
    // tags. It includes the tasks that have no tag if "includeTasksWithNoTag" is "true".
    public List<Task> getTasksByTags(TaskContext context, boolean done, boolean includeTasksWithNoTag , List<TaskTag> tags) {
        List<Task> tasks = new LinkedList<Task>();

        int tagCount = 0;
        if (tags != null) tagCount = tags.size();

        long contextId = context.getId();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            String query = "";
            Cursor cursor = null;

            if (includeTasksWithNoTag) {
                query = "SELECT tasks.id, tasks.title, tasks.description, " +
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_reminder_id, " +
                    "tasks.done " +
                    "FROM tasks LEFT JOIN task_tag_relationships ON tasks.id = task_tag_relationships.task_id " +
                    "WHERE task_tag_relationships.task_id IS NULL " +
                    "AND tasks.task_context_id = ? ";

                if (done) query += "AND tasks.done != 0 ORDER BY tasks.id DESC";
                else query += "AND tasks.done = 0 ORDER BY tasks.id";

                cursor = db.rawQuery(query, new String[] { String.valueOf(contextId) });
                tasks.addAll(getTasksFromCursor(db, cursor, contextId));
            }

            if (tagCount > 0) {
                query = "SELECT DISTINCT tasks.id, tasks.title, tasks.description, " +
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_reminder_id, " +
                    "tasks.done " +
                    "FROM tasks, task_tag_relationships, task_tags " +
                    "WHERE tasks.id = task_tag_relationships.task_id AND task_tag_relationships.task_tag_id = task_tags.id " +
                    "AND tasks.task_context_id = ? " +
                    "AND (";

                String[] selectionArgs = new String[1 + tagCount];
                selectionArgs[0] = String.valueOf(contextId);

                for (int i = 0; i < tagCount; i++) {
                    TaskTag tag = tags.get(i);

                    query += "task_tags.name = ? ";
                    if (i < (tagCount - 1)) query += "OR ";

                    selectionArgs[i + 1] = tag.getName();
                }

                query += ") ";

                if (done) query += "AND tasks.done != 0 ORDER BY tasks.id DESC";
                else query += "AND tasks.done = 0 ORDER BY tasks.id";

                cursor = db.rawQuery(query, selectionArgs);
                tasks.addAll(getTasksFromCursor(db, cursor, contextId));
            }
        }
        finally {
            if (db != null) db.close();
        }

        return tasks;
    }

    public int getTaskCount(boolean done, TaskContext context) {
        int count = 0;

        if (context != null) {
            SQLiteDatabase db = null;

            try {
                db = getReadableDatabase();
                String query = "SELECT count(*) " +
                    "FROM tasks " +
                    "WHERE task_context_id = ? ";

                if (done) query += "AND done = 1";
                else query += "AND done = 0";

                Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(context.getId()) });

                if (cursor.moveToFirst()) count = cursor.getInt(0);
            }
            finally {
                if (db != null) db.close();
            }
        }

        return count;
    }

    public int getTaskCount(boolean done, TaskTag tag) {
        int count = 0;

        if (tag != null) {
            SQLiteDatabase db = null;

            try {
                db = getReadableDatabase();
                String query = "SELECT count(DISTINCT tasks.id) " +
                    "FROM tasks, task_tag_relationships, task_tags " +
                    "WHERE tasks.id = task_tag_relationships.task_id AND task_tag_relationships.task_tag_id = task_tags.id ";

                if (done) query += "AND tasks.done = 1 ";
                else query += "AND tasks.done = 0 ";

                query += "AND task_tags.id = ?";
                Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(tag.getId()) });

                if (cursor.moveToFirst()) count = cursor.getInt(0);
            }
            finally {
                if (db != null) db.close();
            }
        }

        return count;
    }

    public Task getTask(long id) {
        Task task = null;
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done " +
                "FROM tasks WHERE id = ?", new String[] { String.valueOf(id) });

            if (cursor.moveToFirst()) {
                long contextId = cursor.getLong(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);

                Calendar when = null;
                if (!cursor.isNull(3)) {
                    when = Calendar.getInstance();
                    when.setTimeInMillis(cursor.getLong(3));
                }

                boolean ignoreWhenTime = false;
                if (!cursor.isNull(4)) ignoreWhenTime = (cursor.getLong(4) != 0);

                TaskReminder whenReminder = null;
                if (!cursor.isNull(5)) {
                    long whenReminderId = cursor.getLong(5);
                    whenReminder = getTaskReminder(db, whenReminderId);
                }

                Calendar start = null;
                if (!cursor.isNull(6)) {
                    start = Calendar.getInstance();
                    start.setTimeInMillis(cursor.getLong(6));
                }

                boolean ignoreStartTime = false;
                if (!cursor.isNull(7)) ignoreStartTime = (cursor.getLong(7) != 0);

                TaskReminder startReminder = null;
                if (!cursor.isNull(8)) {
                    long startReminderId = cursor.getLong(8);
                    startReminder = getTaskReminder(db, startReminderId);
                }

                Calendar deadline = null;
                if (!cursor.isNull(9)) {
                    deadline = Calendar.getInstance();
                    deadline.setTimeInMillis(cursor.getLong(9));
                }

                boolean ignoreDeadlineTime = false;
                if (!cursor.isNull(10)) ignoreDeadlineTime = (cursor.getLong(10) != 0);

                TaskReminder deadlineReminder = null;
                if (!cursor.isNull(11)) {
                    long deadlineReminderId = cursor.getLong(11);
                    deadlineReminder = getTaskReminder(db, deadlineReminderId);
                }

                boolean done = (cursor.getLong(12) != 0);
                List<TaskTag> tags = getTaskTags(db, id);

                task = new Task(id, contextId, title, description,
                    when, ignoreWhenTime, whenReminder,
                    start, ignoreStartTime, startReminder,
                    deadline, ignoreDeadlineTime, deadlineReminder,
                    done, tags);
            }
        }
        finally {
            if (db != null) db.close();
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

        ContentValues values = new ContentValues();
        values.put("task_context_id", task.getContextId());
        values.put("title", task.getTitle());
        values.put("description", task.getDescription());

        // When
        Calendar when = task.getWhen();
        if (when != null) values.put("when_datetime", when.getTimeInMillis());
        else values.putNull("when_datetime");

        if (task.getIgnoreWhenTime()) values.put("ignore_when_time", 1);
        else values.put("ignore_when_time", 0);

        TaskReminder whenReminder = task.getWhenReminder();
        if (whenReminder != null) values.put("when_reminder_id", whenReminder.getId());
        else values.putNull("when_reminder_id");

        // Start
        Calendar start = task.getStart();
        if (start != null) values.put("start_datetime", start.getTimeInMillis());
        else values.putNull("start_datetime");

        if (task.getIgnoreStartTime()) values.put("ignore_start_time", 1);
        else values.put("ignore_start_time", 0);

        TaskReminder startReminder = task.getStartReminder();
        if (startReminder != null) values.put("start_reminder_id", startReminder.getId());
        else values.putNull("start_reminder_id");

        // Deadline
        Calendar deadline = task.getDeadline();
        if (deadline != null) values.put("deadline_datetime", deadline.getTimeInMillis());
        else values.putNull("deadline_datetime");

        if (task.getIgnoreDeadlineTime()) values.put("ignore_deadline_time", 1);
        else values.put("ignore_deadline_time", 0);

        TaskReminder deadlineReminder = task.getDeadlineReminder();
        if (deadlineReminder != null) values.put("deadline_reminder_id", deadlineReminder.getId());
        else values.putNull("deadline_reminder_id");

        // Done
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
            if (db != null) db.close();
        }
    }

    // This is an auxiliar method intended to be used only inside the "saveTask" method.
    // "db" must represent an already open DB.
    private void updateTaskTagRelationships(SQLiteDatabase db, Task task) {
        // Delete old tag relationships
        long taskId = task.getId();

        List<TaskTag> oldTags = getTaskTags(db, taskId); // Every old tag has a valid ID in the DB, as they come from the DB.
        List<TaskTag> newTags = task.getTags();          // Every new task can have or not a valid ID in the DB, as they come from the application.

        // The comparison to know if a task is contained in a list is through the
        // name of the task, not the ID (see method "equals" in TaskTag class. 
        for (TaskTag t : oldTags) {
            if (!newTags.contains(t)) {
                long oldTagId = t.getId();
                db.delete("task_tag_relationships", "task_id = ? AND task_tag_id = ?", new String[] { String.valueOf(taskId), String.valueOf(oldTagId) });
            }
        }

        long taskContextId = task.getContextId();

        // Save new tags and tag relationships
        for (TaskTag tag : newTags) {
            long tagId = -1;
            // Note: "tag" has a not valid ID yet.

            // Check if the tag already exists in the DB. If not, we save it
            // (this is needed before saving the task tag relationship).
            //
            // A tag is considered to exist if there is a tag in the DB that has its same name.
            TaskTag dbTag = getTaskTag(db, taskContextId, tag.getName());

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
            Cursor cursor = db.rawQuery("SELECT id, task_id, task_tag_id FROM task_tag_relationships " +
                "WHERE task_id = ? AND task_tag_id = ?", new String[] { String.valueOf(taskId), String.valueOf(tagId)});

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
            if (db != null) db.close();
        }
    }
}
