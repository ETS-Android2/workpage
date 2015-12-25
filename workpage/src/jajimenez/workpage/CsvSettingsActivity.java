package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.CheckBox;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class CsvSettingsActivity extends Activity {
    private RadioGroup contextsRadioGroup;
    private RadioButton onlyOpenTasksRadioButton;
    private RadioButton onlyClosedTasksRadioButton;
    private RadioButton allTasksRadioButton;
    private CheckBox fieldNamesCheckBox;
    private CheckBox unixTimeCheckBox;
    private CheckBox idCheckBox;
    private CheckBox descriptionCheckBox;
    private CheckBox tagsCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.csv_settings);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);

        contextsRadioGroup = (RadioGroup) findViewById(R.id.csvSettings_contexts);
        onlyOpenTasksRadioButton = (RadioButton) findViewById(R.id.csvSettings_only_open_tasks);
        onlyClosedTasksRadioButton = (RadioButton) findViewById(R.id.csvSettings_only_closed_tasks);
        allTasksRadioButton = (RadioButton) findViewById(R.id.csvSettings_all_tasks);
        fieldNamesCheckBox = (CheckBox) findViewById(R.id.csvSettings_fieldNames);
        unixTimeCheckBox = (CheckBox) findViewById(R.id.csvSettings_unixTime);
        idCheckBox = (CheckBox) findViewById(R.id.csvSettings_id);
        descriptionCheckBox = (CheckBox) findViewById(R.id.csvSettings_description);
        tagsCheckBox = (CheckBox) findViewById(R.id.csvSettings_tags);

        final ApplicationLogic logic = new ApplicationLogic(this);
        long taskContextToSelectId = logic.getCsvTaskContextToExport();

        final List<TaskContext> contexts = logic.getAllTaskContexts();
        int contextCount = contexts.size();

        TaskContext c = null;
        RadioButton r = null;

        RadioGroup.LayoutParams radioButtonParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < contextCount; i++) {
            c = contexts.get(i);

            r = new RadioButton(this);
            r.setText(c.getName());

            r.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Get the radio button that is checked.
                    int radioButtonId = CsvSettingsActivity.this.contextsRadioGroup.getCheckedRadioButtonId();
                    RadioButton radioButton = (RadioButton) CsvSettingsActivity.this.findViewById(radioButtonId);

                    // Get the index/position of the radio button.
                    int index = CsvSettingsActivity.this.contextsRadioGroup.indexOfChild(radioButton);

                    // Get the ID of the task context that is represented by the radio button.
                    long taskContextId = (contexts.get(index)).getId();

                    // Save settings.
                    logic.setCsvTaskContextToExport(taskContextId);
                }
            });

            contextsRadioGroup.addView(r, radioButtonParams);

            // We ensure that, if the task context to select is not
            // existing anymore, we select the first task context.
            if (i == 0 || c.getId() == taskContextToSelectId) r.setChecked(true);
        }

        int tasksToExport = logic.getCsvTasksToExport();

        switch (tasksToExport) {
            case ApplicationLogic.ONLY_OPEN_TASKS:
                onlyOpenTasksRadioButton.setChecked(true);
                break;
            case ApplicationLogic.ONLY_CLOSED_TASKS:
                onlyClosedTasksRadioButton.setChecked(true);
                break;
            default: // ALL_TASKS
                allTasksRadioButton.setChecked(true);
                break;
        }

        fieldNamesCheckBox.setChecked(logic.getCsvFieldNames());
        unixTimeCheckBox.setChecked(logic.getCsvUnixTime());
        idCheckBox.setChecked(logic.getCsvId());
        descriptionCheckBox.setChecked(logic.getCsvDescription());
        tagsCheckBox.setChecked(logic.getCsvTags());
    }

    public void onOnlyOpenTasksRadioButtonClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvTasksToExport(ApplicationLogic.ONLY_OPEN_TASKS);
    }

    public void onOnlyClosedTasksRadioButtonClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvTasksToExport(ApplicationLogic.ONLY_CLOSED_TASKS);
    }

    public void onAllTasksRadioButtonClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvTasksToExport(ApplicationLogic.ALL_TASKS);
    }

    public void onFieldNamesCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvFieldNames(fieldNamesCheckBox.isChecked());
    }

    public void onUnixTimeCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvUnixTime(unixTimeCheckBox.isChecked());
    }

    public void onIdCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvId(idCheckBox.isChecked());
    }

    public void onDescriptionCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvDescription(descriptionCheckBox.isChecked());
    }

    public void onTagsCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvTags(tagsCheckBox.isChecked());
    }
}
