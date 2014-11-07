package jajimenez.workpage.data;

import java.util.List;
import java.util.LinkedList;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import jajimenez.workpage.data.model.Workspace;

public class DataManager extends SQLiteOpenHelper {
    public static final String DB_NAME = "workpage.db";
    public static final int DB_VERSION = 1;

    public DataManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        db.execSQL("PRAGMA foreign_keys = ON;");

        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String workspacesTableSql = "CREATE TABLE workspaces (" +
            "id         INTEGER PRIMARY KEY, " +
            "name       TEXT NOT NULL, " +
            "list_order INTEGER NOT NULL DEFAULT 0" +
            ");";

        String taskTagsTableSql = "CREATE TABLE task_tags (" +
            "id           INTEGER PRIMARY KEY, " +
            "workspace_id INTEGER, " +
            "name         TEXT NOT NULL, " +
            "list_order   INTEGER NOT NULL DEFAULT 0, " +

            "FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON UPDATE CASCADE ON DELETE CASCADE" +
            ");";

        String tasksTableSql = "CREATE TABLE tasks (" +
            "id             INTEGER PRIMARY KEY, " +
            "workspace_id   INTEGER, " +
            "title          TEXT NOT NULL, " +
            "description    TEXT, " +
            "start_datetime TEXT, " +
            "end_datetime   TEXT, " +
            "done           INTEGER NOT NULL DEFAULT 0, " +
            "done_datetime  TEXT, " +

            "FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON UPDATE CASCADE ON DELETE CASCADE" +
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
            
        db.execSQL(workspacesTableSql);
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
        String workspacesTableSql            = "DROP TABLE IF EXISTS workspaces;";
        
        db.execSQL(tasksRequirementsTableSql);
        db.execSQL(subtasksTableSql);
        db.execSQL(taskTagsRelationshipsTableSql);
        db.execSQL(tasksTableSql);
        db.execSQL(taskTagsTableSql);
        db.execSQL(workspacesTableSql);

        onCreate(db);
    }

    public List<Workspace> getAllWorkspaces() {
        List<Workspace> workspaces = new LinkedList<Workspace>(); 
        SQLiteDatabase db = null;

        try {
            db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM workspaces ORDER BY list_order;", null);
            Workspace workspace = null;

            if (cursor.moveToFirst()) {
                do {
                    // New workspace object, setting its ID, Name and Order.
                    workspace = new Workspace(cursor.getLong(0), cursor.getString(1), cursor.getLong(2));
                    workspaces.add(workspace);
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        return workspaces;
    }

    // Creates or updates a workspace in the database.
    // If the ID of the workspace is less than 0, it
    // inserts a new row in the Workspaces table
    // ignoring that ID. Otherwise, it updates the row
    // of the given ID.
    public void saveWorkspace(Workspace workspace) {
        SQLiteDatabase db = getWritableDatabase();

        long id = workspace.getId();
        ContentValues values = new ContentValues();
        values.put("name", workspace.getName());
        values.put("list_order", workspace.getOrder());

        try {
            if (id < 0) {
                values.put("id", id);
                db.insert("workspaces", null, values);
            } else {
                db.update("workspaces", values, "id = ?", new String[] { String.valueOf(id) });
            }
        }
        finally {
            db.close();
        }
    }
}
