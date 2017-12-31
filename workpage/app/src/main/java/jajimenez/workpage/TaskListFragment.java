package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class TaskListFragment extends Fragment implements TaskContainerFragment {
    private ListView list;
    private TextView emptyText;

    private Bundle savedInstanceState;
    private TaskListHostActivity activity;

    private AppBroadcastReceiver appBroadcastReceiver;
    private LoadTasksDBTask tasksDbTask = null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (TaskListHostActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement TaskListHostActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        View view = inflater.inflate(R.layout.task_list, container, false);

        list = view.findViewById(R.id.task_list_list);
        list.setVisibility(View.INVISIBLE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView l, View v, int position, long id) {
                Task task = (Task) l.getItemAtPosition(position);
                if (TaskListFragment.this.activity != null) TaskListFragment.this.activity.onTaskClicked(task);
            }
        });

        emptyText = view.findViewById(R.id.task_list_empty);
        emptyText.setVisibility(View.GONE);

        createContextualActionBar();
        setSelectedTasks();

        // Initial task load
        loadTasks();

        // Broadcast receiver
        registerBroadcastReceiver();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Broadcast receiver
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        appBroadcastReceiver = new AppBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ApplicationLogic.ACTION_DATA_CHANGED);

        (LocalBroadcastManager.getInstance(getContext())).registerReceiver(appBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        (LocalBroadcastManager.getInstance(getContext())).unregisterReceiver(appBroadcastReceiver);
    }

    private void createContextualActionBar() {
        list.clearChoices();
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                TaskListFragment.this.activity.setActionMode(mode);

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.task, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                TaskListFragment.this.activity.setActionMode(null);
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<Task> selectedTasks = getSelectedTasks();

                switch (item.getItemId()) {
                    case R.id.task_menu_status:
                        if (TaskListFragment.this.activity != null)  {
                            TaskListFragment.this.activity.showChangeTaskStatusDialog(selectedTasks);
                        }

                        eventHandled = true;
                        break;

                    case R.id.task_menu_edit:
                        // Open the task edition activity
                        if (TaskListFragment.this.activity != null)  {
                            TaskListFragment.this.activity.showEditActivity(selectedTasks.get(0));
                        }

                        // Close the context action bar
                        mode.finish();

                        eventHandled = true;
                        break;

                    case R.id.task_menu_delete:
                        // Show a deletion confirmation dialog
                        if (TaskListFragment.this.activity != null)  {
                            TaskListFragment.this.activity.showDeleteTaskDialog(selectedTasks);
                        }

                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTaskCount = list.getCheckedItemCount();
                if (selectedTaskCount > 0) mode.setTitle((TaskListFragment.this.getActivity()).getString(R.string.selected, selectedTaskCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.task_menu_edit);
                Drawable editItemIcon = editItem.getIcon();

                MenuItem deleteItem = (mode.getMenu()).findItem(R.id.task_menu_delete);
                Drawable deleteItemIcon = deleteItem.getIcon();

                if (selectedTaskCount == 1) {
                    editItem.setEnabled(true);
                    editItemIcon.setAlpha(255);
                }
                else {
                    editItem.setEnabled(false);
                    editItemIcon.setAlpha(127);
                }

                deleteItem.setEnabled(true);
                deleteItemIcon.setAlpha(255);
            }
        });
    }

    private void updateInterface(List<Task> tasks) {
        TaskAdapter adapter = new TaskAdapter(getActivity(), R.layout.task_list_item, tasks);
        list.setAdapter(adapter);

        if (adapter.isEmpty()) {
            list.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        }
        else {
            // Re-select items
            if (savedInstanceState != null) {
                int[] selectedItems = savedInstanceState.getIntArray("selected_items");

                if (selectedItems != null) {
                    for (int position : selectedItems) list.setItemChecked(position, true);
                    savedInstanceState.remove("selected_items");
                }
            }

            list.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }

    private void setSelectedTasks() {
        ListAdapter adapter = list.getAdapter();

        if (adapter != null && !adapter.isEmpty() && savedInstanceState != null) {
            int[] selectedItems = savedInstanceState.getIntArray("selected_items");

            if (selectedItems != null) {
                for (int position : selectedItems) list.setItemChecked(position, true);
                savedInstanceState.remove("selected_items");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<Integer> selectedItems = getSelectedItems();
        int selectedItemCount = selectedItems.size();
        int[] selected = new int[selectedItemCount];

        for (int i = 0; i < selectedItemCount; i++) selected[i] = selectedItems.get(i);
        outState.putIntArray("selected_items", selected);

        ActionMode mode = activity.getActionMode();
        if (mode != null) mode.finish();

        super.onSaveInstanceState(outState);
    }

    private List<Integer> getSelectedItems() {
        List<Integer> selectedItems = new LinkedList<>();

        SparseBooleanArray itemSelectedStates = list.getCheckedItemPositions();
        int itemCount = list.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The item with position "i" is selected.
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    private List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new LinkedList<>();

        TaskAdapter adapter = (TaskAdapter) list.getAdapter();
        List<Integer> selectedItems = getSelectedItems();

        for (int position : selectedItems) {
            Task task = adapter.getItem(position);
            selectedTasks.add(task);
        }

        return selectedTasks;
    }

    @Override
    public void setVisible(boolean visible) {
        View root = getView();

        if (visible) root.setVisibility(View.VISIBLE);
        else root.setVisibility(View.GONE);
    }

    private void closeActionBar() {
        // Close the context action bar
        ActionMode mode = activity.getActionMode();
        if (mode != null) mode.finish();
    }

    private void loadTasks() {
        if (tasksDbTask == null || tasksDbTask.getStatus() == AsyncTask.Status.FINISHED) {
            tasksDbTask = new LoadTasksDBTask();
            tasksDbTask.execute();
        }
    }

    private class AppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            TaskListFragment.this.closeActionBar();
            String action = intent.getAction();

            if (action != null && action.equals(ApplicationLogic.ACTION_DATA_CHANGED)) {
                // Get the tasks
                TaskListFragment.this.loadTasks();
            }
        }
    }

    private class LoadTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        protected void onPreExecute() {
            TaskListFragment.this.list.setEnabled(false);
        }

        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = new LinkedList<>();

            try {
                ApplicationLogic applicationLogic = new ApplicationLogic(TaskListFragment.this.getContext());
                TaskContext currentTaskContext = applicationLogic.getCurrentTaskContext();

                // View filters
                String viewStateFilter = applicationLogic.getViewStateFilter();
                boolean includeTasksWithNoTag = applicationLogic.getIncludeTasksWithNoTag();
                List<TaskTag> currentFilterTags = applicationLogic.getCurrentFilterTags();

                switch (viewStateFilter) {
                    case "open":
                        tasks = applicationLogic.getOpenTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                        break;
                    case "doable_today":
                        tasks = applicationLogic.getDoableTodayTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                        break;
                    default:
                        tasks = applicationLogic.getClosedTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                }
            }
            catch (Exception e) {
                // Nothing to do
            }

            return tasks;
        }

        protected void onPostExecute(List<Task> tasks) {
            try {
                TaskListFragment.this.updateInterface(tasks);
                TaskListFragment.this.list.setEnabled(true);
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }
}
