package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;
import java.util.TimeZone;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends AppCompatActivity {
    private EditText title;
    private Button description;

    private Button dateMode;

    private TextView dateMode1;
    private TableLayout tableDate1;
    private Button date1;
    private Button time1;
    private Button timeZone1;
    private Button reminder1;
    private View divider1;

    private TextView dateMode2;
    private TableLayout tableDate2;
    private Button date2;
    private Button time2;
    private Button timeZone2;
    private Button reminder2;
    private View divider2;

    private AutoCompleteTextView addTagAutoTextView;
    private Button addTagButton;
    private LinearLayout addedTagsLinearLayout;

    private EditDescriptionDialogFragment.OnOkButtonClickedListener descriptionListener;
    private DateModeDialogFragment.OnDateModeSetListener dateModeListener;
    private TimeZonePickerDialogFragment.OnTimeZoneSelectedListener timeZoneSelectedListener1;
    private TimeZonePickerDialogFragment.OnTimeZoneSelectedListener timeZoneSelectedListener2;

    private DatePickerDialogFragment.OnDateSetListener date1Listener;
    private DatePickerDialogFragment.OnNoDateSetListener noDate1Listener;
    private TimePickerDialogFragment.OnTimeSetListener time1Listener;
    private TimePickerDialogFragment.OnNoTimeSetListener noTime1Listener;
    private TaskReminderPickerDialogFragment.OnTaskReminderSetListener reminder1Listener;
    private TaskReminderPickerDialogFragment.OnTaskReminderSetListener reminder2Listener;

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

        title = (EditText) findViewById(R.id.edit_task_title);
        description = (Button) findViewById(R.id.edit_task_description);

        dateMode = (Button) findViewById(R.id.edit_task_date_mode);

        dateMode1 = (TextView) findViewById(R.id.edit_task_date_mode_1);
        tableDate1 = (TableLayout) findViewById(R.id.edit_task_table_date_1);
        date1 = (Button) findViewById(R.id.edit_task_date_1);
        time1 = (Button) findViewById(R.id.edit_task_time_1);
        timeZone1 = (Button) findViewById(R.id.edit_task_time_zone_1);
        reminder1 = (Button) findViewById(R.id.edit_task_reminder_1);
        divider1 = findViewById(R.id.edit_task_date_divider_1);

        dateMode2 = (TextView) findViewById(R.id.edit_task_date_mode_2);
        tableDate2 = (TableLayout) findViewById(R.id.edit_task_table_date_2);
        date2 = (Button) findViewById(R.id.edit_task_date_2);
        time2 = (Button) findViewById(R.id.edit_task_time_2);
        timeZone2 = (Button) findViewById(R.id.edit_task_time_zone_2);
        reminder2 = (Button) findViewById(R.id.edit_task_reminder_2);
        divider2 = findViewById(R.id.edit_task_date_divider_2);

        addTagAutoTextView = (AutoCompleteTextView) findViewById(R.id.edit_task_add_tag_autotextview);
        addTagButton = (Button) findViewById(R.id.edit_task_add_tag_button);
        addedTagsLinearLayout = (LinearLayout) findViewById(R.id.edit_task_added_tags);

        title.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                EditTaskActivity.this.currentTask.setTitle(text);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        descriptionListener = new EditDescriptionDialogFragment.OnOkButtonClickedListener() {
            public void onOkButtonClicked(String description) {
                EditTaskActivity.this.currentTask.setDescription(description);
                EditTaskActivity.this.updateInterface();
            }
        };

        // Date Mode
        dateModeListener = new DateModeDialogFragment.OnDateModeSetListener() {
            public void onDateModeSet(int mode) {
                // If the selected mode is the same than the current one, we exit.
                if (mode == EditTaskActivity.this.selectedDateMode) return;

                EditTaskActivity.this.selectedDateMode = mode;

                switch (mode) {
                    case ApplicationLogic.SINGLE_DATE:
                        // Today
                        Calendar when = Calendar.getInstance();
                        when.add(Calendar.HOUR_OF_DAY, 1);
                        when.clear(Calendar.MINUTE);

                        currentTask.setWhen(when);
                        currentTask.setStart(null);
                        currentTask.setDeadline(null);

                        break;
                    case ApplicationLogic.DATE_RANGE:
                        // Today
                        Calendar start = Calendar.getInstance();
                        start.add(Calendar.HOUR_OF_DAY, 1);
                        start.clear(Calendar.MINUTE);

                        // Tomorrow
                        Calendar deadline = Calendar.getInstance();
                        deadline.setTimeInMillis(start.getTimeInMillis());
                        deadline.add(Calendar.DAY_OF_MONTH, 1);

                        currentTask.setWhen(null);
                        currentTask.setStart(start);
                        currentTask.setDeadline(deadline);

                        break;
                }

                currentTask.setIgnoreWhenTime(false);
                currentTask.setIgnoreStartTime(false);
                currentTask.setIgnoreDeadlineTime(false);

                currentTask.setWhenReminder(null);
                currentTask.setStartReminder(null);
                currentTask.setDeadlineReminder(null);

                EditTaskActivity.this.updateInterface();
            }
        };

        timeZoneSelectedListener1 = new TimeZonePickerDialogFragment.OnTimeZoneSelectedListener() {
            @Override
            public void onTimeZoneSelected(TimeZone t) {
                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) (currentTask.getWhen()).setTimeZone(t);
                else (currentTask.getStart()).setTimeZone(t);

                EditTaskActivity.this.updateInterface();
            }
        };

        timeZoneSelectedListener2 = new TimeZonePickerDialogFragment.OnTimeZoneSelectedListener() {
            @Override
            public void onTimeZoneSelected(TimeZone t) {
                (currentTask.getDeadline()).setTimeZone(t);
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

        reminder1Listener = new TaskReminderPickerDialogFragment.OnTaskReminderSetListener() {
            public void onTaskReminderSet(TaskReminder reminder) {
                if (EditTaskActivity.this.selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    EditTaskActivity.this.currentTask.setWhenReminder(reminder);
                }
                else if (EditTaskActivity.this.selectedDateMode == ApplicationLogic.DATE_RANGE) {
                    EditTaskActivity.this.currentTask.setStartReminder(reminder);
                }

                EditTaskActivity.this.updateInterface();
            }
        };

        reminder2Listener = new TaskReminderPickerDialogFragment.OnTaskReminderSetListener() {
            public void onTaskReminderSet(TaskReminder reminder) {
                EditTaskActivity.this.currentTask.setDeadlineReminder(reminder);
                EditTaskActivity.this.updateInterface();
            }
        };

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
        String mode = intent.getStringExtra("mode");

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
            //TaskReminder defaultReminder = applicationLogic.getTaskReminder(4); // 15 minutes.

            Calendar when = currentTask.getWhen();
            //TaskReminder whenRem = currentTask.getWhenReminder();

            Calendar start = currentTask.getStart();
            //TaskReminder startRem = currentTask.getStartReminder();

            Calendar deadline = currentTask.getDeadline();
            //TaskReminder deadlineRem = currentTask.getDeadlineReminder();

            if (when == null && start == null && deadline == null) selectedDateMode = ApplicationLogic.NO_DATE;
            else if (when == null) selectedDateMode = ApplicationLogic.DATE_RANGE;
            else selectedDateMode = ApplicationLogic.SINGLE_DATE;
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

            TimeZonePickerDialogFragment timeZone1Fragment = (TimeZonePickerDialogFragment) (getFragmentManager()).findFragmentByTag("time_zone_picker_1");
            if (timeZone1Fragment != null) {
                timeZone1Fragment.setOnTimeZoneSelectedListener(timeZoneSelectedListener1);
            }

            TimeZonePickerDialogFragment timeZone2Fragment = (TimeZonePickerDialogFragment) (getFragmentManager()).findFragmentByTag("time_zone_picker_2");
            if (timeZone2Fragment != null) {
                timeZone2Fragment.setOnTimeZoneSelectedListener(timeZoneSelectedListener2);
            }

            TaskReminderPickerDialogFragment reminder1Fragment = (TaskReminderPickerDialogFragment) (getFragmentManager()).findFragmentByTag("reminder_picker_1");
            if (reminder1Fragment != null) {
                reminder1Fragment.setOnTaskReminderSetListener(reminder1Listener);
            }

            TaskReminderPickerDialogFragment reminder2Fragment = (TaskReminderPickerDialogFragment) (getFragmentManager()).findFragmentByTag("reminder_picker_2");
            if (reminder2Fragment != null) {
                reminder2Fragment.setOnTaskReminderSetListener(reminder2Listener);
            }

            String title = savedInstanceState.getString("title");
            String description = savedInstanceState.getString("description");

            currentTask.setTitle(title);
            currentTask.setDescription(description);

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
        }

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

        title.setText(currentTask.getTitle());

        String desc = currentTask.getDescription();

        if (desc != null && !desc.isEmpty()) description.setText(R.string.edit_description);
        else description.setText(R.string.add_description);

        TextTool tool = new TextTool();
        Calendar now;

        if (selectedDateMode == ApplicationLogic.NO_DATE) {
            dateMode.setText(R.string.no_date);

            dateMode1.setVisibility(View.GONE);
            tableDate1.setVisibility(View.GONE);
            divider1.setVisibility(View.GONE);

            dateMode2.setVisibility(View.GONE);
            tableDate2.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
        }
        else if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
            Calendar when = currentTask.getWhen();

            dateMode.setText(R.string.single_date);

            dateMode1.setVisibility(View.GONE);
            tableDate1.setVisibility(View.VISIBLE);
            divider1.setVisibility(View.VISIBLE);

            dateMode2.setVisibility(View.GONE);
            tableDate2.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);

            date1.setText(tool.getFormattedDate(when));

            if (currentTask.getIgnoreWhenTime()) time1.setText(R.string.no_time);
            else time1.setText(tool.getFormattedTime(when));

            TimeZone whenTimeZone = when.getTimeZone();
            now = Calendar.getInstance();
            boolean whenDaylight = whenTimeZone.inDaylightTime(now.getTime());
            timeZone1.setText((when.getTimeZone()).getDisplayName(whenDaylight, TimeZone.LONG));

            reminder1.setEnabled(true);
            reminder1.setText(getReminderButtonText(currentTask.getWhenReminder()));
        }
        else {
            Calendar start = currentTask.getStart();
            Calendar deadline = currentTask.getDeadline();

            dateMode.setText(R.string.date_range);

            dateMode1.setVisibility(View.VISIBLE);
            tableDate1.setVisibility(View.VISIBLE);
            divider1.setVisibility(View.VISIBLE);

            dateMode2.setVisibility(View.VISIBLE);
            tableDate2.setVisibility(View.VISIBLE);
            divider2.setVisibility(View.VISIBLE);

            if (start == null) {
                date1.setText(R.string.no_date);
                time1.setText(R.string.no_time);
                time1.setEnabled(false);
                timeZone1.setEnabled(false);

                TimeZone localTimeZone = TimeZone.getDefault();
                now = Calendar.getInstance();
                boolean localDaylight = localTimeZone.inDaylightTime(now.getTime());
                timeZone1.setText(localTimeZone.getDisplayName(localDaylight, TimeZone.LONG));

                reminder1.setEnabled(false);
                reminder1.setText(getReminderButtonText(null));
            }
            else {
                date1.setText(tool.getFormattedDate(start));

                if (currentTask.getIgnoreStartTime()) time1.setText(R.string.no_time);
                else time1.setText(tool.getFormattedTime(start));

                time1.setEnabled(true);
                timeZone1.setEnabled(true);

                TimeZone startTimeZone = start.getTimeZone();
                now = Calendar.getInstance();
                boolean startDaylight = startTimeZone.inDaylightTime(now.getTime());
                timeZone1.setText((start.getTimeZone()).getDisplayName(startDaylight, TimeZone.LONG));

                reminder1.setEnabled(true);
                reminder1.setText(getReminderButtonText(currentTask.getStartReminder()));
            }

            if (deadline == null) {
                date2.setText(R.string.no_date);
                time2.setText(R.string.no_time);
                time2.setEnabled(false);
                timeZone2.setEnabled(false);

                TimeZone localTimeZone = TimeZone.getDefault();
                now = Calendar.getInstance();
                boolean localDaylight = localTimeZone.inDaylightTime(now.getTime());
                timeZone2.setText(localTimeZone.getDisplayName(localDaylight, TimeZone.LONG));

                reminder2.setEnabled(false);
                reminder2.setText(getReminderButtonText(null));
            }
            else {
                date2.setText(tool.getFormattedDate(deadline));

                if (currentTask.getIgnoreDeadlineTime()) time2.setText(R.string.no_time);
                else time2.setText(tool.getFormattedTime(deadline));

                time2.setEnabled(true);
                timeZone2.setEnabled(true);

                TimeZone deadlineTimeZone = deadline.getTimeZone();
                now = Calendar.getInstance();
                boolean deadlineDaylight = deadlineTimeZone.inDaylightTime(now.getTime());
                timeZone2.setText((deadline.getTimeZone()).getDisplayName(deadlineDaylight, TimeZone.LONG));

                reminder2.setEnabled(true);
                reminder2.setText(getReminderButtonText(currentTask.getDeadlineReminder()));
            }
        }
    }

    private String getReminderButtonText(TaskReminder reminder) {
        String text;
        TextTool tool = new TextTool();

        if (reminder == null) {
            text = getString(R.string.add_reminder);
        }
        else {
            long minutes = reminder.getMinutes();

            if (minutes == 0) text = getString(R.string.on_time);
            else text = tool.getTaskReminderText(this, reminder);
        }

        return text;
    }

    public void onSaveItemSelected(MenuItem item) {
        String title = currentTask.getTitle();

        Calendar start = currentTask.getStart();
        Calendar deadline = currentTask.getDeadline();

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
        arguments.putString("description", currentTask.getDescription());
        fragment.setArguments(arguments);

        fragment.setOnOkButtonClickedListener(descriptionListener);
        fragment.show(getFragmentManager(), "edit_description");
    }

    public void onDateModeClicked(View view) {
        DateModeDialogFragment fragment = new DateModeDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putInt("mode", selectedDateMode);
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

    public void onTimeZone1Clicked(View view) {
        TimeZonePickerDialogFragment fragment = new TimeZonePickerDialogFragment();

        fragment.setOnTimeZoneSelectedListener(timeZoneSelectedListener1);
        fragment.show(getFragmentManager(), "time_zone_picker_1");
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

    public void onTimeZone2Clicked(View view) {
        TimeZonePickerDialogFragment fragment = new TimeZonePickerDialogFragment();

        fragment.setOnTimeZoneSelectedListener(timeZoneSelectedListener2);
        fragment.show(getFragmentManager(), "time_zone_picker_2");
    }

    public void onReminder1Clicked(View view) {
        TaskReminderPickerDialogFragment fragment = new TaskReminderPickerDialogFragment();
        Bundle arguments = new Bundle();
        TaskReminder reminder = null;

        if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
            reminder = currentTask.getWhenReminder();
        }
        else if (selectedDateMode == ApplicationLogic.DATE_RANGE) {
            reminder = currentTask.getStartReminder();
        }

        if (reminder != null) arguments.putLong("reminder_id", reminder.getId());

        fragment.setArguments(arguments);
        fragment.setOnTaskReminderSetListener(reminder1Listener);
        fragment.show(getFragmentManager(), "reminder_picker_1");
    }

    public void onReminder2Clicked(View view) {
        TaskReminderPickerDialogFragment fragment = new TaskReminderPickerDialogFragment();
        Bundle arguments = new Bundle();
        TaskReminder reminder = currentTask.getDeadlineReminder();

        if (reminder != null) arguments.putLong("reminder_id", reminder.getId());

        fragment.setArguments(arguments);
        fragment.setOnTaskReminderSetListener(reminder2Listener);
        fragment.show(getFragmentManager(), "reminder_picker_2");
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