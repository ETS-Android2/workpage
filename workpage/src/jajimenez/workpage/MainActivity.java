package jajimenez.workpage;

import java.util.Calendar;
import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.Activity;
import android.app.ListActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.view.ActionMode;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.R;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
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
                inflater.inflate(R.menu.main_contextual_action_bar, menu);

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
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                boolean eventHandled = false;

                switch (item.getItemId()) {
                    case R.id.mainContextualActionBar_menu_status:
                        ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment(mode);
                        statusFragment.show(getFragmentManager(), "change_task_status");
                        eventHandled = true;
                        break;
                        
                    case R.id.mainContextualActionBar_menu_edit:
                        // Open the task edition activity:
                        long selectedTaskId = ((getSelectedTasks()).get(0)).getId();

                        Intent intent = new Intent(MainActivity.this, EditTaskActivity.class);
                        intent.putExtra("action", "edit");
                        intent.putExtra("task_id", selectedTaskId);

                        startActivityForResult(intent, 0);
                        mode.finish(); // Close the context action bar
                        eventHandled = true;
                        break;

                    case R.id.mainContextualActionBar_menu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment(mode);
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

                MenuItem editItem = (mode.getMenu()).findItem(R.id.mainContextualActionBar_menu_edit);
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

    // This method will be called when coming back from EditTask activity.
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

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        DialogFragment fragment = new SwitchTaskContextDialogFragment();
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

    private class TaskAdapter extends ArrayAdapter<Task> {
        private int resource;

        public TaskAdapter(Context context, int resource, List<Task> items) {
            super(context, resource, items);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View itemView, ViewGroup parentViewGroup) {
            TextView titleTextView = null;
            TextView details1TextView = null;
            TextView details2TextView = null;

            if (itemView == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                itemView = inflater.inflate(resource, null);

                titleTextView = (TextView) itemView.findViewById(R.id.taskListItem_title);
                details1TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_1);
                details2TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_2);

                TaskItemViewTag viewTag = new TaskItemViewTag();

                viewTag.titleTextView = titleTextView;
                viewTag.details1TextView = details1TextView;
                viewTag.details2TextView = details2TextView;

                itemView.setTag(viewTag);
            }
            else {
                TaskItemViewTag viewTag = (TaskItemViewTag) itemView.getTag();

                titleTextView = viewTag.titleTextView;
                details1TextView = viewTag.details1TextView;
                details2TextView = viewTag.details2TextView;
            }

            Task task = getItem(position);

            String title = task.getTitle();
            Calendar start = task.getStart();
            Calendar deadline = task.getDeadline();
            List<TaskTag> tags = task.getTags();

            // Show title
            titleTextView.setText(title);

            // Show Tags and Dates
            int tagCount = tags.size();
            String tagsText = "";
            String datesText = getTaskDatesText(start, deadline);

            if (tags != null && tagCount > 0) {
                for (int i = 0; i < tagCount; i++) {
                    String name = (tags.get(i)).getName();

                    if (i == (tagCount - 1)) tagsText += name;
                    else tagsText += (name + ", ");
                }

                details1TextView.setText(tagsText);
                details2TextView.setText(datesText);
            }
            else {
                details1TextView.setText(datesText);
            }

            return itemView;
        }

        private String getTaskDatesText(Calendar start, Calendar deadline) {
            String text = null;

            DateTimeTool tool = new DateTimeTool();
            String formattedStart = null;
            String formattedDeadline = null;

            if (start != null && deadline != null) {
                formattedStart = tool.getInterfaceFormattedDate(start);
                formattedDeadline = tool.getInterfaceFormattedDate(deadline);

                text = MainActivity.this.getString(R.string.task_start_deadline, formattedStart, formattedDeadline);
            }
            else if (start != null) {
                formattedStart = tool.getInterfaceFormattedDate(start);
                text = MainActivity.this.getString(R.string.task_start, formattedStart);
            }
            else if (deadline != null) {
                formattedDeadline = tool.getInterfaceFormattedDate(deadline);
                text = MainActivity.this.getString(R.string.task_deadline, formattedDeadline);
            }
            else {
                text = "";
            }

            return text;
        }

        private class TaskItemViewTag {
            public TextView titleTextView;
            public TextView details1TextView;
            public TextView details2TextView;

            public TaskItemViewTag() {
                titleTextView = null;
                details1TextView = null;
                details2TextView = null;
            }
        }
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

    private class SwitchTaskContextDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            long currentTaskContextId = MainActivity.this.currentTaskContext.getId();
            int selectedItem = -1;

            final List<TaskContext> taskContexts = MainActivity.this.applicationLogic.getAllTaskContexts();
            int taskContextCount = taskContexts.size();
            String[] taskContextNames = new String[taskContextCount];
            TaskContext t = null;

            for (int i = 0; i < taskContextCount; i++) {
                t = taskContexts.get(i);
                taskContextNames[i] = t.getName();

                if (t.getId() == currentTaskContextId) selectedItem = i;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.switch_task_context);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setSingleChoiceItems(taskContextNames, selectedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // "which" is the index position of the selected item and, in this
                    // case, it's also the ID of the task context we want to select.
                    TaskContext newCurrentTaskContext = taskContexts.get(which);

                    MainActivity.this.currentTaskContext = newCurrentTaskContext;
                    MainActivity.this.applicationLogic.setCurrentTaskContext(newCurrentTaskContext);
                    MainActivity.this.applicationLogic.setCurrentFilterTags(new LinkedList<TaskTag>());

                    dialog.dismiss();
                    MainActivity.this.updateInterface();
                }
            });

            return builder.create();
        }
    }

    private class ChangeTaskStatusDialogFragment extends DialogFragment {
        private ActionMode mode;

        public ChangeTaskStatusDialogFragment(ActionMode mode) {
            this.mode = mode;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final List<Task> selectedTasks = MainActivity.this.getSelectedTasks();
            int selectedTaskCount = selectedTasks.size();
            Resources resources = MainActivity.this.getResources();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(resources.getQuantityString(R.plurals.task_status, selectedTaskCount, selectedTaskCount));
            builder.setNegativeButton(R.string.cancel, null);

            String[] items = null;
            final boolean firstTaskDone = (selectedTasks.get(0)).isDone();

            if (!firstTaskDone) items = new String[] { MainActivity.this.getString(R.string.mark_as_done) };
            else items = new String[] { MainActivity.this.getString(R.string.mark_as_not_done) };

            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // Switch tasks status.
                    boolean done = !firstTaskDone;

                    for (Task task : selectedTasks) {
                        task.setDone(done);
                        MainActivity.this.applicationLogic.saveTask(task);
                    }

                    // Close the dialog.
                    dialog.dismiss();

                    // Close the contextual action bar.
                    ChangeTaskStatusDialogFragment.this.mode.finish();

                    // Update the list view.
                    MainActivity.this.updateInterface();
                }
            });

            return builder.create();
        }
    }

    private class DeleteTaskDialogFragment extends DialogFragment {
        private ActionMode mode;

        public DeleteTaskDialogFragment(ActionMode mode) {
            this.mode = mode;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final List<Task> selectedTasks = MainActivity.this.getSelectedTasks();
            final int selectedTaskCount = selectedTasks.size();
            final Resources resources = MainActivity.this.getResources();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_task, selectedTaskCount, selectedTaskCount));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.applicationLogic.deleteTasks(selectedTasks);

                    String text = resources.getQuantityString(R.plurals.task_deleted, selectedTaskCount, selectedTaskCount);
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();

                    // Close the dialog.
                    dialog.dismiss();

                    // Close the contextual action bar.
                    DeleteTaskDialogFragment.this.mode.finish();

                    // Update the list view.
                    MainActivity.this.updateTaskListInterfaceByRemoving(selectedTasks);
                }
            });

            return builder.create();
        }
    }
}
