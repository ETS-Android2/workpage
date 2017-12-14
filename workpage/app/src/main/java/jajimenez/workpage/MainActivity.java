package jajimenez.workpage;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskHostActivity {

    private ActionMode actionMode;
    private TextView viewFilterTextView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TaskListFragment listFragment;

    private boolean interfaceReady;

    private SwitchTaskContextDialogFragment.OnTaskContextsChangedListener switchTaskContextListener;
    private ChangeTaskStatusDialogFragment.OnItemClickListener taskStatusChangeListener;
    private DeleteTaskDialogFragment.OnDeleteListener deleteTaskListener;
    private DataImportConfirmationDialogFragment.OnDataImportConfirmationListener onDataImportConfirmationListener;

    private LoadTasksDBTask tasksDbTask = null;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private String viewStateFilter;
    private boolean includeTasksWithNoTag;
    private List<TaskTag> currentFilterTags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = findViewById(R.id.app_bar_main_toolbar);
        setSupportActionBar(toolbar);

        actionMode = null;
        viewFilterTextView = findViewById(R.id.main_view_filter);

        // Navigation menu
        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listFragment = (TaskListFragment) (getSupportFragmentManager()).findFragmentById(R.id.content_main_list);

        // Listeners
        setDialogListeners();

        // Instance state
        if (savedInstanceState != null) resetDialogListeners();

        // Parameters
        applicationLogic = new ApplicationLogic(this);
        viewStateFilter = "";
        includeTasksWithNoTag = true;
        currentFilterTags = new LinkedList<TaskTag>();

        // Reminders
        applicationLogic.updateAllOpenTaskReminderAlarms(false);

        // User interface
        interfaceReady = false;
        updateInterface();
    }

    private void setDialogListeners() {
        switchTaskContextListener = new SwitchTaskContextDialogFragment.OnTaskContextsChangedListener() {
            public void onNewCurrentTaskContext() {
                MainActivity.this.updateInterface();
            }

            public void onEditTaskContextsSelected() {
                Intent intent = new Intent(MainActivity.this, EditTaskContextsActivity.class);
                startActivityForResult(intent, ApplicationLogic.CHANGE_TASK_CONTEXTS);
            }
        };

        taskStatusChangeListener = new ChangeTaskStatusDialogFragment.OnItemClickListener() {
            public void onItemClick() {
                MainActivity.this.closeActionBar();
            }
        };

        deleteTaskListener = new DeleteTaskDialogFragment.OnDeleteListener() {
            public void onDelete() {
                MainActivity.this.closeActionBar();
            }
        };

        onDataImportConfirmationListener = new DataImportConfirmationDialogFragment.OnDataImportConfirmationListener() {
            public void onConfirmation(Uri input) {
                (new ImportDataTask()).execute(input);
            }
        };
    }

    private void closeActionBar() {
        // Close the context action bar
        if (actionMode != null) actionMode.finish();

        // Update the list view
        updateInterface();
    }

    private void resetDialogListeners() {
        SwitchTaskContextDialogFragment switchTaskContextFragment = (SwitchTaskContextDialogFragment) (getFragmentManager()).findFragmentByTag("switch_task_context");
        if (switchTaskContextFragment != null) switchTaskContextFragment.setOnTaskContextsChangedListener(switchTaskContextListener);

        ChangeTaskStatusDialogFragment changeTaskStatusFragment = (ChangeTaskStatusDialogFragment) (getFragmentManager()).findFragmentByTag("change_task_status");
        if (changeTaskStatusFragment != null) changeTaskStatusFragment.setOnItemClickListener(taskStatusChangeListener);

        DeleteTaskDialogFragment deleteTaskFragment = (DeleteTaskDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task");
        if (deleteTaskFragment != null) deleteTaskFragment.setOnDeleteListener(deleteTaskListener);

        DataImportConfirmationDialogFragment importFragment = (DataImportConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("data_import_confirmation");
        if (importFragment != null) importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (!interfaceReady) return true;

        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.main_nav_context:
                SwitchTaskContextDialogFragment fragment = new SwitchTaskContextDialogFragment();
                fragment.setOnTaskContextsChangedListener(switchTaskContextListener);
                fragment.show(getFragmentManager(), "switch_task_context");
                break;
            case R.id.main_nav_view:
                intent = new Intent(this, ViewActivity.class);
                startActivityForResult(intent, ApplicationLogic.CHANGE_VIEW);
                break;
            case R.id.main_nav_edit_tags:
                intent = new Intent(this, EditTaskTagsActivity.class);
                startActivityForResult(intent, ApplicationLogic.CHANGE_TASK_TAGS);
                break;
            case R.id.main_nav_export_data:
                intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(ApplicationLogic.APP_MIME_TYPE);
                intent.putExtra(Intent.EXTRA_TITLE, ApplicationLogic.getProposedExportDataFileName());

                startActivityForResult(intent, ApplicationLogic.EXPORT_DATA);
                break;
            case R.id.main_nav_import_data:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(ApplicationLogic.APP_MIME_TYPE);

                startActivityForResult(intent, ApplicationLogic.IMPORT_DATA);
                break;
            case R.id.main_nav_about:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Bundle arguments;

        if (resultCode == RESULT_OK) {
            if (requestCode == ApplicationLogic.CHANGE_TASKS
                    || requestCode == ApplicationLogic.CHANGE_TASK_TAGS
                    || requestCode == ApplicationLogic.CHANGE_VIEW
                    || requestCode == ApplicationLogic.CHANGE_TASK_CONTEXTS) {

                updateInterface();
            }
            else if (requestCode == ApplicationLogic.EXPORT_DATA) {
                // Export data
                Uri output = resultData.getData();
                (new ExportDataTask()).execute(output);
            }
            else if (requestCode == ApplicationLogic.IMPORT_DATA) {
                // Import data
                Uri input = resultData.getData();

                DataImportConfirmationDialogFragment importFragment = new DataImportConfirmationDialogFragment();

                arguments = new Bundle();
                arguments.putString("input", input.toString());

                importFragment.setArguments(arguments);
                importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);
                importFragment.show(getFragmentManager(), "data_import_confirmation");
            }
        }
    }

    public void onAddClicked(View view) {
        if (!interfaceReady) return;

        // Close the context action bar
        if (actionMode != null) actionMode.finish();

        // Launch Edit Activity
        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
        intent.putExtra("mode", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);
    }

    public void enableInterface() {
        listFragment.setEnabled(true);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;
        listFragment.setEnabled(false);
    }

    public void updateInterface() {
        // Information about the current task context.
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        Menu drawerMenu = navigationView.getMenu();
        MenuItem contextItem = drawerMenu.findItem(R.id.main_nav_context);
        contextItem.setTitle(currentTaskContext.getName());

        // Information about the current view.
        viewStateFilter = this.applicationLogic.getViewStateFilter();
        String stateFilterText;

        if (viewStateFilter.equals("open")) stateFilterText = getString(R.string.open_2);
        else if (viewStateFilter.equals("doable_today")) stateFilterText = getString(R.string.doable_today_2);
        else stateFilterText = getString(R.string.closed_2);

        // Information about the current filter tags.
        includeTasksWithNoTag = this.applicationLogic.getIncludeTasksWithNoTag();
        currentFilterTags = this.applicationLogic.getCurrentFilterTags();

        int tagFilterCount = 0;
        int maxFilterCount = applicationLogic.getTaskTagCount(currentTaskContext) + 1; // + 1 for the Without Tag filter

        if (currentFilterTags != null) tagFilterCount = currentFilterTags.size();
        if (includeTasksWithNoTag) tagFilterCount++;

        if (tagFilterCount < maxFilterCount) {
            viewFilterTextView.setText(getString(R.string.view_filters, stateFilterText));
        } else {
            viewFilterTextView.setText(stateFilterText);
        }

        if (tasksDbTask == null || tasksDbTask.getStatus() == AsyncTask.Status.FINISHED) {
            tasksDbTask = new LoadTasksDBTask();
            tasksDbTask.execute();
        }
    }

    @Override
    public void setActionMode(ActionMode mode) {
        actionMode = mode;
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
        intent.putExtra("task_id", task.getId());

        startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);
    }

    @Override
    public void showChangeTaskStatusDialog(List<Task> tasks) {
        ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment();

        Bundle arguments = new Bundle();
        int selectedTaskCount = tasks.size();
        long[] taskIds = new long[selectedTaskCount];

        for (int i = 0; i < selectedTaskCount; i++) {
            Task t = tasks.get(i);
            taskIds[i] = t.getId();
        }

        arguments.putLongArray("task_ids", taskIds);
        statusFragment.setArguments(arguments);

        statusFragment.setOnItemClickListener(taskStatusChangeListener);
        statusFragment.show(getFragmentManager(), "change_task_status");
    }

    @Override
    public void showEditActivity(Task task) {
        long taskId = task.getId();

        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("task_id", taskId);

        startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);
    }

    @Override
    public void showDeleteTaskDialog(List<Task> tasks) {
        DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment();

        Bundle arguments = new Bundle();
        int selectedTaskCount = tasks.size();
        long[] taskIds = new long[selectedTaskCount];

        for (int i = 0; i < selectedTaskCount; i++) {
            Task t = tasks.get(i);
            taskIds[i] = t.getId();
        }

        arguments.putLongArray("task_ids", taskIds);
        deleteFragment.setArguments(arguments);

        deleteFragment.setOnDeleteListener(deleteTaskListener);
        deleteFragment.show(getFragmentManager(), "delete_task");
    }

    private class LoadTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        protected void onPreExecute() {
            MainActivity.this.disableInterface();
        }

        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = null;

            if (viewStateFilter.equals("open")) {
                tasks = MainActivity.this.applicationLogic.getOpenTasksByTags(MainActivity.this.currentTaskContext,
                    MainActivity.this.includeTasksWithNoTag,
                    MainActivity.this.currentFilterTags);
            }
            else if (viewStateFilter.equals("doable_today")) {
                tasks = MainActivity.this.applicationLogic.getDoableTodayTasksByTags(MainActivity.this.currentTaskContext,
                    MainActivity.this.includeTasksWithNoTag,
                    MainActivity.this.currentFilterTags);
            }
            else if (viewStateFilter.equals("closed")) {
                tasks = MainActivity.this.applicationLogic.getClosedTasksByTags(MainActivity.this.currentTaskContext,
                    MainActivity.this.includeTasksWithNoTag,
                    MainActivity.this.currentFilterTags);
            }

            return tasks;
        }

        protected void onPostExecute(List<Task> tasks) {
            MainActivity.this.listFragment.setTasks(tasks);
            MainActivity.this.enableInterface();
        }
    }

    private class ExportDataTask extends AsyncTask<Uri, Void, Boolean> {
        protected void onPreExecute() {
            MainActivity.this.disableInterface();
        }

        protected Boolean doInBackground(Uri... parameters) {
            Uri output = parameters[0];

            // "result" will be "false" if the operation was
            // successful or "true" if there was any error.
            boolean result = MainActivity.this.applicationLogic.exportData(output);

            return result;
        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                (Toast.makeText(MainActivity.this, R.string.export_error, Toast.LENGTH_SHORT)).show();
            }
            else {
                (Toast.makeText(MainActivity.this, R.string.export_success, Toast.LENGTH_SHORT)).show();
            }

            MainActivity.this.enableInterface();
        }
    }

    private class ImportDataTask extends AsyncTask<Uri, Void, Integer> {
        protected void onPreExecute() {
            MainActivity.this.disableInterface();
        }

        protected Integer doInBackground(Uri... parameters) {
            Uri input = parameters[0];
            int result = MainActivity.this.applicationLogic.importData(input);

            return result;
        }

        protected void onPostExecute(Integer result) {
            switch (result) {
                case ApplicationLogic.IMPORT_SUCCESS:
                    (Toast.makeText(MainActivity.this, R.string.import_success, Toast.LENGTH_SHORT)).show();
                    break;
                case ApplicationLogic.IMPORT_ERROR_OPENING_FILE:
                    (Toast.makeText(MainActivity.this, R.string.import_error_opening_file, Toast.LENGTH_SHORT)).show();
                    break;

                case ApplicationLogic.IMPORT_ERROR_FILE_NOT_COMPATIBLE:
                    (Toast.makeText(MainActivity.this, R.string.import_error_file_not_compatible, Toast.LENGTH_SHORT)).show();
                    break;

                case ApplicationLogic.IMPORT_ERROR_DATA_NOT_VALID:
                    (Toast.makeText(MainActivity.this, R.string.import_error_data_not_valid, Toast.LENGTH_SHORT)).show();
                    break;

                default:
                    (Toast.makeText(MainActivity.this, R.string.import_error_importing_data, Toast.LENGTH_SHORT)).show();
                    break;
            }

            MainActivity.this.updateInterface();
        }
    }
}
