package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
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
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends Activity {
    private EditText titleEditText;
    private EditText descriptionEditText;
    private CheckBox startCheckBox;
    private CheckBox deadlineCheckBox;
    private Button startButton;
    private Button deadlineButton;
    private AutoCompleteTextView addTagAutoTextView;
    private Button addTagButton; 
    private LinearLayout addedTagsLinearLayout;

    private ApplicationLogic applicationLogic;
    private List<TaskTag> contextTags;
    private Task currentTask;
    private Calendar selectedStartDate;
    private Calendar selectedDeadlineDate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);

        titleEditText = (EditText) findViewById(R.id.editTask_title);
        descriptionEditText = (EditText) findViewById(R.id.editTask_description);
        startCheckBox = (CheckBox) findViewById(R.id.editTask_start_checkbox);
        deadlineCheckBox = (CheckBox) findViewById(R.id.editTask_deadline_checkbox);
        startButton = (Button) findViewById(R.id.editTask_start_button);
        deadlineButton = (Button) findViewById(R.id.editTask_deadline_button);
        addTagAutoTextView = (AutoCompleteTextView) findViewById(R.id.editTask_addTag_autotextview);
        addTagButton = (Button) findViewById(R.id.editTask_addTag_button);
        addedTagsLinearLayout = (LinearLayout) findViewById(R.id.editTask_addedTags);

        addTagAutoTextView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                boolean enabled = (text.length() > 0);

                if (enabled) {
                    // Auxiliar tag object.
                    TaskTag tag = new TaskTag();
                    tag.setName(text);

                    List<TaskTag> taskTags = currentTask.getTags();

                    enabled = !taskTags.contains(tag);
                }

                EditTaskActivity.this.addTagButton.setEnabled(enabled);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        addTagButton.setEnabled(false);

        applicationLogic = new ApplicationLogic(this);
        Intent intent = getIntent();
        String action = intent.getStringExtra("action");

        if (action != null && action.equals("edit")) {
            setTitle(R.string.edit_task);

            // Get task data.
            long taskId = intent.getLongExtra("task_id", -1);
            currentTask = applicationLogic.getTask(taskId);

            selectedStartDate = currentTask.getStart();
            boolean startDate = false;

            if (selectedStartDate == null) selectedStartDate = Calendar.getInstance();
            else startDate = true;

            selectedDeadlineDate = currentTask.getDeadline();
            boolean deadlineDate = false;

            if (selectedDeadlineDate == null) selectedDeadlineDate = Calendar.getInstance();
            else deadlineDate = true;

            // Update interface.
            titleEditText.setText(currentTask.getTitle());
            descriptionEditText.setText(currentTask.getDescription());
            startCheckBox.setChecked(startDate);
            startButton.setEnabled(startDate);
            deadlineCheckBox.setChecked(deadlineDate);
            deadlineButton.setEnabled(deadlineDate);
        }
        else {
            currentTask = new Task();
            long contextId = intent.getLongExtra("task_context_id", -1);
            currentTask.setContextId(contextId);

            selectedStartDate = Calendar.getInstance();
            selectedDeadlineDate = Calendar.getInstance();

            setTitle(R.string.new_task);
            startButton.setEnabled(false);
            deadlineButton.setEnabled(false);
        }

        TaskContext context = applicationLogic.getTaskContext(currentTask.getContextId());
        contextTags = applicationLogic.getAllTaskTags(context);

        setTagCompletionSuggestions();
        if (action.equals("edit")) addInitialTaskTagViews();

        updateInterface();
    }

    private void setTagCompletionSuggestions() {
        TaskContext context = applicationLogic.getTaskContext(currentTask.getContextId());
        List<TaskTag> suggestedTags = new LinkedList<TaskTag>(contextTags);
        suggestedTags.removeAll(currentTask.getTags());

        ArrayAdapter<TaskTag> adapter = new ArrayAdapter<TaskTag>(this, android.R.layout.simple_dropdown_item_1line, suggestedTags);
        addTagAutoTextView.setAdapter(adapter);
    }

    private void addInitialTaskTagViews() {
        List<TaskTag> tags = currentTask.getTags();
        for (TaskTag tag : tags) addTaskTagView(tag, tags);
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
            (Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT)).show();

            // Close the activity
            setResult(RESULT_OK);
            finish();
        }
        else if (!titleValid) {
            (Toast.makeText(this, R.string.title_error, Toast.LENGTH_SHORT)).show();
        }
        else if (!datesValid) {
            (Toast.makeText(this, R.string.dates_error, Toast.LENGTH_SHORT)).show();
        }
    }

    public void onStartCheckBoxClicked(View view) {
        startButton.setEnabled(startCheckBox.isChecked());
    }

    public void onDeadlineCheckBoxClicked(View view) {
        deadlineButton.setEnabled(deadlineCheckBox.isChecked());
    }

    public void onStartButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedStartDate, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                EditTaskActivity.this.selectedStartDate.set(Calendar.YEAR, year);
                EditTaskActivity.this.selectedStartDate.set(Calendar.MONTH, month);
                EditTaskActivity.this.selectedStartDate.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        });

        fragment.show(getFragmentManager(), "start_date_picker");
    }

    public void onDeadlineButtonClicked(View view) {
        DialogFragment fragment = new DatePickerDialogFragment(selectedDeadlineDate, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                EditTaskActivity.this.selectedDeadlineDate.set(Calendar.YEAR, year);
                EditTaskActivity.this.selectedDeadlineDate.set(Calendar.MONTH, month);
                EditTaskActivity.this.selectedDeadlineDate.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        });

        fragment.show(getFragmentManager(), "deadline_date_picker");
    }

    public void onAddTagButtonClicked(View view) {
        String name = ((addTagAutoTextView.getText()).toString()).trim();
        TaskTag tag = new TaskTag(currentTask.getContextId(), name, 0, null);
        List<TaskTag> taskTags = currentTask.getTags();

        // Add the new tag to the tag list of the current task.
        taskTags.add(tag);

        // Add a new tag view.
        addTaskTagView(tag, taskTags);

        // Clear the text box.
        addTagAutoTextView.setText("");
    }

    private void addTaskTagView(final TaskTag tag, final List<TaskTag> taskTags) {
        // Remove the new tag from the suggestions (if the tag already existed). This is by removing it
        // from the "suggestedTags" object, as that object is the source for the sugestions adapter.
        final ArrayAdapter<TaskTag> suggestedTagsAdapter = (ArrayAdapter<TaskTag>) addTagAutoTextView.getAdapter();
        suggestedTagsAdapter.remove(tag);

        final TaskTagView tagView = new TaskTagView(this);
        tagView.setText(tag.getName());

        tagView.setOnRemoveIconClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Remove the tag from the tag list of the current task.
                taskTags.remove(tag);

                // Add the tag to the suggestions, if it previously existed in the
                // current task context. This is by adding it to the "suggestedTags"
                // object, as that object is the source for the sugestions adapter.
                if (contextTags.contains(tag)) suggestedTagsAdapter.add(tag);

                // Remove the tag view.
                addedTagsLinearLayout.removeView(tagView);

                if ((((EditTaskActivity.this.addTagAutoTextView.getText()).toString()).trim()).equals(tag.getName())) {
                    EditTaskActivity.this.addTagButton.setEnabled(true);
                }
            }
        });

        // Add the tag view and clear the text box.
        addedTagsLinearLayout.addView(tagView);
    }
}
