package jajimenez.workpage.data;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;
import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jajimenez.workpage.R;
import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class DataManager extends SQLiteOpenHelper {
    public static final String DB_NAME = "workpage.db";
    public static final String TEMP_DB_NAME = "temp.db";
    public static final int DB_VERSION = 4;

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
        createDBVersion4(db);
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

        // Copy the current Task Tags table into a new temporary table
        db.execSQL("CREATE TEMPORARY TABLE task_tags_temp (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER NOT NULL, " +
            "name            TEXT NOT NULL, " +
            "color           TEXT, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tags_temp (id, task_context_id, name, color) " +
            "SELECT id, task_context_id, name, color FROM task_tags;");

        // Delete the current Task Tags table
        db.execSQL("DROP TABLE task_tags;");

        // Create again the Task Tags table, with its new structure
        db.execSQL("CREATE TABLE task_tags (" +
            "id              INTEGER PRIMARY KEY, " +
            "task_context_id INTEGER NOT NULL, " +
            "name            TEXT NOT NULL, " +
            "color           TEXT, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tags SELECT id, task_context_id, name, color FROM task_tags_temp;");

        // Delete the temporary Task Tags table
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

        // Copy the current Tasks table into a new temporary table
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

        // Delete the current Tasks table
        db.execSQL("DROP TABLE tasks;");

        // Create again the Tasks table, with its new structure
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

        db.execSQL("INSERT INTO tasks (id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done) " +
            "SELECT id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done " +
            "FROM tasks_temp;");

        // Delete the temporary Tasks table
        db.execSQL("DROP TABLE tasks_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateTaskTagRelationshipsTableDBVersion3(SQLiteDatabase db) {
        db.beginTransaction();

        // Copy the current Task Tag Relationships table into a new temporary table
        db.execSQL("CREATE TEMPORARY TABLE task_tag_relationships_temp (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER NOT NULL, " +
            "task_tag_id INTEGER NOT NULL, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tag_relationships_temp (id, task_id, task_tag_id) " +
            "SELECT id, task_id, task_tag_id FROM task_tag_relationships;");

        // Delete the current Task Tag Relationships table
        db.execSQL("DROP TABLE task_tag_relationships;");

        // Create again the Task Tag Relationships table, with its new structure
        db.execSQL("CREATE TABLE task_tag_relationships (" +
            "id          INTEGER PRIMARY KEY, " +
            "task_id     INTEGER NOT NULL, " +
            "task_tag_id INTEGER NOT NULL, " +

            "UNIQUE (task_id, task_tag_id), " +
            "FOREIGN KEY (task_id) REFERENCES tasks(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (task_tag_id) REFERENCES task_tags(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.execSQL("INSERT INTO task_tag_relationships (id, task_id, task_tag_id) " +
            "SELECT id, task_id, task_tag_id FROM task_tag_relationships_temp;");

        // Delete the temporal Task Tag Relationships table
        db.execSQL("DROP TABLE task_tag_relationships_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void createDBVersion4(SQLiteDatabase db) {
        // Create a new table for having a list of all existing countries
        createCountriesTableDBVersion4(db);
        insertAllCountriesDBVersion4(db);

        // Create a new table for having a list of all existing time zones
        createTimeZonesTableDBVersion4(db);
        insertAllTimeZonesDBVersion4(db);

        // Adding 3 new columns in the Tasks table, for defining the
        // time zone of the date/times When, Start and Deadline
        updateTasksTableDBVersion4(db);
    }

    private void createCountriesTableDBVersion4(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL("CREATE TABLE countries (" +
            "id   INTEGER PRIMARY KEY, " +
            "code TEXT NOT NULL, " +

            "UNIQUE (code)" +
            ");");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertAllCountriesDBVersion4(SQLiteDatabase db) {
        db.beginTransaction();

        insertCountryDBVersion4(db, "af");
        insertCountryDBVersion4(db, "ax");
        insertCountryDBVersion4(db, "al");
        insertCountryDBVersion4(db, "dz");
        insertCountryDBVersion4(db, "as");
        insertCountryDBVersion4(db, "ad");
        insertCountryDBVersion4(db, "ao");
        insertCountryDBVersion4(db, "ai");
        insertCountryDBVersion4(db, "aq");
        insertCountryDBVersion4(db, "ag");
        insertCountryDBVersion4(db, "ar");
        insertCountryDBVersion4(db, "am");
        insertCountryDBVersion4(db, "aw");
        insertCountryDBVersion4(db, "au");
        insertCountryDBVersion4(db, "at");
        insertCountryDBVersion4(db, "az");

        insertCountryDBVersion4(db, "bs");
        insertCountryDBVersion4(db, "bh");
        insertCountryDBVersion4(db, "bd");
        insertCountryDBVersion4(db, "bb");
        insertCountryDBVersion4(db, "by");
        insertCountryDBVersion4(db, "be");
        insertCountryDBVersion4(db, "bz");
        insertCountryDBVersion4(db, "bj");
        insertCountryDBVersion4(db, "bm");
        insertCountryDBVersion4(db, "bt");
        insertCountryDBVersion4(db, "bo");
        insertCountryDBVersion4(db, "bq");
        insertCountryDBVersion4(db, "ba");
        insertCountryDBVersion4(db, "bw");
        insertCountryDBVersion4(db, "br");

        insertCountryDBVersion4(db, "io");
        insertCountryDBVersion4(db, "vg");
        insertCountryDBVersion4(db, "bn");
        insertCountryDBVersion4(db, "bg");
        insertCountryDBVersion4(db, "bf");
        insertCountryDBVersion4(db, "bi");

        insertCountryDBVersion4(db, "kh");
        insertCountryDBVersion4(db, "cm");
        insertCountryDBVersion4(db, "ca");
        insertCountryDBVersion4(db, "cv");
        insertCountryDBVersion4(db, "ky");
        insertCountryDBVersion4(db, "cf");
        insertCountryDBVersion4(db, "td");
        insertCountryDBVersion4(db, "cl");
        insertCountryDBVersion4(db, "cn");
        insertCountryDBVersion4(db, "cx");
        insertCountryDBVersion4(db, "cc");
        insertCountryDBVersion4(db, "co");
        insertCountryDBVersion4(db, "km");
        insertCountryDBVersion4(db, "cd");
        insertCountryDBVersion4(db, "cg");
        insertCountryDBVersion4(db, "ck");
        insertCountryDBVersion4(db, "cr");
        insertCountryDBVersion4(db, "ci");
        insertCountryDBVersion4(db, "hr");
        insertCountryDBVersion4(db, "cu");
        insertCountryDBVersion4(db, "cw");
        insertCountryDBVersion4(db, "cy");
        insertCountryDBVersion4(db, "cz");

        insertCountryDBVersion4(db, "dk");
        insertCountryDBVersion4(db, "dj");
        insertCountryDBVersion4(db, "dm");
        insertCountryDBVersion4(db, "do");

        insertCountryDBVersion4(db, "ec");
        insertCountryDBVersion4(db, "eg");
        insertCountryDBVersion4(db, "sv");
        insertCountryDBVersion4(db, "gq");
        insertCountryDBVersion4(db, "er");
        insertCountryDBVersion4(db, "ee");
        insertCountryDBVersion4(db, "et");

        insertCountryDBVersion4(db, "fk");
        insertCountryDBVersion4(db, "fo");
        insertCountryDBVersion4(db, "fj");
        insertCountryDBVersion4(db, "fi");
        insertCountryDBVersion4(db, "fr");
        insertCountryDBVersion4(db, "gf");
        insertCountryDBVersion4(db, "pf");
        insertCountryDBVersion4(db, "tf");

        insertCountryDBVersion4(db, "ga");
        insertCountryDBVersion4(db, "gm");
        insertCountryDBVersion4(db, "ge");
        insertCountryDBVersion4(db, "de");
        insertCountryDBVersion4(db, "gh");
        insertCountryDBVersion4(db, "gi");
        insertCountryDBVersion4(db, "gr");
        insertCountryDBVersion4(db, "gl");
        insertCountryDBVersion4(db, "gd");
        insertCountryDBVersion4(db, "gp");
        insertCountryDBVersion4(db, "gu");
        insertCountryDBVersion4(db, "gt");
        insertCountryDBVersion4(db, "gg");
        insertCountryDBVersion4(db, "gn");
        insertCountryDBVersion4(db, "gw");
        insertCountryDBVersion4(db, "gy");

        insertCountryDBVersion4(db, "ht");
        insertCountryDBVersion4(db, "hn");
        insertCountryDBVersion4(db, "hk");
        insertCountryDBVersion4(db, "hu");

        insertCountryDBVersion4(db, "is");
        insertCountryDBVersion4(db, "in");
        insertCountryDBVersion4(db, "id");
        insertCountryDBVersion4(db, "ir");
        insertCountryDBVersion4(db, "iq");
        insertCountryDBVersion4(db, "ie");
        insertCountryDBVersion4(db, "im");
        insertCountryDBVersion4(db, "il");
        insertCountryDBVersion4(db, "it");

        insertCountryDBVersion4(db, "jm");
        insertCountryDBVersion4(db, "jp");
        insertCountryDBVersion4(db, "je");
        insertCountryDBVersion4(db, "jo");
        insertCountryDBVersion4(db, "kz");
        insertCountryDBVersion4(db, "ke");
        insertCountryDBVersion4(db, "ki");
        insertCountryDBVersion4(db, "kw");
        insertCountryDBVersion4(db, "kg");

        insertCountryDBVersion4(db, "la");
        insertCountryDBVersion4(db, "lv");
        insertCountryDBVersion4(db, "lb");
        insertCountryDBVersion4(db, "ls");
        insertCountryDBVersion4(db, "lr");
        insertCountryDBVersion4(db, "ly");
        insertCountryDBVersion4(db, "li");
        insertCountryDBVersion4(db, "lt");
        insertCountryDBVersion4(db, "lu");

        insertCountryDBVersion4(db, "mo");
        insertCountryDBVersion4(db, "mk");
        insertCountryDBVersion4(db, "mg");
        insertCountryDBVersion4(db, "mw");
        insertCountryDBVersion4(db, "my");
        insertCountryDBVersion4(db, "mv");
        insertCountryDBVersion4(db, "ml");
        insertCountryDBVersion4(db, "mt");
        insertCountryDBVersion4(db, "mh");
        insertCountryDBVersion4(db, "mq");
        insertCountryDBVersion4(db, "mr");
        insertCountryDBVersion4(db, "mu");
        insertCountryDBVersion4(db, "yt");
        insertCountryDBVersion4(db, "mx");
        insertCountryDBVersion4(db, "fm");
        insertCountryDBVersion4(db, "md");
        insertCountryDBVersion4(db, "mc");
        insertCountryDBVersion4(db, "mn");
        insertCountryDBVersion4(db, "me");
        insertCountryDBVersion4(db, "ms");
        insertCountryDBVersion4(db, "ma");
        insertCountryDBVersion4(db, "mz");
        insertCountryDBVersion4(db, "mm");

        insertCountryDBVersion4(db, "na");
        insertCountryDBVersion4(db, "nr");
        insertCountryDBVersion4(db, "np");
        insertCountryDBVersion4(db, "nl");
        insertCountryDBVersion4(db, "nc");
        insertCountryDBVersion4(db, "nz");
        insertCountryDBVersion4(db, "ni");
        insertCountryDBVersion4(db, "ne");
        insertCountryDBVersion4(db, "ng");
        insertCountryDBVersion4(db, "nu");
        insertCountryDBVersion4(db, "nf");
        insertCountryDBVersion4(db, "kp");
        insertCountryDBVersion4(db, "mp");
        insertCountryDBVersion4(db, "no");

        insertCountryDBVersion4(db, "om");

        insertCountryDBVersion4(db, "pk");
        insertCountryDBVersion4(db, "pw");
        insertCountryDBVersion4(db, "ps");
        insertCountryDBVersion4(db, "pa");
        insertCountryDBVersion4(db, "pg");
        insertCountryDBVersion4(db, "py");
        insertCountryDBVersion4(db, "pe");
        insertCountryDBVersion4(db, "ph");
        insertCountryDBVersion4(db, "pn");
        insertCountryDBVersion4(db, "pl");
        insertCountryDBVersion4(db, "pt");
        insertCountryDBVersion4(db, "pr");

        insertCountryDBVersion4(db, "qa");

        insertCountryDBVersion4(db, "re");
        insertCountryDBVersion4(db, "ro");
        insertCountryDBVersion4(db, "ru");
        insertCountryDBVersion4(db, "rw");
        insertCountryDBVersion4(db, "bl");
        insertCountryDBVersion4(db, "sh");
        insertCountryDBVersion4(db, "kn");
        insertCountryDBVersion4(db, "lc");
        insertCountryDBVersion4(db, "mf");
        insertCountryDBVersion4(db, "pm");
        insertCountryDBVersion4(db, "vc");
        insertCountryDBVersion4(db, "ws");
        insertCountryDBVersion4(db, "sm");
        insertCountryDBVersion4(db, "st");
        insertCountryDBVersion4(db, "sa");
        insertCountryDBVersion4(db, "sn");
        insertCountryDBVersion4(db, "rs");
        insertCountryDBVersion4(db, "sc");
        insertCountryDBVersion4(db, "sl");
        insertCountryDBVersion4(db, "sg");
        insertCountryDBVersion4(db, "sx");
        insertCountryDBVersion4(db, "sk");
        insertCountryDBVersion4(db, "si");
        insertCountryDBVersion4(db, "sb");
        insertCountryDBVersion4(db, "so");
        insertCountryDBVersion4(db, "za");
        insertCountryDBVersion4(db, "gs");
        insertCountryDBVersion4(db, "kr");
        insertCountryDBVersion4(db, "ss");
        insertCountryDBVersion4(db, "es");
        insertCountryDBVersion4(db, "lk");
        insertCountryDBVersion4(db, "sd");
        insertCountryDBVersion4(db, "sr");
        insertCountryDBVersion4(db, "sj");
        insertCountryDBVersion4(db, "sz");
        insertCountryDBVersion4(db, "se");
        insertCountryDBVersion4(db, "ch");
        insertCountryDBVersion4(db, "sy");

        insertCountryDBVersion4(db, "tw");
        insertCountryDBVersion4(db, "tj");
        insertCountryDBVersion4(db, "tz");
        insertCountryDBVersion4(db, "th");
        insertCountryDBVersion4(db, "tl");
        insertCountryDBVersion4(db, "tg");
        insertCountryDBVersion4(db, "tk");
        insertCountryDBVersion4(db, "to");
        insertCountryDBVersion4(db, "tt");
        insertCountryDBVersion4(db, "tn");
        insertCountryDBVersion4(db, "tr");
        insertCountryDBVersion4(db, "tm");
        insertCountryDBVersion4(db, "tc");
        insertCountryDBVersion4(db, "tv");

        insertCountryDBVersion4(db, "vi");
        insertCountryDBVersion4(db, "ug");
        insertCountryDBVersion4(db, "ua");
        insertCountryDBVersion4(db, "ae");
        insertCountryDBVersion4(db, "gb");
        insertCountryDBVersion4(db, "us");
        insertCountryDBVersion4(db, "um");
        insertCountryDBVersion4(db, "uy");
        insertCountryDBVersion4(db, "uz");

        insertCountryDBVersion4(db, "vu");
        insertCountryDBVersion4(db, "va");
        insertCountryDBVersion4(db, "ve");
        insertCountryDBVersion4(db, "vn");
        insertCountryDBVersion4(db, "wf");
        insertCountryDBVersion4(db, "eh");
        insertCountryDBVersion4(db, "ye");
        insertCountryDBVersion4(db, "zm");
        insertCountryDBVersion4(db, "zw");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertCountryDBVersion4(SQLiteDatabase db, String countryCode) {
        ContentValues values = new ContentValues();
        values.put("code", countryCode);

        db.insert("countries", null, values);
    }

    private void createTimeZonesTableDBVersion4(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL("CREATE TABLE time_zones (" +
            "id         INTEGER PRIMARY KEY, " +
            "code       TEXT NOT NULL, " +
            "country_id INTEGER NOT NULL, " +

            "UNIQUE (code), " +
            "FOREIGN KEY (country_id) REFERENCES countries(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertAllTimeZonesDBVersion4(SQLiteDatabase db) {
        db.beginTransaction();

        insertTimeZoneDBVersion4(db, "af", "Asia/Kabul");
        insertTimeZoneDBVersion4(db, "ax", "Europe/Mariehamn");
        insertTimeZoneDBVersion4(db, "al", "Europe/Tirane");
        insertTimeZoneDBVersion4(db, "dz", "Africa/Algiers");
        insertTimeZoneDBVersion4(db, "as", "Pacific/Pago_Pago");
        insertTimeZoneDBVersion4(db, "ad", "Europe/Andorra");
        insertTimeZoneDBVersion4(db, "ao", "Africa/Luanda");
        insertTimeZoneDBVersion4(db, "ai", "America/Anguilla");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Casey");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Davis");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/DumontDUrville");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Mawson");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/McMurdo");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Palmer");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Rothera");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Syowa");
        insertTimeZoneDBVersion4(db, "aq", "Antarctica/Vostok");
        insertTimeZoneDBVersion4(db, "ag", "America/Antigua");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Buenos_Aires");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Catamarca");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Cordoba");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Jujuy");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/La_Rioja");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Mendoza");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Rio_Gallegos");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Salta");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/San_Juan");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/San_Luis");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Tucuman");
        insertTimeZoneDBVersion4(db, "ar", "America/Argentina/Ushuaia");
        insertTimeZoneDBVersion4(db, "am", "Asia/Yerevan");
        insertTimeZoneDBVersion4(db, "aw", "America/Aruba");
        insertTimeZoneDBVersion4(db, "au", "Antarctica/Macquarie");
        insertTimeZoneDBVersion4(db, "au", "Australia/Adelaide");
        insertTimeZoneDBVersion4(db, "au", "Australia/Brisbane");
        insertTimeZoneDBVersion4(db, "au", "Australia/Broken_Hill");
        insertTimeZoneDBVersion4(db, "au", "Australia/Currie");
        insertTimeZoneDBVersion4(db, "au", "Australia/Darwin");
        insertTimeZoneDBVersion4(db, "au", "Australia/Eucla");
        insertTimeZoneDBVersion4(db, "au", "Australia/Hobart");
        insertTimeZoneDBVersion4(db, "au", "Australia/Lindeman");
        insertTimeZoneDBVersion4(db, "au", "Australia/Lord_Howe");
        insertTimeZoneDBVersion4(db, "au", "Australia/Melbourne");
        insertTimeZoneDBVersion4(db, "au", "Australia/Perth");
        insertTimeZoneDBVersion4(db, "au", "Australia/Sydney");
        insertTimeZoneDBVersion4(db, "at", "Europe/Vienna");
        insertTimeZoneDBVersion4(db, "az", "Asia/Baku");

        insertTimeZoneDBVersion4(db, "bs", "America/Nassau");
        insertTimeZoneDBVersion4(db, "bh", "Asia/Bahrain");
        insertTimeZoneDBVersion4(db, "bd", "Asia/Dhaka");
        insertTimeZoneDBVersion4(db, "bb", "America/Barbados");
        insertTimeZoneDBVersion4(db, "by", "Europe/Minsk");
        insertTimeZoneDBVersion4(db, "be", "Europe/Brussels");
        insertTimeZoneDBVersion4(db, "bz", "America/Belize");
        insertTimeZoneDBVersion4(db, "bj", "Africa/Porto-Novo");
        insertTimeZoneDBVersion4(db, "bm", "Atlantic/Bermuda");
        insertTimeZoneDBVersion4(db, "bt", "Asia/Thimphu");
        insertTimeZoneDBVersion4(db, "bo", "America/La_Paz");
        insertTimeZoneDBVersion4(db, "bq", "America/Kralendijk");
        insertTimeZoneDBVersion4(db, "ba", "Europe/Sarajevo");
        insertTimeZoneDBVersion4(db, "bw", "Africa/Gaborone");
        insertTimeZoneDBVersion4(db, "br", "America/Araguaina");
        insertTimeZoneDBVersion4(db, "br", "America/Bahia");
        insertTimeZoneDBVersion4(db, "br", "America/Belem");
        insertTimeZoneDBVersion4(db, "br", "America/Boa_Vista");
        insertTimeZoneDBVersion4(db, "br", "America/Campo_Grande");
        insertTimeZoneDBVersion4(db, "br", "America/Cuiaba");
        insertTimeZoneDBVersion4(db, "br", "America/Eirunepe");
        insertTimeZoneDBVersion4(db, "br", "America/Fortaleza");
        insertTimeZoneDBVersion4(db, "br", "America/Maceio");
        insertTimeZoneDBVersion4(db, "br", "America/Manaus");
        insertTimeZoneDBVersion4(db, "br", "America/Noronha");
        insertTimeZoneDBVersion4(db, "br", "America/Porto_Velho");
        insertTimeZoneDBVersion4(db, "br", "America/Recife");
        insertTimeZoneDBVersion4(db, "br", "America/Rio_Branco");
        insertTimeZoneDBVersion4(db, "br", "America/Santarem");
        insertTimeZoneDBVersion4(db, "br", "America/Sao_Paulo");

        insertTimeZoneDBVersion4(db, "io", "Indian/Chagos");
        insertTimeZoneDBVersion4(db, "vg", "America/Tortola");
        insertTimeZoneDBVersion4(db, "bn", "Asia/Brunei");
        insertTimeZoneDBVersion4(db, "bg", "Europe/Sofia");
        insertTimeZoneDBVersion4(db, "bf", "Africa/Ouagadougou");
        insertTimeZoneDBVersion4(db, "bi", "Africa/Bujumbura");

        insertTimeZoneDBVersion4(db, "kh", "Asia/Phnom_Penh");
        insertTimeZoneDBVersion4(db, "cm", "Africa/Douala");
        insertTimeZoneDBVersion4(db, "ca", "America/Atikokan");
        insertTimeZoneDBVersion4(db, "ca", "America/Blanc-Sablon");
        insertTimeZoneDBVersion4(db, "ca", "America/Cambridge_Bay");
        insertTimeZoneDBVersion4(db, "ca", "America/Creston");
        insertTimeZoneDBVersion4(db, "ca", "America/Dawson");
        insertTimeZoneDBVersion4(db, "ca", "America/Dawson_Creek");
        insertTimeZoneDBVersion4(db, "ca", "America/Edmonton");
        insertTimeZoneDBVersion4(db, "ca", "America/Glace_Bay");
        insertTimeZoneDBVersion4(db, "ca", "America/Goose_Bay");
        insertTimeZoneDBVersion4(db, "ca", "America/Halifax");
        insertTimeZoneDBVersion4(db, "ca", "America/Inuvik");
        insertTimeZoneDBVersion4(db, "ca", "America/Iqaluit");
        insertTimeZoneDBVersion4(db, "ca", "America/Moncton");
        insertTimeZoneDBVersion4(db, "ca", "America/Nipigon");
        insertTimeZoneDBVersion4(db, "ca", "America/Pangnirtung");
        insertTimeZoneDBVersion4(db, "ca", "America/Rainy_River");
        insertTimeZoneDBVersion4(db, "ca", "America/Rankin_Inlet");
        insertTimeZoneDBVersion4(db, "ca", "America/Regina");
        insertTimeZoneDBVersion4(db, "ca", "America/Resolute");
        insertTimeZoneDBVersion4(db, "ca", "America/St_Johns");
        insertTimeZoneDBVersion4(db, "ca", "America/Swift_Current");
        insertTimeZoneDBVersion4(db, "ca", "America/Thunder_Bay");
        insertTimeZoneDBVersion4(db, "ca", "America/Toronto");
        insertTimeZoneDBVersion4(db, "ca", "America/Vancouver");
        insertTimeZoneDBVersion4(db, "ca", "America/Whitehorse");
        insertTimeZoneDBVersion4(db, "ca", "America/Winnipeg");
        insertTimeZoneDBVersion4(db, "ca", "America/Yellowknife");
        insertTimeZoneDBVersion4(db, "cv", "Atlantic/Cape_Verde");
        insertTimeZoneDBVersion4(db, "ky", "American/Cayman");
        insertTimeZoneDBVersion4(db, "cf", "Africa/Bangui");
        insertTimeZoneDBVersion4(db, "td", "Africa/Ndjamena");
        insertTimeZoneDBVersion4(db, "cl", "America/Santiago");
        insertTimeZoneDBVersion4(db, "cl", "Pacific/Easter");
        insertTimeZoneDBVersion4(db, "cn", "Asia/Shanghai");
        insertTimeZoneDBVersion4(db, "cn", "Asia/Urumqi");
        insertTimeZoneDBVersion4(db, "cx", "Indian/Christmas");
        insertTimeZoneDBVersion4(db, "cc", "Indian/Cocos");
        insertTimeZoneDBVersion4(db, "co", "America/Bogota");
        insertTimeZoneDBVersion4(db, "km", "Indian/Comoro");
        insertTimeZoneDBVersion4(db, "cd", "Africa/Kinshasa");
        insertTimeZoneDBVersion4(db, "cd", "Africa/Lubumbashi");
        insertTimeZoneDBVersion4(db, "cg", "Africa/Brazzaville");
        insertTimeZoneDBVersion4(db, "ck", "Pacific/Rarotonga");
        insertTimeZoneDBVersion4(db, "cr", "America/Costa_Rica");
        insertTimeZoneDBVersion4(db, "ci", "Africa/Abidjan");
        insertTimeZoneDBVersion4(db, "hr", "Europe/Zagreb");
        insertTimeZoneDBVersion4(db, "cu", "America/Havana");
        insertTimeZoneDBVersion4(db, "cw", "America/Curacao");
        insertTimeZoneDBVersion4(db, "cy", "Asia/Nicosia");
        insertTimeZoneDBVersion4(db, "cz", "Europe/Prague");

        insertTimeZoneDBVersion4(db, "dk", "Europe/Copenhagen");
        insertTimeZoneDBVersion4(db, "dj", "Africa/Djibouti");
        insertTimeZoneDBVersion4(db, "dm", "America/Dominica");
        insertTimeZoneDBVersion4(db, "do", "America/Santo_Domingo");

        insertTimeZoneDBVersion4(db, "ec", "America/Guayaquil");
        insertTimeZoneDBVersion4(db, "ec", "Pacific/Galapagos");
        insertTimeZoneDBVersion4(db, "eg", "Africa/Cairo");
        insertTimeZoneDBVersion4(db, "sv", "America/El_Salvador");
        insertTimeZoneDBVersion4(db, "gq", "Africa/Malabo");
        insertTimeZoneDBVersion4(db, "er", "Africa/Asmara");
        insertTimeZoneDBVersion4(db, "ee", "Europe/Tallinn");
        insertTimeZoneDBVersion4(db, "et", "Africa/Addis_Ababa");

        insertTimeZoneDBVersion4(db, "fk", "Atlantic/Stanley");
        insertTimeZoneDBVersion4(db, "fo", "Atlantic/Faroe");
        insertTimeZoneDBVersion4(db, "fj", "Pacific/Fiji");
        insertTimeZoneDBVersion4(db, "fi", "Europe/Helsinki");
        insertTimeZoneDBVersion4(db, "fr", "Europe/Paris");
        insertTimeZoneDBVersion4(db, "gf", "America/Cayenne");
        insertTimeZoneDBVersion4(db, "pf", "Pacific/Gambier");
        insertTimeZoneDBVersion4(db, "pf", "Pacific/Marquesas");
        insertTimeZoneDBVersion4(db, "pf", "Pacific/Tahiti");
        insertTimeZoneDBVersion4(db, "tf", "Indian/Kerguelen");

        insertTimeZoneDBVersion4(db, "ga", "Africa/Libreville");
        insertTimeZoneDBVersion4(db, "gm", "Africa/Banjul");
        insertTimeZoneDBVersion4(db, "ge", "Asia/Tbilisi");
        insertTimeZoneDBVersion4(db, "de", "Europe/Berlin");
        insertTimeZoneDBVersion4(db, "de", "Europe/Busingen");
        insertTimeZoneDBVersion4(db, "gh", "Africa/Accra");
        insertTimeZoneDBVersion4(db, "gi", "Europe/Gibraltar");
        insertTimeZoneDBVersion4(db, "gr", "Europe/Athens");
        insertTimeZoneDBVersion4(db, "gl", "America/Danmarkshavn");
        insertTimeZoneDBVersion4(db, "gl", "America/Godthab");
        insertTimeZoneDBVersion4(db, "gl", "America/Scoresbysund");
        insertTimeZoneDBVersion4(db, "gl", "America/Thule");
        insertTimeZoneDBVersion4(db, "gd", "America/Grenada");
        insertTimeZoneDBVersion4(db, "gp", "America/Guadeloupe");
        insertTimeZoneDBVersion4(db, "gu", "Pacific/Guam");
        insertTimeZoneDBVersion4(db, "gt", "America/Guatemala");
        insertTimeZoneDBVersion4(db, "gg", "Europe/Guernsey");
        insertTimeZoneDBVersion4(db, "gn", "Africa/Conakry");
        insertTimeZoneDBVersion4(db, "gw", "Africa/Bissau");
        insertTimeZoneDBVersion4(db, "gy", "America/Guyana");

        insertTimeZoneDBVersion4(db, "ht", "America/Port-au-Prince");
        insertTimeZoneDBVersion4(db, "hn", "America/Tegucigalpa");
        insertTimeZoneDBVersion4(db, "hk", "Asia/Hong_Kong");
        insertTimeZoneDBVersion4(db, "hu", "Europe/Budapest");

        insertTimeZoneDBVersion4(db, "is", "Atlantic/Reykjavik");
        insertTimeZoneDBVersion4(db, "in", "Asia/Kolkata");
        insertTimeZoneDBVersion4(db, "id", "Asia/Jakarta");
        insertTimeZoneDBVersion4(db, "id", "Asia/Jayapura");
        insertTimeZoneDBVersion4(db, "id", "Asia/Makassar");
        insertTimeZoneDBVersion4(db, "id", "Asia/Pontianak");
        insertTimeZoneDBVersion4(db, "ir", "Asia/Tehran");
        insertTimeZoneDBVersion4(db, "iq", "Asia/Baghdad");
        insertTimeZoneDBVersion4(db, "ie", "Europe/Dublin");
        insertTimeZoneDBVersion4(db, "im", "Europe/Isle_of_Man");
        insertTimeZoneDBVersion4(db, "il", "Asia/Jerusalem");
        insertTimeZoneDBVersion4(db, "it", "Europe/Rome");

        insertTimeZoneDBVersion4(db, "jm", "America/Jamaica");
        insertTimeZoneDBVersion4(db, "jp", "Asia/Tokyo");
        insertTimeZoneDBVersion4(db, "je", "Europe/Jersey");
        insertTimeZoneDBVersion4(db, "jo", "Asia/Amman");
        insertTimeZoneDBVersion4(db, "kz", "Asia/Almaty");
        insertTimeZoneDBVersion4(db, "kz", "Asia/Aqtau");
        insertTimeZoneDBVersion4(db, "kz", "Asia/Aqtobe");
        insertTimeZoneDBVersion4(db, "kz", "Asia/Oral");
        insertTimeZoneDBVersion4(db, "kz", "Asia/Qyzylorda");
        insertTimeZoneDBVersion4(db, "ke", "Africa/Nairobi");
        insertTimeZoneDBVersion4(db, "ki", "Pacific/Enderbury");
        insertTimeZoneDBVersion4(db, "ki", "Pacific/Kiritimati");
        insertTimeZoneDBVersion4(db, "ki", "Pacific/Tarawa");
        insertTimeZoneDBVersion4(db, "kw", "Asia/Kuwait");
        insertTimeZoneDBVersion4(db, "kg", "Asia/Bishkek");

        insertTimeZoneDBVersion4(db, "la", "Asia/Vientiane");
        insertTimeZoneDBVersion4(db, "lv", "Europe/Riga");
        insertTimeZoneDBVersion4(db, "lb", "Asia/Beirut");
        insertTimeZoneDBVersion4(db, "ls", "Africa/Maseru");
        insertTimeZoneDBVersion4(db, "lr", "Africa/Monrovia");
        insertTimeZoneDBVersion4(db, "ly", "Africa/Tripoli");
        insertTimeZoneDBVersion4(db, "li", "Europe/Vaduz");
        insertTimeZoneDBVersion4(db, "lt", "Europe/Vilnius");
        insertTimeZoneDBVersion4(db, "lu", "Europe/Luxembourg");

        insertTimeZoneDBVersion4(db, "mo", "Asia/Macau");
        insertTimeZoneDBVersion4(db, "mk", "Europe/Skopje");
        insertTimeZoneDBVersion4(db, "mg", "Indian/Antananarivo");
        insertTimeZoneDBVersion4(db, "mw", "Africa/Blantyre");
        insertTimeZoneDBVersion4(db, "my", "Asia/Kuala_Lumpur");
        insertTimeZoneDBVersion4(db, "my", "Asia/Kuching");
        insertTimeZoneDBVersion4(db, "mv", "Indian/Maldives");
        insertTimeZoneDBVersion4(db, "ml", "Africa/Bamako");
        insertTimeZoneDBVersion4(db, "mt", "Europe/Malta");
        insertTimeZoneDBVersion4(db, "mh", "Pacific/Kwajalein");
        insertTimeZoneDBVersion4(db, "mh", "Pacific/Majuro");
        insertTimeZoneDBVersion4(db, "mq", "America/Martinique");
        insertTimeZoneDBVersion4(db, "mr", "Africa/Nouakchott");
        insertTimeZoneDBVersion4(db, "mu", "Indian/Mauritius");
        insertTimeZoneDBVersion4(db, "yt", "Indian/Mayotte");
        insertTimeZoneDBVersion4(db, "mx", "America/Bahia_Banderas");
        insertTimeZoneDBVersion4(db, "mx", "America/Cancun");
        insertTimeZoneDBVersion4(db, "mx", "America/Chihuahua");
        insertTimeZoneDBVersion4(db, "mx", "America/Hermosillo");
        insertTimeZoneDBVersion4(db, "mx", "America/Matamoros");
        insertTimeZoneDBVersion4(db, "mx", "America/Mazatlan");
        insertTimeZoneDBVersion4(db, "mx", "America/Merida");
        insertTimeZoneDBVersion4(db, "mx", "America/Mexico_City");
        insertTimeZoneDBVersion4(db, "mx", "America/Monterrey");
        insertTimeZoneDBVersion4(db, "mx", "America/Ojinaga");
        insertTimeZoneDBVersion4(db, "mx", "America/Tijuana");
        insertTimeZoneDBVersion4(db, "fm", "Pacific/Chuuk");
        insertTimeZoneDBVersion4(db, "fm", "Pacific/Kosrae");
        insertTimeZoneDBVersion4(db, "fm", "Pacific/Pohnpei");
        insertTimeZoneDBVersion4(db, "md", "Europe/Chisinau");
        insertTimeZoneDBVersion4(db, "mc", "Europe/Monaco");
        insertTimeZoneDBVersion4(db, "mn", "Asia/Choibalsan");
        insertTimeZoneDBVersion4(db, "mn", "Asia/Hovd");
        insertTimeZoneDBVersion4(db, "mn", "Asia/Ulaanbaatar");
        insertTimeZoneDBVersion4(db, "me", "Europe/Podgorica");
        insertTimeZoneDBVersion4(db, "ms", "America/Montserrat");
        insertTimeZoneDBVersion4(db, "ma", "Africa/Casablanca");
        insertTimeZoneDBVersion4(db, "mz", "Africa/Maputo");
        insertTimeZoneDBVersion4(db, "mm", "Asia/Rangoon");

        insertTimeZoneDBVersion4(db, "na", "Africa/Windhoek");
        insertTimeZoneDBVersion4(db, "nr", "Pacific/Nauru");
        insertTimeZoneDBVersion4(db, "np", "Asia/Kathmandu");
        insertTimeZoneDBVersion4(db, "nl", "Europe/Amsterdam");
        insertTimeZoneDBVersion4(db, "nc", "Pacific/Noumea");
        insertTimeZoneDBVersion4(db, "nz", "Pacific/Auckland");
        insertTimeZoneDBVersion4(db, "nz", "Pacific/Chatham");
        insertTimeZoneDBVersion4(db, "ni", "America/Managua");
        insertTimeZoneDBVersion4(db, "ne", "Africa/Niamey");
        insertTimeZoneDBVersion4(db, "ng", "Africa/Lagos");
        insertTimeZoneDBVersion4(db, "nu", "Pacific/Niue");
        insertTimeZoneDBVersion4(db, "nf", "Pacific/Norfolk");
        insertTimeZoneDBVersion4(db, "kp", "Asia/Pyongyang");
        insertTimeZoneDBVersion4(db, "mp", "Pacific/Saipan");
        insertTimeZoneDBVersion4(db, "no", "Europe/Oslo");

        insertTimeZoneDBVersion4(db, "om", "Asia/Muscat");

        insertTimeZoneDBVersion4(db, "pk", "Asia/Karachi");
        insertTimeZoneDBVersion4(db, "pw", "Pacific/Palau");
        insertTimeZoneDBVersion4(db, "ps", "Asia/Gaza");
        insertTimeZoneDBVersion4(db, "ps", "Asia/Hebron");
        insertTimeZoneDBVersion4(db, "pa", "America/Panama");
        insertTimeZoneDBVersion4(db, "pg", "Pacific/Port_Moresby");
        insertTimeZoneDBVersion4(db, "py", "America/Asuncion");
        insertTimeZoneDBVersion4(db, "pe", "America/Lima");
        insertTimeZoneDBVersion4(db, "ph", "Asia/Manila");
        insertTimeZoneDBVersion4(db, "pn", "Pacific/Pitcairn");
        insertTimeZoneDBVersion4(db, "pl", "Europe/Warsaw");
        insertTimeZoneDBVersion4(db, "pt", "Atlantic/Azores");
        insertTimeZoneDBVersion4(db, "pt", "Atlantic/Madeira");
        insertTimeZoneDBVersion4(db, "pt", "Europe/Lisbon");
        insertTimeZoneDBVersion4(db, "pr", "America/Puerto_Rico");

        insertTimeZoneDBVersion4(db, "qa", "Asia/Qatar");

        insertTimeZoneDBVersion4(db, "re", "Indian/Reunion");
        insertTimeZoneDBVersion4(db, "ro", "Europe/Bucharest");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Anadyr");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Irkutsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Kamchatka");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Khandyga");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Krasnoyarsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Magadan");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Novokuznetsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Novosibirsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Omsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Sakhalin");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Ust-Nera");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Vladivostok");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Yakutsk");
        insertTimeZoneDBVersion4(db, "ru", "Asia/Yekaterinburg");
        insertTimeZoneDBVersion4(db, "ru", "Europe/Kaliningrad");
        insertTimeZoneDBVersion4(db, "ru", "Europe/Moscow");
        insertTimeZoneDBVersion4(db, "ru", "Europe/Samara");
        insertTimeZoneDBVersion4(db, "ru", "Europe/Simferopol");
        insertTimeZoneDBVersion4(db, "ru", "Europe/Volgograd");
        insertTimeZoneDBVersion4(db, "rw", "Africa/Kigali");
        insertTimeZoneDBVersion4(db, "bl", "America/St_Barthelemy");
        insertTimeZoneDBVersion4(db, "sh", "Atlantic/St_Helena");
        insertTimeZoneDBVersion4(db, "kn", "America/St_Kitts");
        insertTimeZoneDBVersion4(db, "lc", "America/St_Lucia");
        insertTimeZoneDBVersion4(db, "mf", "America/Marigot");
        insertTimeZoneDBVersion4(db, "pm", "America/Miquelon");
        insertTimeZoneDBVersion4(db, "vc", "America/St_Vincent");
        insertTimeZoneDBVersion4(db, "ws", "Pacific/Apia");
        insertTimeZoneDBVersion4(db, "sm", "Europe/San_Marino");
        insertTimeZoneDBVersion4(db, "st", "Africa/Sao_Tome");
        insertTimeZoneDBVersion4(db, "sa", "Asia/Riyadh");
        insertTimeZoneDBVersion4(db, "sn", "Africa/Dakar");
        insertTimeZoneDBVersion4(db, "rs", "Europe/Belgrade");
        insertTimeZoneDBVersion4(db, "sc", "Indian/Mahe");
        insertTimeZoneDBVersion4(db, "sl", "Africa/Freetown");
        insertTimeZoneDBVersion4(db, "sg", "Asia/Singapore");
        insertTimeZoneDBVersion4(db, "sx", "America/Lower_Princes");
        insertTimeZoneDBVersion4(db, "sk", "Europe/Bratislava");
        insertTimeZoneDBVersion4(db, "si", "Europe/Ljubljana");
        insertTimeZoneDBVersion4(db, "sb", "Pacific/Guadalcanal");
        insertTimeZoneDBVersion4(db, "so", "Africa/Mogadishu");
        insertTimeZoneDBVersion4(db, "za", "Africa/Johannesburg");
        insertTimeZoneDBVersion4(db, "gs", "Atlantic/South_Georgia");
        insertTimeZoneDBVersion4(db, "kr", "Asia/Seoul");
        insertTimeZoneDBVersion4(db, "ss", "Africa/Juba");
        insertTimeZoneDBVersion4(db, "es", "Africa/Ceuta");
        insertTimeZoneDBVersion4(db, "es", "Atlantic/Canary");
        insertTimeZoneDBVersion4(db, "es", "Europe/Madrid");
        insertTimeZoneDBVersion4(db, "lk", "Asia/Colombo");
        insertTimeZoneDBVersion4(db, "sd", "Africa/Khartoum");
        insertTimeZoneDBVersion4(db, "sr", "America/Paramaribo");
        insertTimeZoneDBVersion4(db, "sj", "Arctic/Longyearbyen");
        insertTimeZoneDBVersion4(db, "sz", "Africa/Mbabane");
        insertTimeZoneDBVersion4(db, "se", "Europe/Stockholm");
        insertTimeZoneDBVersion4(db, "ch", "Europe/Zurich");
        insertTimeZoneDBVersion4(db, "sy", "Asia/Damascus");

        insertTimeZoneDBVersion4(db, "tw", "Asia/Taipei");
        insertTimeZoneDBVersion4(db, "tj", "Asia/Dushanbe");
        insertTimeZoneDBVersion4(db, "tz", "Africa/Dar_es_Salaam");
        insertTimeZoneDBVersion4(db, "th", "Asia/Bangkok");
        insertTimeZoneDBVersion4(db, "tl", "Asia/Dili");
        insertTimeZoneDBVersion4(db, "tg", "Africa/Lome");
        insertTimeZoneDBVersion4(db, "tk", "Pacific/Fakaofo");
        insertTimeZoneDBVersion4(db, "to", "Pacific/Tongatapu");
        insertTimeZoneDBVersion4(db, "tt", "America/Port_of_Spain");
        insertTimeZoneDBVersion4(db, "tn", "Africa/Tunis");
        insertTimeZoneDBVersion4(db, "tr", "Europe/Istanbul");
        insertTimeZoneDBVersion4(db, "tm", "Asia/Ashgabat");
        insertTimeZoneDBVersion4(db, "tc", "America/Grand_Turk");
        insertTimeZoneDBVersion4(db, "tv", "Pacific/Funafuti");

        insertTimeZoneDBVersion4(db, "vi", "America/St_Thomas");
        insertTimeZoneDBVersion4(db, "ug", "Africa/Kampala");
        insertTimeZoneDBVersion4(db, "ua", "Europe/Kiev");
        insertTimeZoneDBVersion4(db, "ua", "Europe/Uzhgorod");
        insertTimeZoneDBVersion4(db, "ua", "Europe/Zaporozhye");
        insertTimeZoneDBVersion4(db, "ae", "Asia/Dubai");
        insertTimeZoneDBVersion4(db, "gb", "Europe/London");
        insertTimeZoneDBVersion4(db, "us", "America/Adak");
        insertTimeZoneDBVersion4(db, "us", "America/Anchorage");
        insertTimeZoneDBVersion4(db, "us", "America/Boise");
        insertTimeZoneDBVersion4(db, "us", "America/Chicago");
        insertTimeZoneDBVersion4(db, "us", "America/Denver");
        insertTimeZoneDBVersion4(db, "us", "America/Detroit");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Indianapolis");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Knox");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Marengo");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Petersburg");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Tell_City");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Vevay");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Vincennes");
        insertTimeZoneDBVersion4(db, "us", "America/Indiana/Winamac");
        insertTimeZoneDBVersion4(db, "us", "America/Juneau");
        insertTimeZoneDBVersion4(db, "us", "America/Kentucky/Louisville");
        insertTimeZoneDBVersion4(db, "us", "America/Kentucky/Monticello");
        insertTimeZoneDBVersion4(db, "us", "America/Los_Angeles");
        insertTimeZoneDBVersion4(db, "us", "America/Menominee");
        insertTimeZoneDBVersion4(db, "us", "America/Metlakatla");
        insertTimeZoneDBVersion4(db, "us", "America/New_York");
        insertTimeZoneDBVersion4(db, "us", "America/Nome");
        insertTimeZoneDBVersion4(db, "us", "America/North_Dakota/Beulah");
        insertTimeZoneDBVersion4(db, "us", "America/North_Dakota/Center");
        insertTimeZoneDBVersion4(db, "us", "America/North_Dakota/New_Salem");
        insertTimeZoneDBVersion4(db, "us", "America/Phoenix");
        insertTimeZoneDBVersion4(db, "us", "America/Sitka");
        insertTimeZoneDBVersion4(db, "us", "America/Yakutat");
        insertTimeZoneDBVersion4(db, "us", "Pacific/Honolulu");
        insertTimeZoneDBVersion4(db, "um", "Pacific/Midway");
        insertTimeZoneDBVersion4(db, "um", "Pacific/Wake");
        insertTimeZoneDBVersion4(db, "uy", "America/Montevideo");
        insertTimeZoneDBVersion4(db, "uz", "Asia/Samarkand");
        insertTimeZoneDBVersion4(db, "uz", "Asia/Tashkent");

        insertTimeZoneDBVersion4(db, "vu", "Pacific/Efate");
        insertTimeZoneDBVersion4(db, "va", "Europe/Vatican");
        insertTimeZoneDBVersion4(db, "ve", "America/Caracas");
        insertTimeZoneDBVersion4(db, "vn", "Asia/Ho_Chi_Minh");
        insertTimeZoneDBVersion4(db, "wf", "Pacific/Wallis");
        insertTimeZoneDBVersion4(db, "eh", "Africa/El_Aaiun");
        insertTimeZoneDBVersion4(db, "ye", "Asia/Aden");
        insertTimeZoneDBVersion4(db, "zm", "Africa/Lusaka");
        insertTimeZoneDBVersion4(db, "zw", "Africa/Harare");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void insertTimeZoneDBVersion4(SQLiteDatabase db, String countryCode, String timeZoneCode) {
        ContentValues values = new ContentValues();
        values.put("code", timeZoneCode);
        values.put("country_id", getCountryId(db, countryCode));

        db.insert("time_zones", null, values);
    }

    private long getCountryId(SQLiteDatabase db, String countryCode) {
        long id = -1;

        if (countryCode != null && !countryCode.equals("")) {
            Cursor cursor = db.rawQuery("SELECT id FROM countries WHERE code = ?",
                new String[]{String.valueOf(countryCode)});

            if (cursor.moveToFirst()) id = cursor.getLong(0);

            cursor.close();
        }

        return id;
    }

    private void updateTasksTableDBVersion4(SQLiteDatabase db) {
        db.beginTransaction();

        // Copy the current Tasks table into a new temporary table
        db.execSQL("CREATE TEMPORARY TABLE tasks_temp (" +
            "id                      INTEGER PRIMARY KEY, " +
            "task_context_id         INTEGER NOT NULL, " +
            "title                   TEXT NOT NULL, " +
            "description             TEXT, " +

            "when_datetime           INTEGER, " +
            "ignore_when_time        INTEGER NOT NULL DEFAULT 0, " +
            "when_time_zone_code     TEXT, " +
            "when_reminder_id        INTEGER, " +

            "start_datetime          INTEGER, " +
            "ignore_start_time       INTEGER NOT NULL DEFAULT 0, " +
            "start_time_zone_code    TEXT, " +
            "start_reminder_id       INTEGER, " +

            "deadline_datetime       INTEGER, " +
            "ignore_deadline_time    INTEGER NOT NULL DEFAULT 0, " +
            "deadline_time_zone_code TEXT, " +
            "deadline_reminder_id    NTEGER, " +

            "done                    INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (when_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (start_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (deadline_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL" +
            ");");

        db.execSQL("INSERT INTO tasks_temp (id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done) " +
            "SELECT id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_reminder_id, " +
                "done " +
            "FROM tasks;");

        // Delete the current Tasks table
        db.execSQL("DROP TABLE tasks;");

        // Create again the Tasks table, with its new structure
        db.execSQL("CREATE TABLE tasks (" +
            "id                      INTEGER PRIMARY KEY, " +
            "task_context_id         INTEGER NOT NULL, " +
            "title                   TEXT NOT NULL, " +
            "description             TEXT, " +

            "when_datetime           INTEGER, " +
            "ignore_when_time        INTEGER NOT NULL DEFAULT 0, " +
            "when_time_zone_code     TEXT, " +
            "when_reminder_id        INTEGER, " +

            "start_datetime          INTEGER, " +
            "ignore_start_time       INTEGER NOT NULL DEFAULT 0, " +
            "start_time_zone_code    TEXT, " +
            "start_reminder_id       INTEGER, " +

            "deadline_datetime       INTEGER, " +
            "ignore_deadline_time    INTEGER NOT NULL DEFAULT 0, " +
            "deadline_time_zone_code TEXT, " +
            "deadline_reminder_id    INTEGER, " +

            "done                 INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (task_context_id) REFERENCES task_contexts(id) ON UPDATE CASCADE ON DELETE CASCADE, " +
            "FOREIGN KEY (when_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (start_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL, " +
            "FOREIGN KEY (deadline_reminder_id) REFERENCES task_reminders(id) ON UPDATE CASCADE ON DELETE SET NULL" +
            ");");

        // Time zones are null, so there is no need to insert them, but we do it for clarity.
        db.execSQL("INSERT INTO tasks (id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_time_zone_code, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_time_zone_code, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_time_zone_code, deadline_reminder_id, " +
                "done) " +
            "SELECT id, task_context_id, title, description, " +
                "when_datetime, ignore_when_time, when_time_zone_code, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_time_zone_code, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_time_zone_code, deadline_reminder_id, " +
                "done " +
            "FROM tasks_temp;");

        // Delete the temporal Tasks table
        db.execSQL("DROP TABLE tasks_temp;");

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) createDBVersion2(db);
        if (oldVersion < 3) createDBVersion3(db);
        if (oldVersion < 4) createDBVersion4(db);
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
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_time_zone_code, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_time_zone_code, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_time_zone_code, tasks.deadline_reminder_id, " +
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
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_time_zone_code, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_time_zone_code, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_time_zone_code, tasks.deadline_reminder_id, " +
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
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_time_zone_code, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_time_zone_code, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_time_zone_code, tasks.deadline_reminder_id, " +
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
                    "tasks.when_datetime, tasks.ignore_when_time, tasks.when_time_zone_code, tasks.when_reminder_id, " +
                    "tasks.start_datetime, tasks.ignore_start_time, tasks.start_time_zone_code, tasks.start_reminder_id, " +
                    "tasks.deadline_datetime, tasks.ignore_deadline_time, tasks.deadline_time_zone_code, tasks.deadline_reminder_id, " +
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
                "when_datetime, ignore_when_time, when_time_zone_code, when_reminder_id, " +
                "start_datetime, ignore_start_time, start_time_zone_code, start_reminder_id, " +
                "deadline_datetime, ignore_deadline_time, deadline_time_zone_code, deadline_reminder_id, " +
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

                String whenTimeZoneCode = null;
                if (!cursor.isNull(5)) {
                    whenTimeZoneCode = cursor.getString(5);
                    when.setTimeZone(TimeZone.getTimeZone(whenTimeZoneCode));
                }

                TaskReminder whenReminder = null;
                if (!cursor.isNull(6)) {
                    long whenReminderId = cursor.getLong(6);
                    whenReminder = getTaskReminder(db, whenReminderId);
                }

                Calendar start = null;
                if (!cursor.isNull(7)) {
                    start = Calendar.getInstance();
                    start.setTimeInMillis(cursor.getLong(7));
                }

                boolean ignoreStartTime = false;
                if (!cursor.isNull(8)) ignoreStartTime = (cursor.getLong(8) != 0);

                String startTimeZoneCode = null;
                if (!cursor.isNull(9)) {
                    startTimeZoneCode = cursor.getString(9);
                    start.setTimeZone(TimeZone.getTimeZone(startTimeZoneCode));
                }

                TaskReminder startReminder = null;
                if (!cursor.isNull(10)) {
                    long startReminderId = cursor.getLong(10);
                    startReminder = getTaskReminder(db, startReminderId);
                }

                Calendar deadline = null;
                if (!cursor.isNull(11)) {
                    deadline = Calendar.getInstance();
                    deadline.setTimeInMillis(cursor.getLong(11));
                }

                boolean ignoreDeadlineTime = false;
                if (!cursor.isNull(12)) ignoreDeadlineTime = (cursor.getLong(12) != 0);

                String deadlineTimeZoneCode = null;
                if (!cursor.isNull(13)) {
                    deadlineTimeZoneCode = cursor.getString(13);
                    deadline.setTimeZone(TimeZone.getTimeZone(deadlineTimeZoneCode));
                }

                TaskReminder deadlineReminder = null;
                if (!cursor.isNull(14)) {
                    long deadlineReminderId = cursor.getLong(14);
                    deadlineReminder = getTaskReminder(db, deadlineReminderId);
                }

                boolean done = (cursor.getLong(15) != 0);
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

        if (when != null) values.put("when_time_zone_code", (when.getTimeZone()).getID());

        TaskReminder whenReminder = task.getWhenReminder();
        if (whenReminder != null) values.put("when_reminder_id", whenReminder.getId());
        else values.putNull("when_reminder_id");

        // Start
        Calendar start = task.getStart();
        if (start != null) values.put("start_datetime", start.getTimeInMillis());
        else values.putNull("start_datetime");

        if (task.getIgnoreStartTime()) values.put("ignore_start_time", 1);
        else values.put("ignore_start_time", 0);

        if (start != null) values.put("start_time_zone_code", (start.getTimeZone()).getID());

        TaskReminder startReminder = task.getStartReminder();
        if (startReminder != null) values.put("start_reminder_id", startReminder.getId());
        else values.putNull("start_reminder_id");

        // Deadline
        Calendar deadline = task.getDeadline();
        if (deadline != null) values.put("deadline_datetime", deadline.getTimeInMillis());
        else values.putNull("deadline_datetime");

        if (task.getIgnoreDeadlineTime()) values.put("ignore_deadline_time", 1);
        else values.put("ignore_deadline_time", 0);

        if (deadline != null) values.put("deadline_time_zone_code", (deadline.getTimeZone()).getID());

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

    public List<Country> getAllCountries() {
        List<Country> countries = new LinkedList<Country>();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT id, code FROM countries", null);

            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String code = cursor.getString(1);

                    countries.add(new Country(id, code));
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            if (db != null) db.close();
        }

        return countries;
    }

    public Country getCountry(TimeZone timeZone) {
        if (timeZone == null) throw new NullPointerException("Time Zone is null.");

        Country country = null;
        SQLiteDatabase db = null;

        String timeZoneCode = timeZone.getID();

        try {
            db = getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT countries.id, countries.code " +
                "FROM countries INNER JOIN time_zones " +
                "ON countries.id = time_zones.country_id " +
                "WHERE time_zones.code = ?",
                    new String[] { String.valueOf(timeZoneCode) });

            if (cursor.moveToFirst()) {
                long countryId = cursor.getLong(0);
                String countryCode = cursor.getString(1);

                country = new Country(countryId, countryCode);
            }
        }
        finally {
            if (db != null) db.close();
        }

        return country;
    }

    public List<String> getTimeZoneCodes(Country country) {
        if (country == null) throw new NullPointerException("Country is null.");

        List<String> timeZoneCodes = new LinkedList<String>();

        long countryId = country.getId();
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT code " +
                "FROM time_zones " +
                "WHERE country_id = ?", new String[] { String.valueOf(countryId) });

            if (cursor.moveToFirst()) {
                do {
                    String code = cursor.getString(0);
                    timeZoneCodes.add(code);
                }
                while (cursor.moveToNext());
            }
        }
        finally {
            if (db != null) db.close();
        }

        return timeZoneCodes;
    }
}