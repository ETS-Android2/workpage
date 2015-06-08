package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskTag;

public class TaskActivity extends Activity {
    private TextView titleTextView;
    private TextView tagsTextView;
    private TableLayout datesTableLayout;
    private TableRow startTableRow;
    private TextView startTextView;
    private TableRow deadlineTableRow;
    private TextView deadlineTextView;
    private TextView descriptionTextView;

    private ChangeTaskStatusDialogFragment.OnItemClickListener taskStatusChangeListener;
    private DeleteTaskDialogFragment.OnDeleteListener deleteTaskListener;

    private long currentTaskId;
    private Task currentTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);
        
        titleTextView = (TextView) findViewById(R.id.task_title);
        tagsTextView = (TextView) findViewById(R.id.task_tags);
        datesTableLayout = (TableLayout) findViewById(R.id.task_dates);
        startTableRow = (TableRow) findViewById(R.id.task_start_row);
        startTextView = (TextView) findViewById(R.id.task_start);
        deadlineTableRow = (TableRow) findViewById(R.id.task_deadline_row);
        deadlineTextView = (TextView) findViewById(R.id.task_deadline);
        descriptionTextView = (TextView) findViewById(R.id.task_description);

        taskStatusChangeListener = new ChangeTaskStatusDialogFragment.OnItemClickListener() {
            public void onItemClick() {
                // Update the list view.
                TaskActivity.this.updateInterface();
            }
        };

        deleteTaskListener = new DeleteTaskDialogFragment.OnDeleteListener() {
            public void onDelete() {
                // Close the activity.
                TaskActivity.this.finish();
            }
        };

        if (savedInstanceState != null) {
            ChangeTaskStatusDialogFragment changeTaskStatusFragment = (ChangeTaskStatusDialogFragment) (getFragmentManager()).findFragmentByTag("change_task_status");
            if (changeTaskStatusFragment != null) changeTaskStatusFragment.setOnItemClickListener(taskStatusChangeListener);

            DeleteTaskDialogFragment deleteTaskFragment = (DeleteTaskDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task");
            if (deleteTaskFragment != null) deleteTaskFragment.setOnDeleteListener(deleteTaskListener);
        }

        // Load task data.
        Intent intent = getIntent();
        currentTaskId = intent.getLongExtra("task_id", -1);
        currentTask = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task, menu);

        // This is necessary because the drawable of the Edit item icon of this activity keeps
        // the last alpha value set for the Edit item icon of MainActivity. MainActivity has
        // the same menu with the same drawables for the items.
        MenuItem editItem = menu.findItem(R.id.taskMenu_edit);
        Drawable editItemIcon = editItem.getIcon();
        editItemIcon.setAlpha(255);

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInterface();
    }

    private void updateInterface() {
        // Update Task object after possible changes.
        currentTask = (new ApplicationLogic(this)).getTask(currentTaskId);

        if (currentTask.isDone()) setTitle(R.string.task_closed);
        else setTitle(R.string.task_open);

        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());

        // Tags text.
        List<TaskTag> tags = currentTask.getTags();

        int tagCount = 0;
        if (tags != null) tagCount = tags.size();

        if (tagCount == 0) {
            tagsTextView.setVisibility(View.GONE);
        } else {
            String tagsText = "";

            for (int i = 0; i < tagCount; i++) {
                tagsText += (tags.get(i)).getName();
                if (i < (tagCount - 1)) tagsText += ", ";
            }

            tagsTextView.setText(tagsText);
            tagsTextView.setVisibility(View.VISIBLE);
        }

        // Dates texts.
        DateTimeTool tool = new DateTimeTool();
        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

        if (start == null && deadline == null) {
            datesTableLayout.setVisibility(View.GONE);
        } else {
            datesTableLayout.setVisibility(View.VISIBLE);

            if (start == null) {
                startTableRow.setVisibility(View.GONE);
                startTextView.setText("");
            } else {
                startTextView.setText(tool.getInterfaceFormattedDate(currentTask.getStart()));
                startTableRow.setVisibility(View.VISIBLE);
            }

            if (deadline == null) {
                deadlineTableRow.setVisibility(View.GONE);
                deadlineTextView.setText("");
            } else {
                deadlineTextView.setText(tool.getInterfaceFormattedDate(currentTask.getDeadline()));
                deadlineTableRow.setVisibility(View.VISIBLE);
            }
        }
    }

    // Returns "true" if this callback handled the event.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean eventHandled = false;
        List<Task> tasks = null;

        switch (item.getItemId()) {
            case R.id.taskMenu_status:
                tasks = new LinkedList<Task>();
                tasks.add(currentTask);

                ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment(tasks);
                statusFragment.setOnItemClickListener(TaskActivity.this.taskStatusChangeListener);
                statusFragment.show(getFragmentManager(), "change_task_status");

                eventHandled = true;
                break;
                
            case R.id.taskMenu_edit:
                // Open the task edition activity.
                long currentTaskId = currentTask.getId();

                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("task_id", currentTaskId);

                startActivity(intent);
                eventHandled = true;
                break;

            case R.id.taskMenu_delete:
                tasks = new LinkedList<Task>();
                tasks.add(currentTask);

                // Show a deletion confirmation dialog.
                DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment(tasks);
                deleteFragment.setOnDeleteListener(TaskActivity.this.deleteTaskListener);
                deleteFragment.show(getFragmentManager(), "delete_task");

                eventHandled = true;
                break;

            default:
                eventHandled = super.onOptionsItemSelected(item);
                break;
        }

        return eventHandled;
    }
}
