package jajimenez.workpage;

import java.util.Calendar;

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

import jajimenez.workpage.R;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
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
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        (new LoadAllCurrentOpenTasksDBTask()).execute();
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
        public View getView(int position, View rowView, ViewGroup parentViewGroup) {
            TextView titleTextView = null;
            TextView details1TextView = null;
            TextView details2TextView = null;

            if (rowView == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                rowView = inflater.inflate(resource, null);

                titleTextView = (TextView) rowView.findViewById(R.id.taskrow_title);
                details1TextView = (TextView) rowView.findViewById(R.id.taskrow_details_1);
                details2TextView = (TextView) rowView.findViewById(R.id.taskrow_details_2);

                TaskRowViewTag viewTag = new TaskRowViewTag();

                viewTag.titleTextView = titleTextView;
                viewTag.details1TextView = details1TextView;
                viewTag.details2TextView = details2TextView;

                rowView.setTag(viewTag);
            }
            else {
                TaskRowViewTag viewTag = (TaskRowViewTag) rowView.getTag();

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

            return rowView;
        }

        private String getTaskDatesText(Calendar start, Calendar deadline) {
            String text = null;

            DateTimeTool tool = new DateTimeTool();
            String formattedStart = null;
            String formattedDeadline = null;

            if (start != null && deadline != null) {
                formattedStart = tool.getInterfaceFormattedDate(start);
                formattedDeadline = tool.getInterfaceFormattedDate(deadline);

                text = getString(R.string.task_start_deadline, formattedStart, formattedDeadline);
            }
            else if (start != null) {
                formattedStart = tool.getInterfaceFormattedDate(start);
                text = getString(R.string.task_start, formattedStart);
            }
            else if (deadline != null) {
                formattedDeadline = tool.getInterfaceFormattedDate(deadline);
                text = getString(R.string.task_deadline, formattedDeadline);
            }
            else {
                text = "";
            }

            return text;
        }

        private class TaskRowViewTag {
            public TextView titleTextView = null;
            public TextView details1TextView = null;
            public TextView details2TextView = null;
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
}
