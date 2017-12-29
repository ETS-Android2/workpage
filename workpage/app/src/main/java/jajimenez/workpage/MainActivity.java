package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
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

import java.util.List;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, TaskListHostActivity {

    private ActionMode actionMode;
    private TextView viewFilterTextView;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private MenuItem interfaceModeMenuItem;
    private TaskListFragment listFragment;
    private TaskCalendarFragment calendarFragment;

    private boolean interfaceReady;

    // Broadcast receiver
    private AppBroadcastReceiver appBroadcastReceiver;

    // Listeners
    private DataImportConfirmationDialogFragment.OnDataImportConfirmationListener onDataImportConfirmationListener;

    private LoadTasksDBTask tasksDbTask = null;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private String viewStateFilter;
    private boolean includeTasksWithNoTag;
    private List<TaskTag> currentFilterTags;
    private int interfaceMode;

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

        interfaceModeMenuItem = (navigationView.getMenu()).findItem(R.id.main_nav_interface_mode);

        listFragment = (TaskListFragment) (getSupportFragmentManager()).findFragmentById(R.id.content_main_list);
        calendarFragment = (TaskCalendarFragment) (getSupportFragmentManager()).findFragmentById(R.id.content_main_calendar);

        // Listeners
        setDialogListeners();

        // Instance state
        if (savedInstanceState != null) resetDialogListeners();

        // Parameters
        applicationLogic = new ApplicationLogic(this);

        // Reminders
        applicationLogic.updateAllOpenTaskReminderAlarms(false);

        // User interface
        interfaceReady = false;
        updateInterface();

        // Broadcast receiver
        registerBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Broadcast receiver
        unregisterBroadcastReceiver();
    }

    private void setDialogListeners() {
        onDataImportConfirmationListener = new DataImportConfirmationDialogFragment.OnDataImportConfirmationListener() {
            public void onConfirmation(Uri input) {
                (new ImportDataTask()).execute(input);
            }
        };
    }

    private void registerBroadcastReceiver() {
        appBroadcastReceiver = new AppBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ApplicationLogic.ACTION_DATA_CHANGED);

        (LocalBroadcastManager.getInstance(this)).registerReceiver(appBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        (LocalBroadcastManager.getInstance(this)).unregisterReceiver(appBroadcastReceiver);
    }

    private void closeActionBar() {
        // Close the context action bar
        if (actionMode != null) actionMode.finish();
    }

    private void resetDialogListeners() {
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
        // Handle navigation view item clicks here
        if (!interfaceReady) return true;

        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.main_nav_context:
                SwitchTaskContextDialogFragment fragment = new SwitchTaskContextDialogFragment();
                fragment.show(getFragmentManager(), "switch_task_context");
                break;
            case R.id.main_nav_view:
                intent = new Intent(this, ViewActivity.class);
                startActivity(intent);
                break;
            case R.id.main_nav_edit_tags:
                intent = new Intent(this, EditTaskTagsActivity.class);
                startActivity(intent);
                break;
            case R.id.main_nav_interface_mode:
                SwitchInterfaceModeDialogFragment interfaceModeFragment = new SwitchInterfaceModeDialogFragment();
                interfaceModeFragment.show(getFragmentManager(), "switch_interface_mode");
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
            if (requestCode == ApplicationLogic.EXPORT_DATA) {
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

        startActivity(intent);
    }

    public void enableInterface() {
        listFragment.setEnabled(true);
        calendarFragment.setEnabled(true);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;
        listFragment.setEnabled(false);
        calendarFragment.setEnabled(false);
    }

    public void updateInterface() {
        // Information about the current task context
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        Menu drawerMenu = navigationView.getMenu();
        MenuItem contextItem = drawerMenu.findItem(R.id.main_nav_context);
        contextItem.setTitle(currentTaskContext.getName());

        // Information about the current view
        viewStateFilter = this.applicationLogic.getViewStateFilter();
        String stateFilterText;

        switch (viewStateFilter) {
            case "open":
                stateFilterText = getString(R.string.open_2);
                break;
            case "doable_today":
                stateFilterText = getString(R.string.doable_today_2);
                break;
            default:
                stateFilterText = getString(R.string.closed_2);
        }

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

        // Task mode (List or Calendar)
        interfaceMode = applicationLogic.getInterfaceMode();

        if (interfaceMode == ApplicationLogic.INTERFACE_MODE_CALENDAR) {
            // Drawer menu
            interfaceModeMenuItem.setTitle(R.string.calendar);
            interfaceModeMenuItem.setIcon(R.drawable.calendar_1);

            // Fragment
            listFragment.setVisible(false);
            calendarFragment.setVisible(true);
        } else {
            // Drawer menu
            interfaceModeMenuItem.setTitle(R.string.list);
            interfaceModeMenuItem.setIcon(R.drawable.list);

            // Fragment
            listFragment.setVisible(true);
            calendarFragment.setVisible(false);
            calendarFragment.setCurrentMonth();
        }

        // Get the tasks
        if (tasksDbTask == null || tasksDbTask.getStatus() == AsyncTask.Status.FINISHED) {
            tasksDbTask = new LoadTasksDBTask();
            tasksDbTask.execute();
        }
    }

    @Override
    public ActionMode getActionMode() {
        return actionMode;
    }

    @Override
    public void setActionMode(ActionMode mode) {
        actionMode = mode;
    }

    @Override
    public void onTaskClicked(Task task) {
        Intent intent = new Intent(MainActivity.this, TaskActivity.class);
        intent.putExtra("task_id", task.getId());

        startActivity(intent);
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
        statusFragment.show(getFragmentManager(), "change_task_status");
    }

    @Override
    public void showEditActivity(Task task) {
        long taskId = task.getId();

        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("mode", "edit");
        intent.putExtra("task_id", taskId);

        startActivity(intent);
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
        deleteFragment.show(getFragmentManager(), "delete_task");
    }

    private class AppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MainActivity.this.closeActionBar();
            String action = intent.getAction();

            if (action.equals(ApplicationLogic.ACTION_DATA_CHANGED)) {
                MainActivity.this.updateInterface();
            }
        }
    }

    private class LoadTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        protected void onPreExecute() {
            MainActivity.this.disableInterface();
        }

        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = null;

            switch (viewStateFilter) {
                case "open":
                    tasks = MainActivity.this.applicationLogic.getOpenTasksByTags(MainActivity.this.currentTaskContext,
                                MainActivity.this.includeTasksWithNoTag,
                                MainActivity.this.currentFilterTags);
                    break;
                case "doable_today":
                    tasks = MainActivity.this.applicationLogic.getDoableTodayTasksByTags(MainActivity.this.currentTaskContext,
                                MainActivity.this.includeTasksWithNoTag,
                                MainActivity.this.currentFilterTags);
                    break;
                default:
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

            // The returned value will be "false" if the operation
            // was successful or "true" if there was any error.
            return MainActivity.this.applicationLogic.exportData(output);
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
            return MainActivity.this.applicationLogic.importData(input); // Return result
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
        }
    }
}
