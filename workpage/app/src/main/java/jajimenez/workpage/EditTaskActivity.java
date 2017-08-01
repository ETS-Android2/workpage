package jajimenez.workpage;

import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText descriptionEditText;

    private Button dateMode;

    private TableRow rowDateTitle1;
    private TableRow rowDateTime1;
    private Button date1;
    private Button time1;
    private TableRow rowTimeZone1;

    private TableRow rowDateTitle2;
    private TableRow rowDateTime2;
    private Button date2;
    private Button time2;
    private TableRow rowTimeZone2;

    private AutoCompleteTextView addTagAutoTextView;
    private Button addTagButton;
    private LinearLayout addedTagsLinearLayout;

    private EditDescriptionDialogFragment.OnOkButtonClickedListener descriptionListener;

    private DateModeDialogFragment.OnDateModeSetListener dateModeListener;

    private DatePickerDialogFragment.OnDateSetListener date1Listener;
    private DatePickerDialogFragment.OnNoDateSetListener noDate1Listener;
    private TimePickerDialogFragment.OnTimeSetListener time1Listener;
    private TimePickerDialogFragment.OnNoTimeSetListener noTime1Listener;

    private DatePickerDialogFragment.OnDateSetListener date2Listener;
    private DatePickerDialogFragment.OnNoDateSetListener noDate2Listener;
    private TimePickerDialogFragment.OnTimeSetListener time2Listener;
    private TimePickerDialogFragment.OnNoTimeSetListener noTime2Listener;

    private ApplicationLogic applicationLogic = null;
    private Task currentTask = null;
    private List<TaskTag> contextTags = null;

    private int selectedDateMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_task);

        ActionBar bar = getSupportActionBar();

        try {
            bar.setHomeAsUpIndicator(R.drawable.cancel_2);
            bar.setDisplayHomeAsUpEnabled(true);
        }
        catch (NullPointerException e) {
            // Do nothing
        }

        titleEditText = (EditText) findViewById(R.id.edit_task_title);
        descriptionEditText = (EditText) findViewById(R.id.edit_task_description);

        dateMode = (Button) findViewById(R.id.edit_task_date_mode);

        rowDateTitle1 = (TableRow) findViewById(R.id.edit_task_row_date_title_1);
        rowDateTime1 = (TableRow) findViewById(R.id.edit_task_row_date_time_1);
        date1 = (Button) findViewById(R.id.edit_task_date_1);
        time1 = (Button) findViewById(R.id.edit_task_time_1);
        rowTimeZone1 = (TableRow) findViewById(R.id.edit_task_row_time_zone_1);

        rowDateTitle2 = (TableRow) findViewById(R.id.edit_task_row_date_title_2);
        rowDateTime2 = (TableRow) findViewById(R.id.edit_task_row_date_time_2);
        date2 = (Button) findViewById(R.id.edit_task_date_2);
        time2 = (Button) findViewById(R.id.edit_task_time_2);
        rowTimeZone2 = (TableRow) findViewById(R.id.edit_task_row_time_zone_2);

        addTagAutoTextView = (AutoCompleteTextView) findViewById(R.id.edit_task_add_tag_autotextview);
        addTagButton = (Button) findViewById(R.id.edit_task_add_tag_button);
        addedTagsLinearLayout = (LinearLayout) findViewById(R.id.edit_task_added_tags);

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

        descriptionListener = new EditDescriptionDialogFragment.OnOkButtonClickedListener() {
            public void onOkButtonClicked(String description) {
                EditTaskActivity.this.descriptionEditText.setText(description);
            }
        };

        // Date Mode
        dateModeListener = new DateModeDialogFragment.OnDateModeSetListener() {
            public void onDateModeSet(int mode) {
                EditTaskActivity.this.selectedDateMode = mode;
                //DateTimeTool tool = new DateTimeTool();

                switch (mode) {
                    case ApplicationLogic.SINGLE_DATE:
                        // Today
                        Calendar when = Calendar.getInstance();
                        when.add(Calendar.HOUR_OF_DAY, 1);
                        when.clear(Calendar.MINUTE);
                        //tool.clearTimeFields(when);

                        currentTask.setWhen(when);

                        break;
                    case ApplicationLogic.DATE_RANGE:
                        // Today
                        Calendar start = Calendar.getInstance();
                        //tool.clearTimeFields(start);
                        start.add(Calendar.HOUR_OF_DAY, 1);
                        start.clear(Calendar.MINUTE);

                        // Tomorrow
                        Calendar deadline = Calendar.getInstance();
                        deadline.setTimeInMillis(start.getTimeInMillis());
                        //tool.clearTimeFields(deadline);
                        deadline.add(Calendar.DAY_OF_MONTH, 1);

                        currentTask.setStart(start);
                        currentTask.setDeadline(deadline);

                        break;
                }

                EditTaskActivity.this.updateInterface();
            }
        };

        // Date and Time 1
        date1Listener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                Calendar date;
                DateTimeTool tool = new DateTimeTool();

                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    date = EditTaskActivity.this.currentTask.getWhen();
                }
                else {
                    date = EditTaskActivity.this.currentTask.getStart();
                }

                if (date == null) {
                    date = Calendar.getInstance();
                    tool.clearTimeFields(date);

                    if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                        EditTaskActivity.this.currentTask.setWhen(date);
                    }
                    else {
                        EditTaskActivity.this.currentTask.setStart(date);
                    }
                }

                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                date.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        noDate1Listener = new DatePickerDialogFragment.OnNoDateSetListener() {
            public void onNoDateSet() {
                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    EditTaskActivity.this.currentTask.setWhen(null);
                    EditTaskActivity.this.currentTask.setIgnoreWhenTime(true);
                }
                else {
                    EditTaskActivity.this.currentTask.setStart(null);
                    EditTaskActivity.this.currentTask.setIgnoreStartTime(true);
                }

                EditTaskActivity.this.updateInterface();
            }
        };

        time1Listener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                Calendar date;
                DateTimeTool tool = new DateTimeTool();

                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    date = EditTaskActivity.this.currentTask.getWhen();
                    EditTaskActivity.this.currentTask.setIgnoreWhenTime(false);
                }
                else {
                    date = EditTaskActivity.this.currentTask.getStart();
                    EditTaskActivity.this.currentTask.setIgnoreStartTime(false);
                }

                if (date == null) date = Calendar.getInstance();

                tool.clearTimeFields(date);
                date.set(Calendar.HOUR_OF_DAY, hour);
                date.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.updateInterface();
            }
        };

        noTime1Listener = new TimePickerDialogFragment.OnNoTimeSetListener() {
            public void onNoTimeSet() {
                Calendar date;
                DateTimeTool tool = new DateTimeTool();

                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    date = EditTaskActivity.this.currentTask.getWhen();
                    EditTaskActivity.this.currentTask.setIgnoreWhenTime(true);
                }
                else {
                    date = EditTaskActivity.this.currentTask.getStart();
                    EditTaskActivity.this.currentTask.setIgnoreStartTime(true);
                }

                if (date == null) date = Calendar.getInstance();
                tool.clearTimeFields(date);

                EditTaskActivity.this.updateInterface();
            }
        };

        // Date and Time 2
        date2Listener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                Calendar deadline = EditTaskActivity.this.currentTask.getDeadline();
                DateTimeTool tool = new DateTimeTool();

                if (deadline == null) {
                    deadline = Calendar.getInstance();
                    tool.clearTimeFields(deadline);

                    EditTaskActivity.this.currentTask.setDeadline(deadline);
                }

                deadline.set(Calendar.YEAR, year);
                deadline.set(Calendar.MONTH, month);
                deadline.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        noDate2Listener = new DatePickerDialogFragment.OnNoDateSetListener() {
            public void onNoDateSet() {
                EditTaskActivity.this.currentTask.setDeadline(null);
                EditTaskActivity.this.currentTask.setIgnoreDeadlineTime(true);

                EditTaskActivity.this.updateInterface();
            }
        };

        time2Listener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                DateTimeTool tool = new DateTimeTool();
                Calendar deadline = EditTaskActivity.this.currentTask.getDeadline();

                if (deadline == null) deadline = Calendar.getInstance();
                tool.clearTimeFields(deadline);

                deadline.set(Calendar.HOUR_OF_DAY, hour);
                deadline.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.currentTask.setIgnoreDeadlineTime(false);

                EditTaskActivity.this.updateInterface();
            }
        };

        noTime2Listener = new TimePickerDialogFragment.OnNoTimeSetListener() {
            public void onNoTimeSet() {
                DateTimeTool tool = new DateTimeTool();
                Calendar deadline = EditTaskActivity.this.currentTask.getDeadline();

                if (deadline == null) deadline = Calendar.getInstance();
                tool.clearTimeFields(deadline);

                EditTaskActivity.this.currentTask.setIgnoreDeadlineTime(true);

                EditTaskActivity.this.updateInterface();
            }
        };

        addTagButton.setEnabled(false);

        applicationLogic = new ApplicationLogic(this);
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        //int selectedDateOption = 0;
        //selectedDateMode = ApplicationLogic.NO_DATE;
        /*boolean whenTime = false;
        boolean whenReminder = false;
        boolean startDate = false;
        boolean startTime = false;
        boolean startReminder = false;
        boolean deadlineDate = false;
        boolean deadlineTime = false;
        boolean deadlineReminder = false;*/

        if (mode != null && mode.equals("edit")) {
            // Get task data.
            long taskId = intent.getLongExtra("task_id", 0);
            currentTask = applicationLogic.getTask(taskId);
        }
        else {
            long contextId = intent.getLongExtra("task_context_id", 0);

            currentTask = new Task();
            currentTask.setContextId(contextId);
        }

        if (savedInstanceState == null) {
            /*if (mode != null && mode.equals("edit")) {
                titleEditText.setText(currentTask.getTitle());
                descriptionEditText.setText(currentTask.getDescription());
            }*/

            DateTimeTool tool = new DateTimeTool();
            TaskReminder defaultReminder = applicationLogic.getTaskReminder(4); // 15 minutes.

            /*selectedWhen = Calendar.getInstance();
            tool.clearTimeFields(selectedWhen);
            selectedWhenReminder = defaultReminder;

            selectedStart = Calendar.getInstance();
            tool.clearTimeFields(selectedStart);
            selectedStartReminder = defaultReminder;

            selectedDeadline = Calendar.getInstance();
            tool.clearTimeFields(selectedDeadline);
            selectedDeadlineReminder = defaultReminder;*/

            Calendar when = currentTask.getWhen();
            //TaskReminder whenRem = currentTask.getWhenReminder();

            Calendar start = currentTask.getStart();
            //TaskReminder startRem = currentTask.getStartReminder();

            Calendar deadline = currentTask.getDeadline();
            //TaskReminder deadlineRem = currentTask.getDeadlineReminder();

            if (when == null && start == null && deadline == null) {
                //selectedDateOption = NO_DATE;
                selectedDateMode = ApplicationLogic.NO_DATE;
            }
            else if (when == null) {
                //selectedDateOption = DATE_RANGE;
                selectedDateMode = ApplicationLogic.DATE_RANGE;

                /*if (start != null) {
                    selectedStart = start;
                    startDate = true;
                    startTime = !currentTask.getIgnoreStartTime();

                    if (startRem != null) {
                        startReminder = true;
                        selectedStartReminder = startRem;
                    }
                }

                if (deadline != null) {
                    selectedDeadline = deadline;
                    deadlineDate = true;
                    deadlineTime = !currentTask.getIgnoreDeadlineTime();

                    if (deadlineRem != null) {
                        deadlineReminder = true;
                        selectedDeadlineReminder = deadlineRem;
                    }
                }*/
            }
            else {
                //selectedDateOption = WHEN;
                selectedDateMode = ApplicationLogic.SINGLE_DATE;
                /*selectedWhen = when;
                whenTime = !currentTask.getIgnoreWhenTime();

                if (whenRem != null) {
                    whenReminder = true;
                    selectedWhenReminder = whenRem;
                }*/
            }
        }
        else {
            // Dialog listeners
            EditDescriptionDialogFragment descriptionFragment = (EditDescriptionDialogFragment) (getFragmentManager()).findFragmentByTag("edit_description");
            if (descriptionFragment != null) descriptionFragment.setOnOkButtonClickedListener(descriptionListener);

            DateModeDialogFragment modeFragment = (DateModeDialogFragment) (getFragmentManager()).findFragmentByTag("date_mode");
            if (modeFragment != null) modeFragment.setOnDateModeSetListener(dateModeListener);

            DatePickerDialogFragment date1Fragment = (DatePickerDialogFragment) (getFragmentManager()).findFragmentByTag("date_picker_1");
            if (date1Fragment != null) {
                date1Fragment.setOnDateSetListener(date1Listener);
                date1Fragment.setOnNoDateSetListener(noDate1Listener);
            }

            TimePickerDialogFragment time1Fragment = (TimePickerDialogFragment) (getFragmentManager()).findFragmentByTag("time_picker_1");
            if (time1Fragment != null) {
                time1Fragment.setOnTimeSetListener(time1Listener);
                time1Fragment.setOnNoTimeSetListener(noTime1Listener);
            }

            DatePickerDialogFragment date2Fragment = (DatePickerDialogFragment) (getFragmentManager()).findFragmentByTag("date_picker_2");
            if (date2Fragment != null) {
                date2Fragment.setOnDateSetListener(date2Listener);
                date2Fragment.setOnNoDateSetListener(noDate2Listener);
            }

            TimePickerDialogFragment time2Fragment = (TimePickerDialogFragment) (getFragmentManager()).findFragmentByTag("time_picker_2");
            if (time2Fragment != null) {
                time2Fragment.setOnTimeSetListener(time2Listener);
                time2Fragment.setOnNoTimeSetListener(noTime2Listener);
            }

            // Date mode
            selectedDateMode = savedInstanceState.getInt("selected_date_mode");

            // Dates and time
            long when = savedInstanceState.getLong("when", -1);
            long start = savedInstanceState.getLong("start", -1);
            long deadline = savedInstanceState.getLong("deadline", -1);

            if (when >= 0) {
                boolean ignoreWhenTime = savedInstanceState.getBoolean("ignore_when_time", false);

                Calendar calWhen = Calendar.getInstance();
                calWhen.setTimeInMillis(when);

                currentTask.setWhen(calWhen);
                currentTask.setIgnoreWhenTime(ignoreWhenTime);
            }

            if (start >= 0) {
                boolean ignoreStartTime = savedInstanceState.getBoolean("ignore_start_time", false);

                Calendar calStart = Calendar.getInstance();
                calStart.setTimeInMillis(start);

                currentTask.setStart(calStart);
                currentTask.setIgnoreWhenTime(ignoreStartTime);
            }

            if (deadline >= 0) {
                boolean ignoreDeadlineTime = savedInstanceState.getBoolean("ignore_deadline_time", false);

                Calendar calDeadline = Calendar.getInstance();
                calDeadline.setTimeInMillis(deadline);

                currentTask.setDeadline(calDeadline);
                currentTask.setIgnoreDeadlineTime(ignoreDeadlineTime);
            }

            /*whenReminder = savedInstanceState.getBoolean("when_reminder");
            startDate = savedInstanceState.getBoolean("start_date");
            startTime = savedInstanceState.getBoolean("start_time");
            startReminder = savedInstanceState.getBoolean("start_reminder");
            deadlineDate = savedInstanceState.getBoolean("deadline_date");
            deadlineTime = savedInstanceState.getBoolean("deadline_time");
            deadlineReminder = savedInstanceState.getBoolean("deadline_reminder");*/

            //long whenTime = savedInstanceState.getLong("when",-1);

            /*selectedWhen = getSavedCalendar(savedInstanceState, "selected_when");
            selectedWhenReminder = applicationLogic.getTaskReminder(savedInstanceState.getLong("selected_when_reminder_id"));

            selectedStart = getSavedCalendar(savedInstanceState, "selected_start");
            selectedStartReminder = applicationLogic.getTaskReminder(savedInstanceState.getLong("selected_start_reminder_id"));

            selectedDeadline = getSavedCalendar(savedInstanceState, "selected_deadline");
            selectedDeadlineReminder = applicationLogic.getTaskReminder(savedInstanceState.getLong("selected_deadline_reminder_id"));*/
        }

        /*switch(selectedDateMode) {
            case WHEN:
                whenRadioButton.setChecked(true);
                break;
            case DATE_RANGE:
                dateRangeRadioButton.setChecked(true);
                break;
            default:
                noDateRadioButton.setChecked(true);
                break;
        }*/

        TaskContext context = applicationLogic.getTaskContext(currentTask.getContextId());
        contextTags = applicationLogic.getAllTaskTags(context);

        if (savedInstanceState != null) currentTask.setTags(getSavedAddedTaskTags(savedInstanceState));

        setTagCompletionSuggestions();
        addInitialTaskTagViews();

        updateInterface();
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

        outState.putInt("selected_date_mode", selectedDateMode);

        outState.putString("title", currentTask.getTitle());
        outState.putString("description", currentTask.getDescription());

        // Selected dates and reminders.
        Calendar when = currentTask.getWhen();
        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

        if (when != null) {
            outState.putLong("when", when.getTimeInMillis());
            outState.putBoolean("ignore_when_time", currentTask.getIgnoreWhenTime());
        }
        else {
            outState.putLong("when", -1);
            outState.putBoolean("ignore_when_time", false);
        }

        if (start != null) {
            outState.putLong("start", start.getTimeInMillis());
            outState.putBoolean("ignore_start_time", currentTask.getIgnoreStartTime());
        }
        else {
            outState.putLong("start", -1);
            outState.putBoolean("ignore_start_time", false);
        }

        if (deadline != null) {
            outState.putLong("deadline", deadline.getTimeInMillis());
            outState.putBoolean("ignore_deadline_time", currentTask.getIgnoreDeadlineTime());
        }
        else {
            outState.putLong("deadline", -1);
            outState.putBoolean("ignore_deadline_time", false);
        }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean eventHandled = false;
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();

                eventHandled = true;
                break;
        }

        return eventHandled;
    }

    private void updateInterface() {
        if (currentTask.getId() < 0) setTitle(R.string.new_task);
        else setTitle(R.string.edit_task);

        titleEditText.setText(currentTask.getTitle());
        descriptionEditText.setText(currentTask.getDescription());

        TextTool tool = new TextTool();

        if (selectedDateMode == ApplicationLogic.NO_DATE) {
            dateMode.setText(R.string.no_date);

            rowDateTitle1.setVisibility(View.GONE);
            rowDateTime1.setVisibility(View.GONE);
            rowTimeZone1.setVisibility(View.GONE);

            rowDateTitle2.setVisibility(View.GONE);
            rowDateTime2.setVisibility(View.GONE);
            rowTimeZone2.setVisibility(View.GONE);
        }
        else if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
            Calendar when = currentTask.getWhen();

            dateMode.setText(R.string.single_date);

            rowDateTitle1.setVisibility(View.GONE);
            rowDateTime1.setVisibility(View.VISIBLE);
            rowTimeZone1.setVisibility(View.VISIBLE);

            rowDateTitle2.setVisibility(View.GONE);
            rowDateTime2.setVisibility(View.GONE);
            rowTimeZone2.setVisibility(View.GONE);

            date1.setText(tool.getFormattedDate(when));

            if (currentTask.getIgnoreWhenTime()) time1.setText(R.string.no_time);
            else time1.setText(tool.getFormattedTime(when));
        }
        else {
            Calendar start = currentTask.getStart();
            Calendar deadline = currentTask.getDeadline();

            dateMode.setText(R.string.date_range);

            rowDateTitle1.setVisibility(View.VISIBLE);
            rowDateTime1.setVisibility(View.VISIBLE);
            rowTimeZone1.setVisibility(View.VISIBLE);

            rowDateTitle2.setVisibility(View.VISIBLE);
            rowDateTime2.setVisibility(View.VISIBLE);
            rowTimeZone2.setVisibility(View.VISIBLE);

            if (start == null) {
                date1.setText(R.string.no_date);
                time1.setText(R.string.no_time);
                time1.setEnabled(false);
            }
            else {
                date1.setText(tool.getFormattedDate(start));

                if (currentTask.getIgnoreStartTime()) time1.setText(R.string.no_time);
                else time1.setText(tool.getFormattedTime(start));

                time1.setEnabled(true);
            }

            if (deadline == null) {
                date2.setText(R.string.no_date);
                time2.setText(R.string.no_time);
                time2.setEnabled(false);
            }
            else {
                date2.setText(tool.getFormattedDate(deadline));

                if (currentTask.getIgnoreDeadlineTime()) time2.setText(R.string.no_time);
                else time2.setText(tool.getFormattedTime(deadline));

                time2.setEnabled(true);
            }
        }

        /*whenDateButton.setText(tool.getFormattedDate(selectedWhen));
        whenTimeButton.setText(tool.getFormattedTime(selectedWhen));
        whenReminderButton.setText(tool.getTaskReminderText(this, selectedWhenReminder));

        startDateButton.setText(tool.getFormattedDate(selectedStart));
        startTimeButton.setText(tool.getFormattedTime(selectedStart));
        startReminderButton.setText(tool.getTaskReminderText(this, selectedStartReminder));

        deadlineDateButton.setText(tool.getFormattedDate(selectedDeadline));
        deadlineTimeButton.setText(tool.getFormattedTime(selectedDeadline));
        deadlineReminderButton.setText(tool.getTaskReminderText(this, selectedDeadlineReminder));*/
    }

    public void onSaveItemSelected(MenuItem item) {
        //Calendar when = currentTask.getWhen();
        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

        //String title = (titleEditText.getText()).toString();
        String title = currentTask.getTitle();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid;

        if (selectedDateMode == ApplicationLogic.DATE_RANGE) {
            datesValid =
                    start == null ||
                    deadline == null ||
                    compareCalendars(deadline, currentTask.getIgnoreDeadlineTime(), start, currentTask.getIgnoreStartTime()) >= 0;
        }
        else {
            datesValid = true;
        }

        if (titleValid && datesValid) {
            // Update Current Task
            //currentTask.setTitle(title);
            //currentTask.setDescription((descriptionEditText.getText()).toString());

            /*Calendar when = null;
            boolean ignoreWhenTime = false;
            TaskReminder whenReminder = null;

            Calendar start = null;
            boolean ignoreStartTime = false;
            TaskReminder startReminder = null;

            Calendar deadline = null;
            boolean ignoreDeadlineTime = false;
            TaskReminder deadlineReminder = null;

            DateTimeTool tool = new DateTimeTool();*/

            /*if (whenRadioButton.isChecked()) {
                when = Calendar.getInstance();
                when.setTimeInMillis(selectedWhen.getTimeInMillis());

                ignoreWhenTime = !whenTimeCheckBox.isChecked();
                if (ignoreWhenTime) tool.clearTimeFields(when);

                if (whenReminderCheckBox.isChecked()) whenReminder = selectedWhenReminder;
            }
            else if (dateRangeRadioButton.isChecked()) {
                if (startDateCheckBox.isChecked()) {
                    start = Calendar.getInstance();
                    start.setTimeInMillis(selectedStart.getTimeInMillis());

                    ignoreStartTime = !startTimeCheckBox.isChecked();
                    if (ignoreStartTime) tool.clearTimeFields(start);

                    if (startReminderCheckBox.isChecked()) startReminder = selectedStartReminder;
                }

                if (deadlineDateCheckBox.isChecked()) {
                    deadline = Calendar.getInstance();
                    deadline.setTimeInMillis(selectedDeadline.getTimeInMillis());

                    ignoreDeadlineTime = !deadlineTimeCheckBox.isChecked();
                    if (ignoreDeadlineTime) tool.clearTimeFields(deadline);

                    if (deadlineReminderCheckBox.isChecked()) deadlineReminder = selectedDeadlineReminder;
                }
            }*/

            /*currentTask.setWhen(when);
            currentTask.setIgnoreWhenTime(ignoreWhenTime);
            currentTask.setWhenReminder(whenReminder);

            currentTask.setStart(start);
            currentTask.setIgnoreStartTime(ignoreStartTime);
            currentTask.setStartReminder(startReminder);

            currentTask.setDeadline(deadline);
            currentTask.setIgnoreDeadlineTime(ignoreDeadlineTime);
            currentTask.setDeadlineReminder(deadlineReminder);*/

            // Save Current Task
            applicationLogic.saveTask(currentTask);
            (Toast.makeText(this, R.string.task_saved, Toast.LENGTH_SHORT)).show();
            setResult(RESULT_OK);

            // Close the activity
            finish();
        }
        else if (!titleValid) {
            (Toast.makeText(this, R.string.title_error, Toast.LENGTH_SHORT)).show();
        }
        else {
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

    public void onEditDescriptionClicked(View view) {
        EditDescriptionDialogFragment fragment = new EditDescriptionDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putString("description", (descriptionEditText.getText()).toString());
        fragment.setArguments(arguments);

        fragment.setOnOkButtonClickedListener(descriptionListener);
        fragment.show(getFragmentManager(), "edit_description");
    }

    public void onDateModeClicked(View view) {
        DateModeDialogFragment fragment = new DateModeDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putInt("mode", ApplicationLogic.NO_DATE);
        fragment.setArguments(arguments);

        fragment.setOnDateModeSetListener(dateModeListener);
        fragment.show(getFragmentManager(), "date_mode");
    }

    public void onDate1Clicked(View view) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();

        Bundle arguments = new Bundle();
        Calendar date;

        if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
            date = currentTask.getWhen();
            arguments.putBoolean("include_no_date_button", false);
        }
        else {
            date = currentTask.getStart();
            arguments.putBoolean("include_no_date_button", true);
        }

        if (date == null) {
            date = Calendar.getInstance();
            date.add(Calendar.HOUR_OF_DAY, 1);
            date.clear(Calendar.MINUTE);
        }

        arguments.putInt("year", date.get(Calendar.YEAR));
        arguments.putInt("month", date.get(Calendar.MONTH));
        arguments.putInt("day", date.get(Calendar.DAY_OF_MONTH));

        fragment.setArguments(arguments);
        fragment.setOnDateSetListener(date1Listener);
        fragment.setOnNoDateSetListener(noDate1Listener);
        fragment.show(getFragmentManager(), "date_picker_1");
    }

    public void onTime1Clicked(View view) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();

        Bundle arguments = new Bundle();
        Calendar time;

        if (selectedDateMode == ApplicationLogic.SINGLE_DATE) time = currentTask.getWhen();
        else time = currentTask.getStart();

        if (time == null) {
            time = Calendar.getInstance();
            time.add(Calendar.HOUR_OF_DAY, 1);
            time.clear(Calendar.MINUTE);
        }

        arguments.putInt("hour", time.get(Calendar.HOUR_OF_DAY));
        arguments.putInt("minute", time.get(Calendar.MINUTE));
        arguments.putBoolean("include_no_time_button", true);

        fragment.setArguments(arguments);
        fragment.setOnTimeSetListener(time1Listener);
        fragment.setOnNoTimeSetListener(noTime1Listener);
        fragment.show(getFragmentManager(), "time_picker_1");
    }

    public void onDate2Clicked(View view) {
        DatePickerDialogFragment fragment = new DatePickerDialogFragment();

        Bundle arguments = new Bundle();
        Calendar date = currentTask.getDeadline();

        if (date == null) {
            date = Calendar.getInstance();
            date.add(Calendar.HOUR_OF_DAY, 1);
            date.clear(Calendar.MINUTE);
        }

        arguments.putInt("year", date.get(Calendar.YEAR));
        arguments.putInt("month", date.get(Calendar.MONTH));
        arguments.putInt("day", date.get(Calendar.DAY_OF_MONTH));
        arguments.putBoolean("include_no_date_button", true);

        fragment.setArguments(arguments);
        fragment.setOnDateSetListener(date2Listener);
        fragment.setOnNoDateSetListener(noDate2Listener);
        fragment.show(getFragmentManager(), "date_picker_2");
    }

    public void onTime2Clicked(View view) {
        TimePickerDialogFragment fragment = new TimePickerDialogFragment();

        Bundle arguments = new Bundle();
        Calendar time = currentTask.getDeadline();

        if (time == null) {
            time = Calendar.getInstance();
            time.add(Calendar.HOUR_OF_DAY, 1);
            time.clear(Calendar.MINUTE);
        }

        arguments.putInt("hour", time.get(Calendar.HOUR_OF_DAY));
        arguments.putInt("minute", time.get(Calendar.MINUTE));
        arguments.putBoolean("include_no_time_button", true);

        fragment.setArguments(arguments);
        fragment.setOnTimeSetListener(time2Listener);
        fragment.setOnNoTimeSetListener(noTime2Listener);
        fragment.show(getFragmentManager(), "time_picker_2");
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