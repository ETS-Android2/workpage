package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.ActionMode;
import android.content.IntentFilter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DataChangeReceiverActivity {

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView viewTextView;
    private TextView filterTagsValueTextView;
    private ActionMode actionMode;
    private ListView listView;
    private TextView emptyTextView;

    private Bundle savedInstanceState;
    private boolean interfaceReady;

    private SwitchTaskContextDialogFragment.OnNewCurrentTaskContextSetListener switchTaskContextListener;
    private ChangeTaskStatusDialogFragment.OnItemClickListener taskStatusChangeListener;
    private DeleteTaskDialogFragment.OnDeleteListener deleteTaskListener;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private String viewStateFilter;
    private boolean includeTasksWithNoTag;
    private List<TaskTag> currentFilterTags;

    private DataExportBroadcastReceiver exportReceiver;
    private DataImportBroadcastReceiver importReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar_main_toolbar);
        setSupportActionBar(toolbar);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView l, View v, int position, long id) {
                Task selectedTask = (Task) l.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, TaskActivity.class);
                intent.putExtra("task_id", selectedTask.getId());

                startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);
            }
        });

        emptyTextView = (TextView) findViewById(android.R.id.empty);
        viewTextView = (TextView) findViewById(R.id.main_view);
        filterTagsValueTextView = (TextView) findViewById(R.id.main_filter_tags_value);

        actionMode = null;

        createContextualActionBar();
        interfaceReady = false;

        // Add Task button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.app_bar_main_add_task_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!interfaceReady) return;

                Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                intent.putExtra("mode", "new");
                intent.putExtra("task_context_id", currentTaskContext.getId());

                startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);
            }
        });

        // Navigation menu
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Listeners
        switchTaskContextListener = new SwitchTaskContextDialogFragment.OnNewCurrentTaskContextSetListener() {
            public void onNewCurrentTaskContextSet() {
                MainActivity.this.updateInterface();
            }
        };

        taskStatusChangeListener = new ChangeTaskStatusDialogFragment.OnItemClickListener() {
            public void onItemClick() {
                // Close the context action bar.
                if (MainActivity.this.actionMode != null) MainActivity.this.actionMode.finish();

                // Update the list view.
                MainActivity.this.updateInterface();
            }
        };

        deleteTaskListener = new DeleteTaskDialogFragment.OnDeleteListener() {
            public void onDelete() {
                // Close the context action bar.
                if (MainActivity.this.actionMode != null) MainActivity.this.actionMode.finish();

                // Update the list view.
                MainActivity.this.updateInterface();
            }
        };

        // Instance state
        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            SwitchTaskContextDialogFragment switchTaskContextFragment = (SwitchTaskContextDialogFragment) (getFragmentManager()).findFragmentByTag("switch_task_context");
            if (switchTaskContextFragment != null) switchTaskContextFragment.setOnNewCurrentTaskContextSetListener(switchTaskContextListener);

            ChangeTaskStatusDialogFragment changeTaskStatusFragment = (ChangeTaskStatusDialogFragment) (getFragmentManager()).findFragmentByTag("change_task_status");
            if (changeTaskStatusFragment != null) changeTaskStatusFragment.setOnItemClickListener(taskStatusChangeListener);

            DeleteTaskDialogFragment deleteTaskFragment = (DeleteTaskDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task");
            if (deleteTaskFragment != null) deleteTaskFragment.setOnDeleteListener(deleteTaskListener);
        }

        // Parameters
        applicationLogic = new ApplicationLogic(this);
        viewStateFilter = "";
        includeTasksWithNoTag = true;
        currentFilterTags = new LinkedList<TaskTag>();

        applicationLogic.updateAllOpenTaskReminderAlarms(false);
        updateInterface();
    }

    private void createContextualActionBar() {
        listView.clearChoices();
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MainActivity.this.actionMode = mode;

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.task, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                MainActivity.this.actionMode = null;
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<Task> selectedTasks = MainActivity.this.getSelectedTasks();

                Bundle arguments;
                int selectedTaskCount;
                long[] taskIds;

                switch (item.getItemId()) {
                    case R.id.task_menu_status:
                        ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment();

                        arguments = new Bundle();
                        selectedTaskCount = selectedTasks.size();
                        taskIds = new long[selectedTaskCount];

                        for (int i = 0; i < selectedTaskCount; i++) {
                            Task t = selectedTasks.get(i);
                            taskIds[i] = t.getId();
                        }

                        arguments.putLongArray("task_ids", taskIds);
                        statusFragment.setArguments(arguments);

                        statusFragment.setOnItemClickListener(MainActivity.this.taskStatusChangeListener);
                        statusFragment.show(getFragmentManager(), "change_task_status");

                        eventHandled = true;
                        break;

                    case R.id.task_menu_edit:
                        // Open the task edition activity.
                        long selectedTaskId = (selectedTasks.get(0)).getId();

                        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                        intent.putExtra("mode", "edit");
                        intent.putExtra("task_id", selectedTaskId);

                        startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);

                        // Close the context action bar.
                        mode.finish();

                        eventHandled = true;
                        break;

                    case R.id.task_menu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment();

                        arguments = new Bundle();
                        selectedTaskCount = selectedTasks.size();
                        taskIds = new long[selectedTaskCount];

                        for (int i = 0; i < selectedTaskCount; i++) {
                            Task t = selectedTasks.get(i);
                            taskIds[i] = t.getId();
                        }

                        arguments.putLongArray("task_ids", taskIds);
                        deleteFragment.setArguments(arguments);

                        deleteFragment.setOnDeleteListener(MainActivity.this.deleteTaskListener);
                        deleteFragment.show(getFragmentManager(), "delete_task");

                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTaskCount = MainActivity.this.listView.getCheckedItemCount();
                mode.setTitle(MainActivity.this.getString(R.string.selected, selectedTaskCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.task_menu_edit);
                Drawable editItemIcon = editItem.getIcon();

                if (selectedTaskCount == 1) {
                    editItem.setEnabled(true);
                    editItemIcon.setAlpha(255);
                }
                else {
                    editItem.setEnabled(false);
                    editItemIcon.setAlpha(127);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        exportReceiver = new DataExportBroadcastReceiver(this);
        IntentFilter exportFilter = new IntentFilter(ApplicationConstants.DATA_EXPORT_ACTION);
        registerReceiver(exportReceiver, exportFilter);

        importReceiver = new DataImportBroadcastReceiver(this);
        IntentFilter importFilter = new IntentFilter(ApplicationConstants.DATA_IMPORT_ACTION);
        registerReceiver(importReceiver, importFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(exportReceiver);
        unregisterReceiver(importReceiver);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        if (!interfaceReady) return true;
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.main_nav_context:
                SwitchTaskContextDialogFragment fragment = new SwitchTaskContextDialogFragment();
                fragment.setOnNewCurrentTaskContextSetListener(switchTaskContextListener);
                fragment.show(getFragmentManager(), "switch_task_context");
                break;
            case R.id.main_nav_view:
                intent = new Intent(this, ViewActivity.class);
                startActivity(intent);
                break;
            case R.id.main_nav_edit_tags:
                intent = new Intent(this, EditTaskTagsActivity.class);
                startActivityForResult(intent, ApplicationLogic.CHANGE_TASK_TAGS);
                break;
            case R.id.main_nav_export_data:
                intent = new Intent(this, FileBrowserActivity.class);
                intent.putExtra("mode", "export");
                startActivity(intent);
                break;
            case R.id.main_nav_import_data:
                intent = new Intent(this, FileBrowserActivity.class);
                intent.putExtra("mode", "import");
                startActivity(intent);
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == ApplicationLogic.CHANGE_TASKS
                || requestCode == ApplicationLogic.CHANGE_TASK_TAGS)
                && resultCode == RESULT_OK) {
            updateInterface();
        }
    }

    public void enableInterface() {
        listView.setEnabled(true);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;
        listView.setEnabled(false);
    }

    public void updateInterface() {
        // Information about the current task context.
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        Menu drawerMenu = navigationView.getMenu();
        MenuItem contextItem = drawerMenu.findItem(R.id.main_nav_context);
        contextItem.setTitle(currentTaskContext.getName());

        // Information about the current view.
        viewStateFilter = this.applicationLogic.getViewStateFilter();

        if (viewStateFilter.equals("open")) viewTextView.setText(R.string.open);
        else if (viewStateFilter.equals("doable_today")) viewTextView.setText(R.string.doable_today);
        else if (viewStateFilter.equals("closed")) viewTextView.setText(R.string.closed);

        // Information about the current filter tags.
        includeTasksWithNoTag = this.applicationLogic.getIncludeTasksWithNoTag();
        currentFilterTags = this.applicationLogic.getCurrentFilterTags();

        int filterTagCount = 0;
        if (currentFilterTags != null) filterTagCount = currentFilterTags.size();

        if (!includeTasksWithNoTag && filterTagCount == 0) {
            filterTagsValueTextView.setText(R.string.none);
        }
        else if (includeTasksWithNoTag && filterTagCount == applicationLogic.getTaskTagCount(currentTaskContext)) {
            filterTagsValueTextView.setText(R.string.all);
        }
        else {
            String tagsText = "";

            if (includeTasksWithNoTag) {
                tagsText += getString(R.string.without_tags);
                if (filterTagCount > 0) tagsText += getString(R.string.separator);
            }

            for (int i = 0; i < filterTagCount; i++) {
                tagsText += (currentFilterTags.get(i)).getName();
                if (i < (filterTagCount - 1)) tagsText += getString(R.string.separator);
            }

            filterTagsValueTextView.setText(tagsText);
        }

        // Show tasks.
        (new LoadTasksDBTask()).execute();
    }


    private void updateTaskListInterface(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<Task>();

        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_list_item, tasks);
        listView.setAdapter(adapter);

        if (adapter.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        }
        else {
            if (savedInstanceState != null) {
                int[] selectedItems = savedInstanceState.getIntArray("selected_items");

                if (selectedItems != null) {
                    for (int position : selectedItems) listView.setItemChecked(position, true);
                    savedInstanceState.remove("selected_items");
                }
            }

            listView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    private List<Integer> getSelectedItems() {
        List<Integer> selectedItems = new LinkedList<Integer>();

        SparseBooleanArray itemSelectedStates = listView.getCheckedItemPositions();
        int itemCount = listView.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The item with position "i" is selected.
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    private List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new LinkedList<Task>();

        TaskAdapter adapter = (TaskAdapter) listView.getAdapter();
        List<Integer> selectedItems = getSelectedItems();

        for (int position : selectedItems) {
            Task task = adapter.getItem(position);
            selectedTasks.add(task);
        }

        return selectedTasks;
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
            MainActivity.this.updateTaskListInterface(tasks);
            MainActivity.this.enableInterface();;
        }
    }
}