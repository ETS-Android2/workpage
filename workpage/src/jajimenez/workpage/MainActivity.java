package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.view.ActionMode;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class MainActivity extends ListActivity implements DataChangeReceiverActivity {
    private Menu menu;
    private TextView viewTextView;
    private TextView filterTagsTitleTextView;
    private TextView filterTagsValueTextView;
    private ActionBar actionBar;
    private ActionMode actionMode;
    private ListView listView;
    private TextView emptyTextView;

    private Bundle savedInstanceState;
    private boolean interfaceReady;
    private boolean inFront;

    private SwitchTaskContextDialogFragment.OnNewCurrentTaskContextSetListener switchTaskContextListener;
    private ChangeTaskStatusDialogFragment.OnItemClickListener taskStatusChangeListener;
    private DeleteTaskDialogFragment.OnDeleteListener deleteTaskListener;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private String currentView;
    private List<TaskTag> currentFilterTags;

    private DataExportBroadcastReceiver exportReceiver = null;
    private IntentFilter exportFilter = null;

    private DataImportBroadcastReceiver importReceiver = null;
    private IntentFilter importFilter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        viewTextView = (TextView) findViewById(R.id.main_view);
        filterTagsTitleTextView = (TextView) findViewById(R.id.main_filterTags_title);
        filterTagsValueTextView = (TextView) findViewById(R.id.main_filterTags_value);
        listView = getListView();
        emptyTextView = (TextView) findViewById(android.R.id.empty);
        actionBar = getActionBar();
        actionMode = null;

        createContextualActionBar();
        interfaceReady = false;
        inFront = false;

        switchTaskContextListener = new SwitchTaskContextDialogFragment.OnNewCurrentTaskContextSetListener() {
            public void onNewCurrentTaskContextSet(TaskContext newCurrentTaskContext, String newCurrentView, List<TaskTag> newCurrentFilterTags) {
                if (newCurrentTaskContext != null) {
                    MainActivity.this.currentTaskContext = newCurrentTaskContext;
                    MainActivity.this.currentView = newCurrentView;
                    MainActivity.this.currentFilterTags = newCurrentFilterTags;

                    MainActivity.this.updateInterface();
                }
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

        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            SwitchTaskContextDialogFragment switchTaskContextFragment = (SwitchTaskContextDialogFragment) (getFragmentManager()).findFragmentByTag("switch_task_context");
            if (switchTaskContextFragment != null) switchTaskContextFragment.setOnNewCurrentTaskContextSetListener(switchTaskContextListener);

            ChangeTaskStatusDialogFragment changeTaskStatusFragment = (ChangeTaskStatusDialogFragment) (getFragmentManager()).findFragmentByTag("change_task_status");
            if (changeTaskStatusFragment != null) changeTaskStatusFragment.setOnItemClickListener(taskStatusChangeListener);

            DeleteTaskDialogFragment deleteTaskFragment = (DeleteTaskDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task");
            if (deleteTaskFragment != null) deleteTaskFragment.setOnDeleteListener(deleteTaskListener);
        }

        applicationLogic = new ApplicationLogic(this);
        currentView = "";
        currentFilterTags = new LinkedList<TaskTag>();
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

                switch (item.getItemId()) {
                    case R.id.taskMenu_status:
                        ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment(selectedTasks);
                        statusFragment.setOnItemClickListener(MainActivity.this.taskStatusChangeListener);
                        statusFragment.show(getFragmentManager(), "change_task_status");

                        eventHandled = true;
                        break;
                        
                    case R.id.taskMenu_edit:
                        // Open the task edition activity.
                        long selectedTaskId = (selectedTasks.get(0)).getId();

                        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                        intent.putExtra("mode", "edit");
                        intent.putExtra("task_id", selectedTaskId);

                        startActivity(intent);

                        // Close the context action bar.
                        mode.finish();

                        eventHandled = true;
                        break;

                    case R.id.taskMenu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment(selectedTasks);
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

                MenuItem editItem = (mode.getMenu()).findItem(R.id.taskMenu_edit);
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
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ImportDataService.getStatus() == ImportDataService.STATUS_NOT_RUNNING && ExportDataService.getStatus() == ExportDataService.STATUS_NOT_RUNNING) {
            updateInterface();
        }
        else {
            disableInterface();
        }

        inFront = true;

        exportReceiver = new DataExportBroadcastReceiver(this);
        exportFilter = new IntentFilter(ApplicationConstants.DATA_EXPORT_ACTION);
        registerReceiver(exportReceiver, exportFilter);

        importReceiver = new DataImportBroadcastReceiver(this);
        importFilter = new IntentFilter(ApplicationConstants.DATA_IMPORT_ACTION);
        registerReceiver(importReceiver, importFilter);
    }

    @Override
    public void onPause() {
        super.onPause();

        inFront = false;

        unregisterReceiver(exportReceiver);
        unregisterReceiver(importReceiver);
    }

    public boolean isInFront() {
        return inFront;
    }

    public void enableInterface() {
        listView.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;
        setProgressBarIndeterminateVisibility(true);
        listView.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<Integer> selectedItems = getSelectedItems();
        int selectedItemCount = selectedItems.size();
        int[] selected = new int[selectedItemCount];

        for (int i = 0; i < selectedItemCount; i++) selected[i] = selectedItems.get(i);
        outState.putIntArray("selected_items", selected);

        if (actionMode != null) actionMode.finish();

        super.onSaveInstanceState(outState);
    }

    public void updateInterface() {
        currentTaskContext = applicationLogic.getCurrentTaskContext();

        // Information about the current task context.
        actionBar.setSubtitle(currentTaskContext.getName());

        // Information about the current view.
        currentView = this.applicationLogic.getCurrentView();
        
        if (currentView.equals("open")) viewTextView.setText(R.string.open);
        else if (currentView.equals("doable_today")) viewTextView.setText(R.string.doable_today);
        else if (currentView.equals("closed")) viewTextView.setText(R.string.closed);

        // Information about the current filter tags.
        currentFilterTags = this.applicationLogic.getCurrentFilterTags();

        int filterTagCount = 0;
        if (currentFilterTags != null) filterTagCount = currentFilterTags.size();

        if (filterTagCount == 0) {
            filterTagsTitleTextView.setVisibility(View.GONE);
            filterTagsValueTextView.setVisibility(View.GONE);

            filterTagsValueTextView.setText("");
        }
        else {
            filterTagsTitleTextView.setVisibility(View.VISIBLE);
            filterTagsValueTextView.setVisibility(View.VISIBLE);

            String tagsText = "";

            for (int i = 0; i < filterTagCount; i++) {
                tagsText += (currentFilterTags.get(i)).getName();
                if (i < (filterTagCount - 1)) tagsText += ", ";
            }

            filterTagsValueTextView.setText(tagsText);
        }

        emptyTextView.setText("");

        // Show tasks.
        (new LoadTasksDBTask()).execute();
    }

    private void updateTaskListInterface(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<Task>();

        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_list_item, tasks);
        setListAdapter(adapter);

        if (adapter.isEmpty()) {
            emptyTextView.setText(R.string.no_tasks);
        } else if (savedInstanceState != null) {
            int[] selectedItems = savedInstanceState.getIntArray("selected_items");

            if (selectedItems != null) {
                for (int position : selectedItems) listView.setItemChecked(position, true);
                savedInstanceState.remove("selected_items");
            }
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

        TaskAdapter adapter = (TaskAdapter) getListAdapter();
        List<Integer> selectedItems = getSelectedItems();
        
        for (int position : selectedItems) {
            Task task = adapter.getItem(position);
            selectedTasks.add(task);
        }

        return selectedTasks;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Task selectedTask = (Task) l.getItemAtPosition(position);

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task_id", selectedTask.getId());

        startActivity(intent);
    }

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        SwitchTaskContextDialogFragment fragment = new SwitchTaskContextDialogFragment();
        fragment.setOnNewCurrentTaskContextSetListener(switchTaskContextListener);
        fragment.show(getFragmentManager(), "switch_task_context");
    }

    public void onEditTaskTagsItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, EditTaskTagsActivity.class);
        startActivity(intent);
    }

    public void onViewItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, ViewActivity.class);
        startActivity(intent);
    }

    public void onNewTaskItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("mode", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivity(intent);
    }

    public void onExportDataItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra("mode", "export");
        startActivity(intent);
    }

    public void onImportDataItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, FileBrowserActivity.class);
        intent.putExtra("mode", "import");
        startActivity(intent);
    }

    public void onAboutItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private class LoadTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        protected void onPreExecute() {
            MainActivity.this.disableInterface();
        }

        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = null;

            if (currentView.equals("open")) tasks = MainActivity.this.applicationLogic.getOpenTasks(MainActivity.this.currentTaskContext, currentFilterTags);
            else if (currentView.equals("doable_today")) tasks = MainActivity.this.applicationLogic.getDoableTodayTasks(MainActivity.this.currentTaskContext, currentFilterTags);
            else if (currentView.equals("closed")) tasks = MainActivity.this.applicationLogic.getClosedTasks(MainActivity.this.currentTaskContext, currentFilterTags);

            return tasks;
        }

        protected void onPostExecute(List<Task> tasks) {
            MainActivity.this.updateTaskListInterface(tasks);
            MainActivity.this.enableInterface();;
        }
    }
}
