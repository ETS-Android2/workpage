package jajimenez.workpage;

import java.util.List;
import java.util.Calendar;
import java.util.TimeZone;

import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskTag;

public class TaskActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView tagsTextView;

    // When
    private LinearLayout whenInformation;
    private TextView whenValueTextView;
    private TextView whenTimeZoneTextView;
    private ImageView whenDstImageView;

    // Start/Deadline Dates
    private TableLayout datesTableLayout;

    // Date 1
    private TextView date1TitleTextView;
    private TextView date1ValueTextView;
    private TextView date1TimeZoneTextView;
    private ImageView date1DstImageView;

    // Date 2
    private TableRow date2TableRow1;

    private TextView date2TitleTextView;
    private TextView date2ValueTextView;

    private TableRow date2TableRow2;

    private TextView date2TimeZoneTextView;
    private ImageView date2DstImageView;

    // Description
    private TextView descriptionTextView;

    private ChangeTaskStatusDialogFragment.OnItemClickListener taskStatusChangeListener;
    private DeleteTaskDialogFragment.OnDeleteListener deleteTaskListener;

    private long currentTaskId;
    private Task currentTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);

        titleTextView = (TextView) findViewById(R.id.task_title);
        tagsTextView = (TextView) findViewById(R.id.task_tags);

        // When
        whenInformation = (LinearLayout) findViewById(R.id.task_when_information);
        whenValueTextView = (TextView) findViewById(R.id.task_when_value);
        whenTimeZoneTextView = (TextView) findViewById(R.id.task_when_time_zone);
        whenDstImageView = (ImageView) findViewById(R.id.task_when_dst);

        // Start/Deadline Dates
        datesTableLayout = (TableLayout) findViewById(R.id.task_dates);

        // Date 1
        date1TitleTextView = (TextView) findViewById(R.id.task_date1_title);
        date1ValueTextView = (TextView) findViewById(R.id.task_date1_value);
        date1TimeZoneTextView = (TextView) findViewById(R.id.task_date1_time_zone);
        date1DstImageView = (ImageView) findViewById(R.id.task_date1_dst);

        // Date 2
        date2TableRow1 = (TableRow) findViewById(R.id.task_date2_row_1);

        date2TitleTextView = (TextView) findViewById(R.id.task_date2_title);
        date2ValueTextView = (TextView) findViewById(R.id.task_date2_value);

        date2TableRow2 = (TableRow) findViewById(R.id.task_date2_row_2);

        date2TimeZoneTextView = (TextView) findViewById(R.id.task_date2_time_zone);
        date2DstImageView = (ImageView) findViewById(R.id.task_date2_dst);

        // Description
        descriptionTextView = (TextView) findViewById(R.id.task_description);

        taskStatusChangeListener = new ChangeTaskStatusDialogFragment.OnItemClickListener() {
            public void onItemClick() {
                // Set the result as OK for making Main Activity update its interface
                setResult(RESULT_OK);

                // Update the list view.
                TaskActivity.this.updateInterface();
            }
        };

        deleteTaskListener = new DeleteTaskDialogFragment.OnDeleteListener() {
            public void onDelete() {
                // Set the result as OK for making Main Activity update its interface
                setResult(RESULT_OK);

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

        updateInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task, menu);

        // This is necessary because the drawable of the Edit item icon of this activity keeps
        // the last alpha value set for the Edit item icon in MainActivity. MainActivity has
        // the same menu with the same drawables for the items.
        MenuItem editItem = menu.findItem(R.id.task_menu_edit);
        Drawable editItemIcon = editItem.getIcon();
        editItemIcon.setAlpha(255);

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ApplicationLogic.CHANGE_TASKS && resultCode == RESULT_OK) {
            setResult(RESULT_OK);
            updateInterface();
        }
    }

    private void updateInterface() {
        ApplicationLogic applicationLogic = new ApplicationLogic(this);
        TextTool textTool = new TextTool();

        // Update Task object after possible changes.
        currentTask = applicationLogic.getTask(currentTaskId);

        ActionBar bar = getSupportActionBar();

        if (currentTask.isDone()) bar.setSubtitle(R.string.closed_1);
        else bar.setSubtitle(R.string.open_1);

        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());

        // Tags text.
        List<TaskTag> tags = currentTask.getTags();

        int tagCount = 0;
        if (tags != null) tagCount = tags.size();

        if (tagCount == 0) {
            tagsTextView.setText("");
            tagsTextView.setVisibility(View.GONE);
        } else {
            tagsTextView.setText(textTool.getTagsText(this, currentTask));
            tagsTextView.setVisibility(View.VISIBLE);
        }

        // Dates texts.
        TextTool tool = new TextTool();

        Calendar when = currentTask.getWhen();
        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

        // When is defined.
        if (when != null) {
            whenInformation.setVisibility(View.VISIBLE);
            datesTableLayout.setVisibility(View.GONE);

            whenValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.WHEN, false));

            TimeZone whenTimeZone = when.getTimeZone();
            whenTimeZoneTextView.setText(tool.getFormattedTimeZone(this, whenTimeZone, when, TextTool.LONG));

            if (whenTimeZone.inDaylightTime(when.getTime())) whenDstImageView.setVisibility(View.VISIBLE);
            else whenDstImageView.setVisibility(View.GONE);
        }
        // Any of Start and Deadline is defined.
        else if (start != null || deadline != null) {
            whenInformation.setVisibility(View.GONE);
            datesTableLayout.setVisibility(View.VISIBLE);

            // Both are defined.
            if (start != null && deadline != null) {
                date2TableRow1.setVisibility(View.VISIBLE);
                date2TableRow2.setVisibility(View.VISIBLE);

                date1TitleTextView.setText(getString(R.string.start_2));
                date2TitleTextView.setText(getString(R.string.deadline_2));

                date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.START, false));
                date2ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.DEADLINE, false));

                TimeZone startTimeZone = start.getTimeZone();
                TimeZone deadlineTimeZone = deadline.getTimeZone();

                date1TimeZoneTextView.setText(tool.getFormattedTimeZone(this, startTimeZone, start, TextTool.LONG));
                date2TimeZoneTextView.setText(tool.getFormattedTimeZone(this, deadlineTimeZone, deadline, TextTool.LONG));

                if (startTimeZone.inDaylightTime(start.getTime())) date1DstImageView.setVisibility(View.VISIBLE);
                else date1DstImageView.setVisibility(View.GONE);

                if (deadlineTimeZone.inDaylightTime(deadline.getTime())) date2DstImageView.setVisibility(View.VISIBLE);
                else date2DstImageView.setVisibility(View.GONE);
            }
            // Only one is defined.
            else {
                date2TableRow1.setVisibility(View.VISIBLE);
                date2TableRow2.setVisibility(View.GONE);

                if (start != null) {
                    date1TitleTextView.setText(getString(R.string.start_2));
                    date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.START, false));

                    TimeZone startTimeZone = start.getTimeZone();
                    date1TimeZoneTextView.setText(tool.getFormattedTimeZone(this, startTimeZone, start, TextTool.LONG));

                    if (startTimeZone.inDaylightTime(start.getTime())) date1DstImageView.setVisibility(View.VISIBLE);
                    else date1DstImageView.setVisibility(View.GONE);
                }
                else { // deadline != null
                    date1TitleTextView.setText(getString(R.string.deadline_2));
                    date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.DEADLINE, false));

                    TimeZone deadlineTimeZone = deadline.getTimeZone();
                    date1TimeZoneTextView.setText(tool.getFormattedTimeZone(this, deadlineTimeZone, deadline, TextTool.LONG));

                    if (deadlineTimeZone.inDaylightTime(deadline.getTime())) date2DstImageView.setVisibility(View.VISIBLE);
                    else date2DstImageView.setVisibility(View.GONE);
                }
            }
        }
        else { // No date is defined.
            whenInformation.setVisibility(View.GONE);
            datesTableLayout.setVisibility(View.GONE);
        }
    }

    // Returns "true" if this callback handled the event.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean eventHandled = false;
        Bundle arguments;
        long[] taskIds;
        int id = item.getItemId();

        switch (id) {
            case R.id.task_menu_status:
                ChangeTaskStatusDialogFragment statusFragment = new ChangeTaskStatusDialogFragment();

                arguments = new Bundle();
                taskIds = new long[1];
                taskIds[0] = currentTask.getId();
                arguments.putLongArray("task_ids", taskIds);

                statusFragment.setArguments(arguments);
                statusFragment.setOnItemClickListener(TaskActivity.this.taskStatusChangeListener);
                statusFragment.show(getFragmentManager(), "change_task_status");

                eventHandled = true;
                break;

            case R.id.task_menu_edit:
                // Open the task edition activity.
                long currentTaskId = currentTask.getId();

                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("task_id", currentTaskId);

                startActivityForResult(intent, ApplicationLogic.CHANGE_TASKS);

                eventHandled = true;
                break;

            case R.id.task_menu_delete:
                // Show a deletion confirmation dialog.
                DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment();

                arguments = new Bundle();
                taskIds = new long[1];
                taskIds[0] = currentTask.getId();
                arguments.putLongArray("task_ids", taskIds);

                deleteFragment.setArguments(arguments);
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