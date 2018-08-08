package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;

public class TaskActivity extends AppCompatActivity {
    private TextView titleTextView;
    private TextView tagsTextView;
    private View datesDivider;

    // Single
    private LinearLayout singleInformation;
    private TextView singleValueTextView;
    private TextView singleTimeZoneTextView;
    private ImageView singleDstImageView;

    // Start/End Dates
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

    // Broadcast receiver
    private AppBroadcastReceiver appBroadcastReceiver;

    private long currentTaskId;
    private Task currentTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);

        titleTextView = findViewById(R.id.task_title);
        tagsTextView = findViewById(R.id.task_tags);
        datesDivider = findViewById(R.id.task_dates_divider);

        // Single
        singleInformation = findViewById(R.id.task_single_information);
        singleValueTextView = findViewById(R.id.task_single_value);
        singleTimeZoneTextView = findViewById(R.id.task_single_time_zone);
        singleDstImageView = findViewById(R.id.task_single_dst);

        // Start/End Dates
        datesTableLayout = findViewById(R.id.task_dates);

        // Date 1
        date1TitleTextView = findViewById(R.id.task_date1_title);
        date1ValueTextView = findViewById(R.id.task_date1_value);
        date1TimeZoneTextView = findViewById(R.id.task_date1_time_zone);
        date1DstImageView = findViewById(R.id.task_date1_dst);

        // Date 2
        date2TableRow1 = findViewById(R.id.task_date2_row_1);

        date2TitleTextView = findViewById(R.id.task_date2_title);
        date2ValueTextView = findViewById(R.id.task_date2_value);

        date2TableRow2 = findViewById(R.id.task_date2_row_2);

        date2TimeZoneTextView = findViewById(R.id.task_date2_time_zone);
        date2DstImageView = findViewById(R.id.task_date2_dst);

        // Description
        descriptionTextView = findViewById(R.id.task_description);

        // Load task data
        Intent intent = getIntent();
        currentTaskId = intent.getLongExtra("task_id", -1);
        currentTask = null;

        updateInterface();

        // Broadcast receiver
        registerBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Broadcast receiver
        unregisterBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        appBroadcastReceiver = new AppBroadcastReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter intentFilter1 = new IntentFilter(ApplicationLogic.ACTION_DATA_CHANGED);
        IntentFilter intentFilter2 = new IntentFilter(ApplicationLogic.ACTION_DATA_CHANGED_TASK_DELETED);

        manager.registerReceiver(appBroadcastReceiver, intentFilter1);
        manager.registerReceiver(appBroadcastReceiver, intentFilter2);
    }

    private void unregisterBroadcastReceiver() {
        (LocalBroadcastManager.getInstance(this)).unregisterReceiver(appBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.task, menu);

        // This is necessary because the common drawables keep values previously set
        MenuItem editItem = menu.findItem(R.id.task_menu_edit);
        Drawable editItemIcon = editItem.getIcon();

        MenuItem deleteItem = menu.findItem(R.id.task_menu_delete);
        Drawable deleteItemIcon = deleteItem.getIcon();

        editItem.setEnabled(true);
        editItemIcon.setAlpha(255);

        deleteItem.setEnabled(true);
        deleteItemIcon.setAlpha(255);

        return true;
    }

    private void updateInterface() {
        ApplicationLogic applicationLogic = new ApplicationLogic(this);
        TextTool textTool = new TextTool();

        // Update Task object after possible changes
        currentTask = applicationLogic.getTask(currentTaskId);

        ActionBar bar = getSupportActionBar();

        if (bar != null) {
            if (currentTask.isDone()) bar.setSubtitle(R.string.closed_1);
            else bar.setSubtitle(R.string.open_1);
        }

        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());

        // Tags text
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

        // Dates texts
        TextTool tool = new TextTool();

        Calendar single = currentTask.getSingle();
        Calendar start = currentTask.getStart();
        Calendar end = currentTask.getEnd();

        // Single is defined
        if (single != null) {
            datesDivider.setVisibility(View.VISIBLE);
            singleInformation.setVisibility(View.VISIBLE);
            datesTableLayout.setVisibility(View.GONE);

            singleValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.SINGLE, false));

            TimeZone singleTimeZone = single.getTimeZone();
            singleTimeZoneTextView.setText(tool.getFormattedOffset(this, singleTimeZone, single));

            if (singleTimeZone.inDaylightTime(single.getTime())) singleDstImageView.setVisibility(View.VISIBLE);
            else singleDstImageView.setVisibility(View.GONE);
        }
        // Any of Start and End is defined
        else if (start != null || end != null) {
            datesDivider.setVisibility(View.VISIBLE);
            singleInformation.setVisibility(View.GONE);
            datesTableLayout.setVisibility(View.VISIBLE);

            // Both are defined
            if (start != null && end != null) {
                date2TableRow1.setVisibility(View.VISIBLE);
                date2TableRow2.setVisibility(View.VISIBLE);

                date1TitleTextView.setText(getString(R.string.start_2));
                date2TitleTextView.setText(getString(R.string.end_2));

                date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.START, false));
                date2ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.END, false));

                TimeZone startTimeZone = start.getTimeZone();
                TimeZone endTimeZone = end.getTimeZone();

                date1TimeZoneTextView.setText(tool.getFormattedOffset(this, startTimeZone, start));
                date2TimeZoneTextView.setText(tool.getFormattedOffset(this, endTimeZone, end));

                if (startTimeZone.inDaylightTime(start.getTime())) date1DstImageView.setVisibility(View.VISIBLE);
                else date1DstImageView.setVisibility(View.GONE);

                if (endTimeZone.inDaylightTime(end.getTime())) date2DstImageView.setVisibility(View.VISIBLE);
                else date2DstImageView.setVisibility(View.GONE);
            }
            // Only one is defined
            else {
                datesDivider.setVisibility(View.VISIBLE);
                date2TableRow1.setVisibility(View.GONE);
                date2TableRow2.setVisibility(View.GONE);

                if (start != null) {
                    date1TitleTextView.setText(getString(R.string.start_2));
                    date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.START, false));

                    TimeZone startTimeZone = start.getTimeZone();
                    date1TimeZoneTextView.setText(tool.getFormattedOffset(this, startTimeZone, start));

                    if (startTimeZone.inDaylightTime(start.getTime())) date1DstImageView.setVisibility(View.VISIBLE);
                    else date1DstImageView.setVisibility(View.GONE);
                }
                else { // end != null
                    date1TitleTextView.setText(getString(R.string.end_2));
                    date1ValueTextView.setText(tool.getTaskDateText(this, currentTask, false, TextTool.END, false));

                    TimeZone endTimeZone = end.getTimeZone();
                    date1TimeZoneTextView.setText(tool.getFormattedOffset(this, endTimeZone, end));

                    if (endTimeZone.inDaylightTime(end.getTime())) date2DstImageView.setVisibility(View.VISIBLE);
                    else date2DstImageView.setVisibility(View.GONE);
                }
            }
        }
        else { // No date is defined
            datesDivider.setVisibility(View.GONE);
            singleInformation.setVisibility(View.GONE);
            datesTableLayout.setVisibility(View.GONE);
        }
    }

    // Returns "true" if this callback handled the event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean eventHandled;
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
                statusFragment.show(getFragmentManager(), "change_task_status");

                eventHandled = true;
                break;

            case R.id.task_menu_edit:
                // Open the task edition activity
                long currentTaskId = currentTask.getId();

                Intent intent = new Intent(this, EditTaskActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("task_id", currentTaskId);

                startActivity(intent);
                eventHandled = true;
                break;

            case R.id.task_menu_delete:
                // Show a deletion confirmation dialog
                DeleteTaskDialogFragment deleteFragment = new DeleteTaskDialogFragment();

                arguments = new Bundle();
                taskIds = new long[1];
                taskIds[0] = currentTask.getId();
                arguments.putLongArray("task_ids", taskIds);

                deleteFragment.setArguments(arguments);
                deleteFragment.show(getFragmentManager(), "delete_task");

                eventHandled = true;
                break;

            default:
                eventHandled = super.onOptionsItemSelected(item);
                break;
        }

        return eventHandled;
    }

    private class AppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                if (action.equals(ApplicationLogic.ACTION_DATA_CHANGED)) {
                    TaskActivity.this.updateInterface();
                } else if (action.equals(ApplicationLogic.ACTION_DATA_CHANGED_TASK_DELETED)) {
                    TaskActivity.this.finish();
                }
            }
        }
    }
}
