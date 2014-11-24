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

import jajimenez.workpage.R;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.Task;

public class MainActivity extends ListActivity {
    private ListView listView;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = getListView();
        createContextualActionBar();

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = this.applicationLogic.getCurrentTaskContext();

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

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.mainContextualActionBar_menu_status:
                        // ToDo
                        // mode.finish(); --> Closes the context action bar
                        return true;
                    case R.id.mainContextualActionBar_menu_edit:
                        // ToDo
                        // mode.finish(); --> Closes the context action bar
                        return true;
                    case R.id.mainContextualActionBar_menu_delete:
                        DialogFragment fragment = new DeleteTaskDialogFragment(mode);
                        fragment.show(getFragmentManager(), "delete_task");
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTaskCount = MainActivity.this.listView.getCheckedItemCount();
                mode.setTitle(MainActivity.this.getString(R.string.selected_tasks, selectedTaskCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.mainContextualActionBar_menu_edit);
                editItem.setEnabled(selectedTaskCount == 1);
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
        setTitle(currentTaskContext.getName());
        (new LoadAllCurrentOpenTasksDBTask()).execute();
    }

    private void updateTaskListInterface(List<Task> tasks) {
        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_list_item, tasks);
        setListAdapter(adapter);
    }

    private void updateTaskListInterfaceByRemove(List<Task> tasksToRemove) {
        TaskAdapter adapter = (TaskAdapter) getListAdapter();
        for (Task task : tasksToRemove) adapter.remove(task);
    }

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        DialogFragment fragment = new SwitchTaskContextDialogFragment();
        fragment.show(getFragmentManager(), "switch_task_context");
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
            List<Long> taskTags = task.getTags();

            titleTextView.setText(title);
            details1TextView.setText(getTaskDatesText(start, deadline));

            if (taskTags != null && taskTags.size() > 0) {
                // ToDo: List task's tags on "details2TextView"
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
            return MainActivity.this.applicationLogic.getAllCurrentOpenTasks(MainActivity.this.currentTaskContext);
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
                public void onClick(DialogInterface dialog, int id) {
                    TaskContext newCurrentTaskContext = taskContexts.get(id);

                    MainActivity.this.currentTaskContext = newCurrentTaskContext;
                    MainActivity.this.applicationLogic.setCurrentTaskContext(newCurrentTaskContext);

                    dialog.dismiss();
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
                public void onClick(DialogInterface dialog, int id) {
                    for (Task task : selectedTasks) {
                        // ToDo: implement this method called:
                        // MainActivity.this.applicationLogic.deleteTask(id);
                    }

                    String text = resources.getQuantityString(R.plurals.task_deleted, selectedTaskCount, selectedTaskCount);
                    Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();

                    // Close the dialog.
                    dialog.dismiss();

                    // Close the contextual action bar.
                    DeleteTaskDialogFragment.this.mode.finish();

                    // Update the list view.
                    updateTaskListInterfaceByRemove(selectedTasks);
                }
            });

            return builder.create();
        }
    }
}
