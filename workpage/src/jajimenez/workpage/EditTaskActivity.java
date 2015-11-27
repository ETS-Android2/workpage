package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.DatePicker;
import android.widget.TimePicker;
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

    private RadioButton noDateRadioButton;
    private RadioButton whenRadioButton;

    private TextView whenDateTextView;
    private Button whenDateButton;
    private CheckBox whenTimeCheckBox;
    private Button whenTimeButton;
    private CheckBox whenRemindersCheckBox;
    private Button whenRemindersButton;

    private RadioButton dateRangeRadioButton;

    private TextView startTitleTextView;
    private CheckBox startDateCheckBox;
    private Button startDateButton;
    private CheckBox startTimeCheckBox;
    private Button startTimeButton;
    private CheckBox startRemindersCheckBox;
    private Button startRemindersButton;

    private TextView deadlineTitleTextView;
    private CheckBox deadlineDateCheckBox;
    private Button deadlineDateButton;
    private CheckBox deadlineTimeCheckBox;
    private Button deadlineTimeButton;
    private CheckBox deadlineRemindersCheckBox;
    private Button deadlineRemindersButton;

    private AutoCompleteTextView addTagAutoTextView;
    private Button addTagButton; 
    private LinearLayout addedTagsLinearLayout;

    private DatePickerDialogFragment.OnDateSetListener onWhenDateSetListener;
    private TimePickerDialogFragment.OnTimeSetListener onWhenTimeSetListener;

    private DatePickerDialogFragment.OnDateSetListener onStartDateSetListener;
    private TimePickerDialogFragment.OnTimeSetListener onStartTimeSetListener;

    private DatePickerDialogFragment.OnDateSetListener onDeadlineDateSetListener;
    private TimePickerDialogFragment.OnTimeSetListener onDeadlineTimeSetListener;

    private ApplicationLogic applicationLogic = null;
    private Task currentTask = null;
    private List<TaskTag> contextTags = null;

    private Calendar selectedWhen = null;
    private Calendar selectedStart = null;
    private Calendar selectedDeadline = null;

    private final static int NO_DATE = 0;
    private final static int WHEN = 1;
    private final static int DATE_RANGE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);

        titleEditText = (EditText) findViewById(R.id.editTask_title);
        descriptionEditText = (EditText) findViewById(R.id.editTask_description);

        noDateRadioButton = (RadioButton) findViewById(R.id.editTask_noDate_radioButton);
        whenRadioButton = (RadioButton) findViewById(R.id.editTask_when_radioButton);

        whenDateTextView = (TextView) findViewById(R.id.editTask_when_date_textView);
        whenDateButton = (Button) findViewById(R.id.editTask_when_date_button);
        whenTimeCheckBox = (CheckBox) findViewById(R.id.editTask_when_time_checkBox);
        whenTimeButton = (Button) findViewById(R.id.editTask_when_time_button);
        whenRemindersCheckBox = (CheckBox) findViewById(R.id.editTask_when_reminders_checkBox);
        whenRemindersButton = (Button) findViewById(R.id.editTask_when_reminders_button);

        dateRangeRadioButton = (RadioButton) findViewById(R.id.editTask_dateRange_radioButton);

        startTitleTextView = (TextView) findViewById(R.id.editTask_start_title_textView);
        startDateCheckBox = (CheckBox) findViewById(R.id.editTask_start_date_checkBox);
        startDateButton = (Button) findViewById(R.id.editTask_start_date_button);
        startTimeCheckBox = (CheckBox) findViewById(R.id.editTask_start_time_checkBox);
        startTimeButton = (Button) findViewById(R.id.editTask_start_time_button);
        startRemindersCheckBox = (CheckBox) findViewById(R.id.editTask_start_reminders_checkBox);
        startRemindersButton = (Button) findViewById(R.id.editTask_start_reminders_button);

        deadlineTitleTextView = (TextView) findViewById(R.id.editTask_deadline_title_textView);
        deadlineDateCheckBox = (CheckBox) findViewById(R.id.editTask_deadline_date_checkBox);
        deadlineDateButton = (Button) findViewById(R.id.editTask_deadline_date_button);
        deadlineTimeCheckBox = (CheckBox) findViewById(R.id.editTask_deadline_time_checkBox);
        deadlineTimeButton = (Button) findViewById(R.id.editTask_deadline_time_button);
        deadlineRemindersCheckBox = (CheckBox) findViewById(R.id.editTask_deadline_reminders_checkBox);
        deadlineRemindersButton = (Button) findViewById(R.id.editTask_deadline_reminders_button);

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

        onWhenDateSetListener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                EditTaskActivity.this.selectedWhen.set(Calendar.YEAR, year);
                EditTaskActivity.this.selectedWhen.set(Calendar.MONTH, month);
                EditTaskActivity.this.selectedWhen.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        onWhenTimeSetListener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                EditTaskActivity.this.selectedWhen.set(Calendar.HOUR_OF_DAY, hour);
                EditTaskActivity.this.selectedWhen.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.updateInterface();
            }
        };

        onStartDateSetListener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                EditTaskActivity.this.selectedStart.set(Calendar.YEAR, year);
                EditTaskActivity.this.selectedStart.set(Calendar.MONTH, month);
                EditTaskActivity.this.selectedStart.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        onStartTimeSetListener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                EditTaskActivity.this.selectedStart.set(Calendar.HOUR_OF_DAY, hour);
                EditTaskActivity.this.selectedStart.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.updateInterface();
            }
        };

        onDeadlineDateSetListener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                EditTaskActivity.this.selectedDeadline.set(Calendar.YEAR, year);
                EditTaskActivity.this.selectedDeadline.set(Calendar.MONTH, month);
                EditTaskActivity.this.selectedDeadline.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        onDeadlineTimeSetListener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                EditTaskActivity.this.selectedDeadline.set(Calendar.HOUR_OF_DAY, hour);
                EditTaskActivity.this.selectedDeadline.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.updateInterface();
            }
        };

        addTagButton.setEnabled(false);

        applicationLogic = new ApplicationLogic(this);
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        int selectedDateOption = 0;
        boolean whenTime = false;
        boolean whenReminders = false;
        boolean startDate = false;
        boolean startTime = false;
        boolean startReminders = false;
        boolean deadlineDate = false;
        boolean deadlineTime = false;
        boolean deadlineReminders = false;

        if (mode != null && mode.equals("edit")) {
            // Get task data.
            long taskId = intent.getLongExtra("task_id", -1);
            currentTask = applicationLogic.getTask(taskId);

            // Update interface.
            setTitle(R.string.edit_task);
            titleEditText.setText(currentTask.getTitle());
            descriptionEditText.setText(currentTask.getDescription());
        }
        else {
            long contextId = intent.getLongExtra("task_context_id", -1);

            currentTask = new Task();
            currentTask.setContextId(contextId);

            // Update interface.
            setTitle(R.string.new_task);
        }

        if (savedInstanceState == null) {
            DateTimeTool tool = new DateTimeTool();

            selectedWhen = Calendar.getInstance();
            tool.clearTimeFields(selectedWhen);

            selectedStart = Calendar.getInstance();
            tool.clearTimeFields(selectedStart);

            selectedDeadline = Calendar.getInstance();
            tool.clearTimeFields(selectedDeadline);

            Calendar when = currentTask.getWhen();
            Calendar start = currentTask.getStart();
            Calendar deadline = currentTask.getDeadline();

            if (when == null && start == null && deadline == null) {
                selectedDateOption = NO_DATE;
            }
            else if (when == null) {
                selectedDateOption = DATE_RANGE;

                if (start != null) {
                    selectedStart = start;
                    startDate = true;
                    startTime = !currentTask.getIgnoreStartTime();
                    // ToDo: startReminders
                }

                if (deadline != null) {
                    selectedDeadline = deadline;
                    deadlineDate = true;
                    deadlineTime = !currentTask.getIgnoreDeadlineTime();
                    // ToDo: deadlineReminders
                }
            }
            else {
                selectedDateOption = WHEN;
                selectedWhen = when;
                whenTime = !currentTask.getIgnoreWhenTime();
                // ToDo: whenReminders
            }
        }
        else {
            DatePickerDialogFragment whenDateFragment = (DatePickerDialogFragment) (getFragmentManager()).findFragmentByTag("when_date_picker");
            if (whenDateFragment != null) whenDateFragment.setOnDateSetListener(onWhenDateSetListener);

            TimePickerDialogFragment whenTimeFragment = (TimePickerDialogFragment) (getFragmentManager()).findFragmentByTag("when_time_picker");
            if (whenTimeFragment != null) whenTimeFragment.setOnTimeSetListener(onWhenTimeSetListener);

            DatePickerDialogFragment startDateFragment = (DatePickerDialogFragment) (getFragmentManager()).findFragmentByTag("start_date_picker");
            if (startDateFragment != null) startDateFragment.setOnDateSetListener(onStartDateSetListener);

            TimePickerDialogFragment startTimeFragment = (TimePickerDialogFragment) (getFragmentManager()).findFragmentByTag("start_time_picker");
            if (startTimeFragment != null) startTimeFragment.setOnTimeSetListener(onStartTimeSetListener);

            DatePickerDialogFragment deadlineDateFragment = (DatePickerDialogFragment) (getFragmentManager()).findFragmentByTag("deadline_date_picker");
            if (deadlineDateFragment != null) deadlineDateFragment.setOnDateSetListener(onDeadlineDateSetListener);

            TimePickerDialogFragment deadlineTimeFragment = (TimePickerDialogFragment) (getFragmentManager()).findFragmentByTag("deadline_time_picker");
            if (deadlineTimeFragment != null) deadlineTimeFragment.setOnTimeSetListener(onDeadlineTimeSetListener);

            selectedDateOption = savedInstanceState.getInt("selected_date_option");
            whenTime = savedInstanceState.getBoolean("when_time");
            whenReminders = savedInstanceState.getBoolean("when_reminders");
            startDate = savedInstanceState.getBoolean("start_date");
            startTime = savedInstanceState.getBoolean("start_time");
            startReminders = savedInstanceState.getBoolean("start_reminders");
            deadlineDate = savedInstanceState.getBoolean("deadline_date");
            deadlineTime = savedInstanceState.getBoolean("deadline_time");
            deadlineReminders = savedInstanceState.getBoolean("deadline_reminders");

            selectedWhen = getSavedCalendar(savedInstanceState, "when");
            selectedStart = getSavedCalendar(savedInstanceState, "start");
            selectedDeadline = getSavedCalendar(savedInstanceState, "deadline");
        }

        switch(selectedDateOption) {
            case WHEN:
                whenRadioButton.setChecked(true);
                break;
            case DATE_RANGE:
                dateRangeRadioButton.setChecked(true);
                break;
            default:
                noDateRadioButton.setChecked(true);
                break;
        }

        whenTimeCheckBox.setChecked(whenTime);
        whenRemindersCheckBox.setChecked(whenReminders);
        startDateCheckBox.setChecked(startDate);
        startTimeCheckBox.setChecked(startTime);
        startRemindersCheckBox.setChecked(startReminders);
        deadlineDateCheckBox.setChecked(deadlineDate);
        deadlineTimeCheckBox.setChecked(deadlineTime);
        deadlineRemindersCheckBox.setChecked(deadlineReminders);

        updateOnDateOptionChanged();

        TaskContext context = applicationLogic.getTaskContext(currentTask.getContextId());
        contextTags = applicationLogic.getAllTaskTags(context);

        if (savedInstanceState != null) currentTask.setTags(getSavedAddedTaskTags(savedInstanceState));

        setTagCompletionSuggestions();
        addInitialTaskTagViews();

        updateInterface();
    }

    private Calendar getSavedCalendar(Bundle savedInstanceState, String dateType) {
        long dateTime = savedInstanceState.getLong(dateType);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);

        return calendar;
    }

    private List<TaskTag> getSavedAddedTaskTags(Bundle savedInstanceState) {
        LinkedList<TaskTag> tags = new LinkedList<TaskTag>();
        String[] tagNames = savedInstanceState.getStringArray("task_tags");
        
        if (tagNames != null) {
            for (String name : tagNames) {
                TaskTag tag = new TaskTag();
                tag.setName(name);

                tags.add(tag);
            }
        }

        return tags;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Dates radiobuttons and checkboxes status.
        if (whenRadioButton.isChecked()) outState.putInt("selected_date_option", WHEN);
        else if (dateRangeRadioButton.isChecked()) outState.putInt("selected_date_option", DATE_RANGE);
        else outState.putInt("selected_date_option", NO_DATE);

        outState.putBoolean("when_time", whenTimeCheckBox.isChecked());
        outState.putBoolean("when_reminders", whenRemindersCheckBox.isChecked());
        outState.putBoolean("start_date", startDateCheckBox.isChecked());
        outState.putBoolean("start_time", startTimeCheckBox.isChecked());
        outState.putBoolean("start_reminders", startRemindersCheckBox.isChecked());
        outState.putBoolean("deadline_date", deadlineDateCheckBox.isChecked());
        outState.putBoolean("deadline_time", deadlineTimeCheckBox.isChecked());
        outState.putBoolean("deadline_reminders", deadlineRemindersCheckBox.isChecked());

        // Selected dates.
        outState.putLong("when", selectedWhen.getTimeInMillis());
        outState.putLong("start", selectedStart.getTimeInMillis());
        outState.putLong("deadline", selectedDeadline.getTimeInMillis());
        
        // Task tags.
        List<TaskTag> tags = currentTask.getTags();
        int tagCount = tags.size();
        String[] tagNames = new String[tagCount];
        for (int i = 0; i < tagCount; i++) tagNames[i] = (tags.get(i)).getName();

        outState.putStringArray("task_tags", tagNames);
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

        whenDateButton.setText(tool.getFormattedDate(selectedWhen));
        whenTimeButton.setText(tool.getFormattedTime(selectedWhen));

        startDateButton.setText(tool.getFormattedDate(selectedStart));
        startTimeButton.setText(tool.getFormattedTime(selectedStart));

        deadlineDateButton.setText(tool.getFormattedDate(selectedDeadline));
        deadlineTimeButton.setText(tool.getFormattedTime(selectedDeadline));
    }

    public void onSaveItemSelected(MenuItem item) {
        String title = (titleEditText.getText()).toString();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid = (noDateRadioButton.isChecked() ||
            whenRadioButton.isChecked() ||
            (startDateCheckBox.isChecked() && !deadlineDateCheckBox.isChecked()) ||
            (!startDateCheckBox.isChecked() && deadlineDateCheckBox.isChecked()) ||
            (startDateCheckBox.isChecked() && deadlineDateCheckBox.isChecked() &&
                compareCalendars(selectedDeadline, !deadlineTimeCheckBox.isChecked(), selectedStart, !startTimeCheckBox.isChecked()) >= 0));

        if (titleValid && datesValid) {
            // Update Current Task
            currentTask.setTitle(title);
            currentTask.setDescription((descriptionEditText.getText()).toString());

            Calendar when = null;
            boolean ignoreWhenTime = false;

            Calendar start = null;
            boolean ignoreStartTime = false;

            Calendar deadline = null;
            boolean ignoreDeadlineTime = false;

            DateTimeTool tool = new DateTimeTool();

            if (whenRadioButton.isChecked()) {
                when = Calendar.getInstance();
                when.setTimeInMillis(selectedWhen.getTimeInMillis());

                ignoreWhenTime = !whenTimeCheckBox.isChecked();

                if (ignoreWhenTime) tool.clearTimeFields(when);
            }
            else if (dateRangeRadioButton.isChecked()) {
                if (startDateCheckBox.isChecked()) {
                    start = Calendar.getInstance();
                    start.setTimeInMillis(selectedStart.getTimeInMillis());

                    ignoreStartTime = !startTimeCheckBox.isChecked();

                    if (ignoreStartTime) tool.clearTimeFields(start);
                }

                if (deadlineDateCheckBox.isChecked()) {
                    deadline = Calendar.getInstance();
                    deadline.setTimeInMillis(selectedDeadline.getTimeInMillis());

                    ignoreDeadlineTime = !deadlineTimeCheckBox.isChecked();

                    if (ignoreDeadlineTime) tool.clearTimeFields(deadline);
                }
            }

            currentTask.setWhen(when);
            currentTask.setIgnoreWhenTime(ignoreWhenTime);

            currentTask.setStart(start);
            currentTask.setIgnoreStartTime(ignoreStartTime);

            currentTask.setDeadline(deadline);
            currentTask.setIgnoreDeadlineTime(ignoreDeadlineTime);

            // Save Current Task
            applicationLogic.saveTask(currentTask);
            (Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT)).show();

            // Close the activity
            finish();
        }
        else if (!titleValid) {
            (Toast.makeText(this, R.string.title_error, Toast.LENGTH_SHORT)).show();
        }
        else if (!datesValid) {
            (Toast.makeText(this, R.string.dates_error, Toast.LENGTH_SHORT)).show();
        }
    }

    private int compareCalendars(Calendar a, boolean ignoreATime, Calendar b, boolean ignoreBTime) {
        int result = 0;

        if (a != null && b != null) {
            DateTimeTool tool = new DateTimeTool();

            Calendar a2 = Calendar.getInstance();
            a2.setTimeInMillis(a.getTimeInMillis());
            if (ignoreATime) tool.clearTimeFields(a2);

            Calendar b2 = Calendar.getInstance();
            b2.setTimeInMillis(b.getTimeInMillis());
            if (ignoreBTime) tool.clearTimeFields(b2);

            result = a2.compareTo(b2);
        }

        return result;
    }

    public void onNoDateRadioButtonClicked(View view) {
        updateOnDateOptionChanged();
    }

    public void onWhenRadioButtonClicked(View view) {
        updateOnDateOptionChanged();
    }

    public void onDateRangeRadioButtonClicked(View view) {
        updateOnDateOptionChanged();
    }

    private void updateOnDateOptionChanged() {
        boolean when = whenRadioButton.isChecked();
        boolean dateRange = dateRangeRadioButton.isChecked();

        whenDateTextView.setEnabled(when);
        whenDateButton.setEnabled(when);
        whenTimeCheckBox.setEnabled(when);
        whenTimeButton.setEnabled(when && whenTimeCheckBox.isChecked());
        whenRemindersCheckBox.setEnabled(when);
        whenRemindersButton.setEnabled(when && whenRemindersCheckBox.isChecked());

        startTitleTextView.setEnabled(dateRange);
        startDateCheckBox.setEnabled(dateRange);
        startDateButton.setEnabled(dateRange && startDateCheckBox.isChecked());
        startTimeCheckBox.setEnabled(dateRange && startDateCheckBox.isChecked());
        startTimeButton.setEnabled(dateRange && startDateCheckBox.isChecked() && startTimeCheckBox.isChecked());
        startRemindersCheckBox.setEnabled(dateRange && startDateCheckBox.isChecked());
        startRemindersButton.setEnabled(dateRange && startDateCheckBox.isChecked() && startRemindersCheckBox.isChecked());

        deadlineTitleTextView.setEnabled(dateRange);
        deadlineDateCheckBox.setEnabled(dateRange);
        deadlineDateButton.setEnabled(dateRange && deadlineDateCheckBox.isChecked());
        deadlineTimeCheckBox.setEnabled(dateRange && deadlineDateCheckBox.isChecked());
        deadlineTimeButton.setEnabled(dateRange && deadlineDateCheckBox.isChecked() && deadlineTimeCheckBox.isChecked());
        deadlineRemindersCheckBox.setEnabled(dateRange && deadlineDateCheckBox.isChecked());
        deadlineRemindersButton.setEnabled(dateRange && deadlineDateCheckBox.isChecked() && deadlineRemindersCheckBox.isChecked());
    }

    public void onWhenDateButtonClicked(View view) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment(selectedWhen);

        fragment.setOnDateSetListener(EditTaskActivity.this.onWhenDateSetListener);
        fragment.show(getFragmentManager(), "when_date_picker");
    }

    public void onWhenTimeCheckBoxClicked(View view) {
        whenTimeButton.setEnabled(whenTimeCheckBox.isChecked());
    }

    public void onWhenTimeButtonClicked(View view) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment(selectedWhen);

        fragment.setOnTimeSetListener(EditTaskActivity.this.onWhenTimeSetListener);
        fragment.show(getFragmentManager(), "when_time_picker");
    }

    public void onWhenRemindersCheckBoxClicked(View view) {
        whenRemindersButton.setEnabled(whenRemindersCheckBox.isChecked());
    }

    public void onWhenRemindersButtonClicked(View view) {
        // ToDo
    }

    public void onStartDateCheckBoxClicked(View view) {
        boolean date = startDateCheckBox.isChecked();
        boolean time = startTimeCheckBox.isChecked();

        startDateButton.setEnabled(date);
        startTimeCheckBox.setEnabled(date);
        startTimeButton.setEnabled(date && time);
        startRemindersCheckBox.setEnabled(date);
    }

    public void onStartDateButtonClicked(View view) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment(selectedStart);

        fragment.setOnDateSetListener(EditTaskActivity.this.onStartDateSetListener);
        fragment.show(getFragmentManager(), "start_date_picker");
    }

    public void onStartTimeCheckBoxClicked(View view) {
        startTimeButton.setEnabled(startTimeCheckBox.isChecked());
    }

    public void onStartTimeButtonClicked(View view) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment(selectedStart);

        fragment.setOnTimeSetListener(EditTaskActivity.this.onStartTimeSetListener);
        fragment.show(getFragmentManager(), "start_time_picker");
    }

    public void onStartRemindersCheckBoxClicked(View view) {
        startRemindersButton.setEnabled(startRemindersCheckBox.isChecked());
    }

    public void onStartRemindersButtonClicked(View view) {
        // ToDo
    }

    public void onDeadlineDateCheckBoxClicked(View view) {
        boolean date = deadlineDateCheckBox.isChecked();
        boolean time = deadlineTimeCheckBox.isChecked();

        deadlineDateButton.setEnabled(date);
        deadlineTimeCheckBox.setEnabled(date);
        deadlineTimeButton.setEnabled(date && time);
        deadlineRemindersCheckBox.setEnabled(date);
    }

    public void onDeadlineDateButtonClicked(View view) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment(selectedDeadline);

        fragment.setOnDateSetListener(EditTaskActivity.this.onDeadlineDateSetListener);
        fragment.show(getFragmentManager(), "deadline_date_picker");
    }

    public void onDeadlineTimeCheckBoxClicked(View view) {
        deadlineTimeButton.setEnabled(deadlineTimeCheckBox.isChecked());
    }

    public void onDeadlineTimeButtonClicked(View view) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment(selectedDeadline);

        fragment.setOnTimeSetListener(EditTaskActivity.this.onDeadlineTimeSetListener);
        fragment.show(getFragmentManager(), "deadline_time_picker");
    }

    public void onDeadlineRemindersCheckboxClicked(View view) {
        deadlineRemindersButton.setEnabled(deadlineRemindersCheckBox.isChecked());
    }

    public void onDeadlineRemindersButtonClicked(View view) {
        // ToDo
    }

    public void onAddTagButtonClicked(View view) {
        String name = ((addTagAutoTextView.getText()).toString()).trim();
        TaskTag tag = new TaskTag(currentTask.getContextId(), name, null);
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
