package jajimenez.workpage;

import java.util.Locale;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
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
    private Calendar currentStartDate = null;
    private Calendar currentEndDate = null;

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
            currentTask.setWorkspaceId(intent.getLongExtra("task_context_id", -1));

            currentStartDate = Calendar.getInstance();
            currentEndDate = Calendar.getInstance();

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
        fromButton.setText(getInterfaceFormattedDate(currentStartDate));
        toButton.setText(getInterfaceFormattedDate(currentEndDate));
    }

    private String getInterfaceFormattedDate(Calendar calendar) {
        Locale locale = Locale.getDefault();

        String day = String.format(locale, "%02d", calendar.get(Calendar.DAY_OF_MONTH));
        String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale);
        String year = String.valueOf(calendar.get(Calendar.YEAR));

        return String.format(locale, "%s %s %s", day, month, year);
    }

    private String getIso8601FormattedDate(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return String.format("%d-%02d-%02d 00:00:00.000", year, month, day);
    }

    public void onSaveItemSelected(MenuItem item) {
        String title = (titleEditText.getText()).toString();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid = (!fromCheckBox.isEnabled() || !toCheckBox.isEnabled() || currentEndDate.compareTo(currentStartDate) >= 0);

        if (titleValid && datesValid) {
            String description = (descriptionEditText.getText()).toString();

            String startDateTime;
            if (fromCheckBox.isChecked()) startDateTime = getIso8601FormattedDate(currentStartDate);
            else startDateTime = "";

            String endDateTime;
            if (toCheckBox.isChecked()) endDateTime = getIso8601FormattedDate(currentEndDate);
            else endDateTime = "";

            // Update Current Task
            currentTask.setTitle(title);
            currentTask.setDescription(description);
            currentTask.setStartDateTime(startDateTime);
            currentTask.setEndDateTime(endDateTime);

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
        DialogFragment fragment = new DatePickerFragment(currentStartDate);
        fragment.show(getFragmentManager(), "from_date_picker");
    }

    public void onToButtonClicked(View view) {
        DialogFragment fragment = new DatePickerFragment(currentEndDate);
        fragment.show(getFragmentManager(), "to_date_picker");
    }

    private class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        private Calendar calendar;

        public DatePickerFragment(Calendar calendar) {
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
