package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.ActionMode;
import android.content.Intent;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class MainActivity extends ListActivity {
    private TextView viewTextView;
    private TextView filterTagsTitleTextView;
    private TextView filterTagsValueTextView;
    private ListView listView;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private String currentView;
    private List<TaskTag> currentFilterTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        viewTextView = (TextView) findViewById(R.id.main_view);
        filterTagsTitleTextView = (TextView) findViewById(R.id.main_filterTags_title);
        filterTagsValueTextView = (TextView) findViewById(R.id.main_filterTags_value);
        listView = getListView();

        createContextualActionBar();

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = this.applicationLogic.getCurrentTaskContext();
        currentView = "";
        currentFilterTags = new LinkedList<TaskTag>();

        updateInterface();
    }

    private void createContextualActionBar() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override   
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.task, menu);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Do nothing
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<Task> selectedTasks = MainActivity.this.getSelectedTasks();

                switch (item.getItemId()) {
                    case R.id.taskMenu_status:
                        ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment(MainActivity.this, selectedTasks);

                        statusFragment.setOnItemClickListener(new ChangeTaskStatusDialogFragment.OnItemClickListener() {
                            public void onItemClick() {
                                // Close the context action bar.
                                mode.finish();

                                // Update the list view.
                                MainActivity.this.updateTaskListInterfaceByRemoving(selectedTasks);
                            }
                        });

                        statusFragment.show(getFragmentManager(), "change_task_status");
                        eventHandled = true;
                        break;
                        
                    case R.id.taskMenu_edit:
                        // Open the task edition activity.
                        long selectedTaskId = (selectedTasks.get(0)).getId();

                        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                        intent.putExtra("action", "edit");
                        intent.putExtra("task_id", selectedTaskId);

                        startActivityForResult(intent, 0);

                        // Close the context action bar.
                        mode.finish();

                        eventHandled = true;
                        break;

                    case R.id.taskMenu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment(MainActivity.this, selectedTasks);

                        deleteFragment.setOnDeleteListener(new DeleteTaskDialogFragment.OnDeleteListener() {
                            public void onDelete() {
                                // Close the contextual action bar.
                                mode.finish();

                                // Update the list view.
                                MainActivity.this.updateTaskListInterfaceByRemoving(selectedTasks);
                            }
                        });

                        deleteFragment.show(getFragmentManager(), "delete_task");
                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTaskCount = MainActivity.this.listView.getCheckedItemCount();
                mode.setTitle(MainActivity.this.getString(R.string.selected_tasks, selectedTaskCount));

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

        return true;
    }

    // This method will be called when coming back from EditTask activity (directly or through Task activity).
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) updateInterface();
    }

    private void updateInterface() {
        // Information about the current task context.
        (getActionBar()).setSubtitle(currentTaskContext.getName());

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

        // Show tasks.
        (new LoadAllCurrentOpenTasksDBTask()).execute();
    }

    private void updateTaskListInterface(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<Task>();

        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_list_item, tasks);
        setListAdapter(adapter);
    }

    private void updateTaskListInterfaceByRemoving(List<Task> tasksToRemove) {
        TaskAdapter adapter = (TaskAdapter) getListAdapter();
        for (Task task : tasksToRemove) adapter.remove(task);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Task selectedTask = (Task) l.getItemAtPosition(position);

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("task_id", selectedTask.getId());

        startActivityForResult(intent, 0);
    }

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        SwitchTaskContextDialogFragment fragment = new SwitchTaskContextDialogFragment(this);

        fragment.setOnNewCurrentTaskContextSetListener(new SwitchTaskContextDialogFragment.OnNewCurrentTaskContextSetListener() {
            public void onNewCurrentTaskContextSet(TaskContext newCurrentTaskContext, String newCurrentView, List<TaskTag> newCurrentFilterTags) {
                if (newCurrentTaskContext != null) {
                    MainActivity.this.currentTaskContext = newCurrentTaskContext;
                    MainActivity.this.currentView = newCurrentView;
                    MainActivity.this.currentFilterTags = newCurrentFilterTags;

                    MainActivity.this.updateInterface();
                }
            }
        });

        fragment.show(getFragmentManager(), "switch_task_context");
    }

    public void onViewItemSelected(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, ViewActivity.class);
        startActivityForResult(intent, 0);
    }

    public void onNewTaskItemSelected(MenuItem item) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("action", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivityForResult(intent, 0);
    }

    public void onAboutItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new LinkedList<Task>();
        
        TaskAdapter adapter = (TaskAdapter) getListAdapter();
        SparseBooleanArray itemSelectedStates = listView.getCheckedItemPositions();
        int itemCount = listView.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The task with position "i" is selected.
                Task task = adapter.getItem(i);
                selectedTasks.add(task);
            }
        }

        return selectedTasks;
    }

    private class LoadAllCurrentOpenTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = null;

            if (currentView.equals("open")) tasks = MainActivity.this.applicationLogic.getOpenTasks(MainActivity.this.currentTaskContext, currentFilterTags);
            else if (currentView.equals("doable_today")) tasks = MainActivity.this.applicationLogic.getDoableTodayTasks(MainActivity.this.currentTaskContext, currentFilterTags);
            else if (currentView.equals("closed")) tasks = MainActivity.this.applicationLogic.getClosedTasks(MainActivity.this.currentTaskContext, currentFilterTags);

            return tasks;
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            MainActivity.this.updateTaskListInterface(tasks);
        }
    }
}
