package jajimenez.workpage;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends Activity {
    private EditText titleEditText = null;
    private EditText descriptionEditText = null;
    private CheckBox startCheckBox = null;
    private CheckBox deadlineCheckBox = null;
    private Button startButton = null;
    private Button deadlineButton = null;

    private ApplicationLogic applicationLogic = null;
    private Task currentTask = null;
    private Calendar selectedStartDate = null;
    private Calendar selectedDeadlineDate = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);

        titleEditText = (EditText) findViewById(R.id.edittask_title);
        descriptionEditText = (EditText) findViewById(R.id.edittask_description);
        startCheckBox = (CheckBox) findViewById(R.id.edittask_start_checkbox);
        deadlineCheckBox = (CheckBox) findViewById(R.id.edittask_deadline_checkbox);
        startButton = (Button) findViewById(R.id.edittask_start_button);
        deadlineButton = (Button) findViewById(R.id.edittask_deadline_button);

        applicationLogic = new ApplicationLogic(this);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");

        if (action != null && action.equals("edit")) {
            // ToDo
            setTitle(R.string.edit_task);
        }
        else {
            currentTask = new Task();
            currentTask.setTaskContextId(intent.getLongExtra("task_context_id", -1));

            selectedStartDate = Calendar.getInstance();
            selectedDeadlineDate = Calendar.getInstance();

            setTitle(R.string.new_task);
            startButton.setEnabled(false);
            deadlineButton.setEnabled(false);
        }

        updateInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_task, menu);

        return true;
    }

    private void updateInterface() {
        DateTimeTool tool = new DateTimeTool();
        
        startButton.setText(tool.getInterfaceFormattedDate(selectedStartDate));
        deadlineButton.setText(tool.getInterfaceFormattedDate(selectedDeadlineDate));
    }

    public void onSaveItemSelected(MenuItem item) {
        String title = (titleEditText.getText()).toString();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid = (!startCheckBox.isChecked() || !deadlineCheckBox.isChecked() || selectedDeadlineDate.compareTo(selectedStartDate) >= 0);

        if (titleValid && datesValid) {
            // Update Current Task
            currentTask.setTitle(title);
            currentTask.setDescription((descriptionEditText.getText()).toString());

            if (startCheckBox.isChecked()) currentTask.setStart(selectedStartDate);
            else currentTask.setStart(null);

            if (deadlineCheckBox.isChecked()) currentTask.setDeadline(selectedDeadlineDate);
            else currentTask.setDeadline(null);

            // Save Current Task
            applicationLogic.saveTask(currentTask);
            Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();

            // Close the activity
            finish();
        }
        else if (!titleValid) {
            Toast.makeText(this, R.string.title_error, Toast.LENGTH_SHORT).show();
        }
        else if (!datesValid) {
            Toast.makeText(this, R.string.dates_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void onStartCheckBoxClicked(View view) {
        startButton.setEnabled(startCheckBox.isChecked());
    }

    public void onDeadlineCheckBoxClicked(View view) {
        deadlineButton.setEnabled(deadlineCheckBox.isChecked());
    }

    public void onStartButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedStartDate);
        fragment.show(getFragmentManager(), "start_date_picker");
    }

    public void onDeadlineButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedDeadlineDate);
        fragment.show(getFragmentManager(), "deadline_date_picker");
    }

    private class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private Calendar calendar;

        public DatePickerDialogFragment(Calendar calendar) {
            super();
            this.calendar = calendar;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);

            EditTaskActivity.this.updateInterface();
        }
    }
}
