package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

import org.json.JSONObject;

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
    private boolean isTablet;

    // Broadcast receiver
    private AppBroadcastReceiver appBroadcastReceiver;

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

        isTablet = (getResources()).getBoolean(R.bool.is_tablet);

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

        // Parameters
        applicationLogic = new ApplicationLogic(this);

        // Reminders
        applicationLogic.updateAllOpenTaskReminderAlarms(false);

        // User interface
        updateInterface();

        // Broadcast receiver
        registerBroadcastReceiver();

        // Clear any existing temporal import data
        applicationLogic.clearImportPreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Broadcast receiver
        unregisterBroadcastReceiver();
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here
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
                intent = new Intent(this, ExportDataSettingsActivity.class);
                startActivity(intent);

                break;
            case R.id.main_nav_import_data:
                intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(ApplicationLogic.APP_MIME_TYPE);

                startActivityForResult(intent, ApplicationLogic.IMPORT_DATA);
                break;
            case R.id.main_nav_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
        if (requestCode == ApplicationLogic.IMPORT_DATA && resultCode == RESULT_OK) {
            // Load data
            Uri input = resultData.getData();
            (new LoadDataTask()).execute(input);
        }
    }

    public void onAddClicked(View view) {
        // Close the context action bar
        if (actionMode != null) actionMode.finish();

        // Launch Edit Activity
        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
        intent.putExtra("mode", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivity(intent);
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

        FragmentManager manager = getSupportFragmentManager();
        Fragment listFrag = manager.findFragmentByTag("list");
        Fragment calendarFrag = manager.findFragmentByTag("calendar");

        if (isTablet) {
            interfaceModeMenuItem.setVisible(false);
        } else {
            interfaceModeMenuItem.setVisible(true);

            if (interfaceMode == ApplicationLogic.INTERFACE_MODE_CALENDAR) {
                // Drawer menu
                interfaceModeMenuItem.setTitle(R.string.calendar);
                interfaceModeMenuItem.setIcon(R.drawable.calendar_1);

                if (calendarFrag == null) {
                    FragmentTransaction t = manager.beginTransaction();
                    calendarFrag = new TaskCalendarFragment();

                    if (listFrag == null) {
                        t.add(R.id.content_main_container, calendarFrag, "calendar");
                    } else {
                        t.replace(R.id.content_main_container, calendarFrag, "calendar");
                    }

                    t.commit();
                }
            } else {
                // Drawer menu
                interfaceModeMenuItem.setTitle(R.string.list);
                interfaceModeMenuItem.setIcon(R.drawable.list);

                if (listFrag == null) {
                    FragmentTransaction t = manager.beginTransaction();
                    listFrag = new TaskListFragment();

                    if (calendarFrag == null) {
                        t.add(R.id.content_main_container, listFrag, "list");
                    } else {
                        t.replace(R.id.content_main_container, listFrag, "list");
                    }

                    t.commit();
                }
            }
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

            if (action != null && action.equals(ApplicationLogic.ACTION_DATA_CHANGED)) {
                MainActivity.this.updateInterface();
            }
        }
    }

    private class LoadDataTask extends AsyncTask<Uri, Void, JSONObject> {
        protected void onPreExecute() {
            // Nothing to do
        }

        protected JSONObject doInBackground(Uri... parameters) {
            JSONObject data = null;

            Uri input = parameters[0];
            ApplicationLogic logic = new ApplicationLogic(MainActivity.this, false);

            try {
                data = logic.loadData(input);
            } catch (Exception e) {
                logic.setNotifyDataChanges(true);
            }


            return data;
        }

        protected void onPostExecute(JSONObject data) {
            if (data == null) {
                (Toast.makeText(MainActivity.this, R.string.data_import_error, Toast.LENGTH_SHORT)).show();

            } else {
                ApplicationLogic logic = new ApplicationLogic(MainActivity.this);
                logic.setDataToImport(data.toString());

                startActivity(new Intent(MainActivity.this, ImportDataSettingsActivity.class));
            }
        }
    }
}
