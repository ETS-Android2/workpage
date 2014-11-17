package jajimenez.workpage;

import java.util.Calendar;
import java.text.DateFormat;

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
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends Activity {
    private EditText titleEditText = null;
    private EditText descriptionEditText = null;
    private CheckBox fromCheckBox = null;
    private CheckBox toCheckBox = null;
    private Button fromButton = null;
    private Button toButton = null;

    private ApplicationLogic applicationLogic = null;
    private Task currentTask = null;
    private Calendar selectedFromDate = null;
    private Calendar selectedToDate = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task);

        titleEditText = (EditText)findViewById(R.id.title_edittext);
        descriptionEditText = (EditText)findViewById(R.id.description_edittext);
        fromCheckBox = (CheckBox)findViewById(R.id.from_checkbox);
        toCheckBox = (CheckBox)findViewById(R.id.to_checkbox);
        fromButton = (Button)findViewById(R.id.from_button);
        toButton = (Button)findViewById(R.id.to_button);

        applicationLogic = new ApplicationLogic(this);

        Intent intent = getIntent();
        String action = intent.getStringExtra("action");

        if (action != null && action.equals("edit")) {
            // ToDo
            setTitle(R.string.edit_task);
        } else {
            currentTask = new Task();
            currentTask.setTaskContextId(intent.getLongExtra("task_context_id", -1));

            selectedFromDate = Calendar.getInstance();
            selectedToDate = Calendar.getInstance();

            setTitle(R.string.new_task);
            fromButton.setEnabled(false);
            toButton.setEnabled(false);
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
        fromButton.setText(getInterfaceFormattedDate(selectedFromDate));
        toButton.setText(getInterfaceFormattedDate(selectedToDate));
    }

    private String getInterfaceFormattedDate(Calendar calendar) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
        return dateFormat.format(calendar.getTime());
    }

    public void onSaveItemSelected(MenuItem item) {
        String title = (titleEditText.getText()).toString();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid = (!fromCheckBox.isChecked() || !toCheckBox.isChecked() || selectedToDate.compareTo(selectedFromDate) >= 0);

        if (titleValid && datesValid) {
            // Update Current Task
            currentTask.setTitle(title);
            currentTask.setDescription((descriptionEditText.getText()).toString());

            if (fromCheckBox.isChecked()) currentTask.setStartDateTime(selectedFromDate);
            else currentTask.setStartDateTime(null);

            if (toCheckBox.isChecked()) currentTask.setEndDateTime(selectedToDate);
            else currentTask.setEndDateTime(null);

            // Save Current Task
            applicationLogic.saveTask(currentTask);
            Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT).show();

            // Close the activity
            finish();
        } else if (!titleValid) {
            Toast.makeText(this, R.string.title_error, Toast.LENGTH_SHORT).show();
        } else if (!datesValid) {
            Toast.makeText(this, R.string.dates_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void onFromCheckBoxClicked(View view) {
        fromButton.setEnabled(fromCheckBox.isChecked());
    }

    public void onToCheckBoxClicked(View view) {
        toButton.setEnabled(toCheckBox.isChecked());
    }

    public void onFromButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedFromDate);
        fragment.show(getFragmentManager(), "from_date_picker");
    }

    public void onToButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedToDate);
        fragment.show(getFragmentManager(), "to_date_picker");
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
