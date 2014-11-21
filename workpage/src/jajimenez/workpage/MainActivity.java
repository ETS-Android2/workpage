package jajimenez.workpage;

import java.util.List;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ListActivity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.Task;

public class MainActivity extends ListActivity {
    private ApplicationLogic applicationLogic = null;
    private TaskContext currentTaskContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = this.applicationLogic.getCurrentTaskContext();

        updateInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    private void updateInterface() {
        setTitle(currentTaskContext.getName());
        (new LoadAllOpenTasksDBTask()).execute();
    }

    private void updateTaskListInterface(List<Task> tasks) {
        TaskAdapter adapter = new TaskAdapter(this, R.layout.task_row, tasks);
        setListAdapter(adapter);
    }

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        DialogFragment fragment = new SwitchTaskContextDialogFragment();
        fragment.show(getFragmentManager(), "switch_task_context");
    }

    public void onNewTaskItemSelected(MenuItem item) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("action", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivity(intent);
    }

    public void onAboutItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private class TaskAdapter extends ArrayAdapter<Task> {
        private int resource;

        public TaskAdapter(Context context, int resource, List<Task> items) {
            super(context, resource, items);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View rowView, ViewGroup parent) {
            TaskRowViewTag tag = null;

            if (rowView == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                rowView = inflater.inflate(resource, null);

                tag = new TaskRowViewTag((TextView) rowView.findViewById(R.id.title_textview));
                rowView.setTag(tag);
            } else {
                tag = (TaskRowViewTag) rowView.getTag();
            }

            Task task = getItem(position);
            (tag.getTitleTextView()).setText(task.getTitle());

            return rowView;
        }
    }

    private class TaskRowViewTag {
        private TextView titleTextView;

        public TaskRowViewTag(TextView titleTextView) {
            this.titleTextView = titleTextView;
        }

        public TextView getTitleTextView() {
            return titleTextView;
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

    private class LoadAllOpenTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        @Override
        protected List<Task> doInBackground(Void... parameters) {
            return MainActivity.this.applicationLogic.getAllOpenTasks(MainActivity.this.currentTaskContext);
        }

        @Override
        protected void onPostExecute(List<Task> tasks) {
            MainActivity.this.updateTaskListInterface(tasks);
        }
    }
}
