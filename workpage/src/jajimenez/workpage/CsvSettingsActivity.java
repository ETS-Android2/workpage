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
    private CheckBox fieldNamesCheckBox;
    private CheckBox unixTimeCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.csv_settings);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);

        contextsRadioGroup = (RadioGroup) findViewById(R.id.csvSettings_contexts);
        fieldNamesCheckBox = (CheckBox) findViewById(R.id.csvSettings_fieldNames);
        unixTimeCheckBox = (CheckBox) findViewById(R.id.csvSettings_unixTime);

        final ApplicationLogic logic = new ApplicationLogic(this);
        long taskContextToSelectId = logic.getCsvTaskContextToSave();

        final List<TaskContext> contexts = logic.getAllTaskContexts();
        int contextCount = contexts.size();

        TaskContext c = null;
        RadioButton r = null;

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
                    logic.setCsvTaskContextToSave(taskContextId);
                }
            });

            contextsRadioGroup.addView(r);

            // We ensure that, if the task context to select is not
            // existing anymore, we select the first task context.
            if (i == 0 || c.getId() == taskContextToSelectId) r.setChecked(true);
        }

        fieldNamesCheckBox.setChecked(logic.getCsvFieldNames());
        unixTimeCheckBox.setChecked(logic.getCsvUnixTime());
    }

    public void onFieldNamesCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvFieldNames(fieldNamesCheckBox.isChecked());
    }

    public void onUnixTimeCheckBoxClicked(View view) {
        ApplicationLogic logic = new ApplicationLogic(this);
        logic.setCsvUnixTime(unixTimeCheckBox.isChecked());
    }
}
