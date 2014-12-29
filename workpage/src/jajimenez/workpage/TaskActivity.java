package jajimenez.workpage;

import java.util.List;
import java.util.Calendar;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;

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

        // Load task data.
        Intent intent = getIntent();
        long taskId = intent.getLongExtra("task_id", -1);
        currentTask = (new ApplicationLogic(this)).getTask(taskId);

        updateInterface();
    }

    private void updateInterface() {
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
        }
        else {
            String tagsText = "";

            for (int i = 0; i < tagCount; i++) {
                tagsText += (tags.get(i)).getName();
                if (i < (tagCount - 1)) tagsText += ", ";
            }

            tagsTextView.setText(tagsText);
        }

        // Dates texts.
        DateTimeTool tool = new DateTimeTool();
        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

        if (start == null && deadline == null) {
            datesTableLayout.setVisibility(View.GONE);
        }
        else {
            if (start == null) startTableRow.setVisibility(View.GONE);
            else startTextView.setText(tool.getInterfaceFormattedDate(currentTask.getStart()));

            if (deadline == null) deadlineTableRow.setVisibility(View.GONE);
            else deadlineTextView.setText(tool.getInterfaceFormattedDate(currentTask.getDeadline()));
        }
    }
}
