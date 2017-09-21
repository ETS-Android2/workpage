package jajimenez.workpage;

import java.util.LinkedList;
import java.util.List;
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
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.data.model.TaskReminder;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class EditTaskActivity extends AppCompatActivity {
    private EditText title;

    private Button dateMode;

    private TextView dateMode1;
    private TableLayout tableDate1;
    private Button date1;
    private Button time1;
    private TableRow timeZoneRow1;
    private Button timeZone1;
    private TableRow reminderRow1;
    private Button reminder1;
    private View divider1;

    private TextView dateMode2;
    private TableLayout tableDate2;
    private Button date2;
    private Button time2;
    private TableRow timeZoneRow2;
    private Button timeZone2;
    private TableRow reminderRow2;
    private Button reminder2;
    private View divider2;

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

    private TaskTagPickerDialogFragment.OnTaskTagsSelectedListener tagsSelectedListener;

    private ApplicationLogic applicationLogic = null;
    private Task currentTask = null;

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

        applicationLogic = new ApplicationLogic(this);
        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        title = (EditText) findViewById(R.id.edit_task_title);
        dateMode = (Button) findViewById(R.id.edit_task_date_mode);

        dateMode1 = (TextView) findViewById(R.id.edit_task_date_mode_1);
        tableDate1 = (TableLayout) findViewById(R.id.edit_task_table_date_1);
        date1 = (Button) findViewById(R.id.edit_task_date_1);
        time1 = (Button) findViewById(R.id.edit_task_time_1);
        timeZoneRow1 = (TableRow) findViewById(R.id.edit_task_row_time_zone_1);
        timeZone1 = (Button) findViewById(R.id.edit_task_time_zone_1);
        reminderRow1 = (TableRow) findViewById(R.id.edit_task_row_reminder_1);
        reminder1 = (Button) findViewById(R.id.edit_task_reminder_1);
        divider1 = findViewById(R.id.edit_task_date_divider_1);

        dateMode2 = (TextView) findViewById(R.id.edit_task_date_mode_2);
        tableDate2 = (TableLayout) findViewById(R.id.edit_task_table_date_2);
        date2 = (Button) findViewById(R.id.edit_task_date_2);
        time2 = (Button) findViewById(R.id.edit_task_time_2);
        timeZoneRow2 = (TableRow) findViewById(R.id.edit_task_row_time_zone_2);
        timeZone2 = (Button) findViewById(R.id.edit_task_time_zone_2);
        reminderRow2 = (TableRow) findViewById(R.id.edit_task_row_reminder_2);
        reminder2 = (Button) findViewById(R.id.edit_task_reminder_2);
        divider2 = findViewById(R.id.edit_task_date_divider_2);

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

        if (mode != null && mode.equals("new")) title.requestFocus();

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
                        Calendar single = Calendar.getInstance();
                        single.add(Calendar.HOUR_OF_DAY, 1);
                        single.clear(Calendar.MINUTE);

                        currentTask.setSingle(single);
                        currentTask.setStart(null);
                        currentTask.setEnd(null);

                        break;
                    case ApplicationLogic.DATE_RANGE:
                        // Today
                        Calendar start = Calendar.getInstance();
                        start.add(Calendar.HOUR_OF_DAY, 1);
                        start.clear(Calendar.MINUTE);

                        // Tomorrow
                        Calendar end = Calendar.getInstance();
                        end.setTimeInMillis(start.getTimeInMillis());
                        end.add(Calendar.DAY_OF_MONTH, 1);

                        currentTask.setSingle(null);
                        currentTask.setStart(start);
                        currentTask.setEnd(end);

                        break;
                }

                currentTask.setIgnoreSingleTime(false);
                currentTask.setIgnoreStartTime(false);
                currentTask.setIgnoreEndTime(false);

                currentTask.setSingleReminder(null);
                currentTask.setStartReminder(null);
                currentTask.setEndReminder(null);

                EditTaskActivity.this.updateInterface();
            }
        };

        timeZoneSelectedListener1 = new TimeZonePickerDialogFragment.OnTimeZoneSelectedListener() {
            @Override
            public void onTimeZoneSelected(TimeZone t) {
                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) (currentTask.getSingle()).setTimeZone(t);
                else (currentTask.getStart()).setTimeZone(t);

                EditTaskActivity.this.updateInterface();
            }
        };

        timeZoneSelectedListener2 = new TimeZonePickerDialogFragment.OnTimeZoneSelectedListener() {
            @Override
            public void onTimeZoneSelected(TimeZone t) {
                (currentTask.getEnd()).setTimeZone(t);
                EditTaskActivity.this.updateInterface();
            }
        };

        // Date and Time 1
        date1Listener = new DatePickerDialogFragment.OnDateSetListener() {
            public void onDateSet(int year, int month, int day) {
                Calendar date;
                DateTimeTool tool = new DateTimeTool();

                if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    date = EditTaskActivity.this.currentTask.getSingle();
                }
                else {
                    date = EditTaskActivity.this.currentTask.getStart();
                }

                if (date == null) {
                    date = Calendar.getInstance();
                    tool.clearTimeFields(date);

                    if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                        EditTaskActivity.this.currentTask.setSingle(date);
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
                    EditTaskActivity.this.currentTask.setSingle(null);
                    EditTaskActivity.this.currentTask.setIgnoreSingleTime(true);
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
                    date = EditTaskActivity.this.currentTask.getSingle();
                    EditTaskActivity.this.currentTask.setIgnoreSingleTime(false);
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
                    date = EditTaskActivity.this.currentTask.getSingle();
                    EditTaskActivity.this.currentTask.setIgnoreSingleTime(true);
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
                Calendar end = EditTaskActivity.this.currentTask.getEnd();
                DateTimeTool tool = new DateTimeTool();

                if (end == null) {
                    end = Calendar.getInstance();
                    tool.clearTimeFields(end);

                    EditTaskActivity.this.currentTask.setEnd(end);
                }

                end.set(Calendar.YEAR, year);
                end.set(Calendar.MONTH, month);
                end.set(Calendar.DAY_OF_MONTH, day);

                EditTaskActivity.this.updateInterface();
            }
        };

        noDate2Listener = new DatePickerDialogFragment.OnNoDateSetListener() {
            public void onNoDateSet() {
                EditTaskActivity.this.currentTask.setEnd(null);
                EditTaskActivity.this.currentTask.setIgnoreEndTime(true);

                EditTaskActivity.this.updateInterface();
            }
        };

        time2Listener = new TimePickerDialogFragment.OnTimeSetListener() {
            public void onTimeSet(int hour, int minute) {
                DateTimeTool tool = new DateTimeTool();
                Calendar end = EditTaskActivity.this.currentTask.getEnd();

                if (end == null) end = Calendar.getInstance();
                tool.clearTimeFields(end);

                end.set(Calendar.HOUR_OF_DAY, hour);
                end.set(Calendar.MINUTE, minute);

                EditTaskActivity.this.currentTask.setIgnoreEndTime(false);
                EditTaskActivity.this.updateInterface();
            }
        };

        noTime2Listener = new TimePickerDialogFragment.OnNoTimeSetListener() {
            public void onNoTimeSet() {
                DateTimeTool tool = new DateTimeTool();
                Calendar end = EditTaskActivity.this.currentTask.getEnd();

                if (end == null) end = Calendar.getInstance();
                tool.clearTimeFields(end);

                EditTaskActivity.this.currentTask.setIgnoreEndTime(true);
                EditTaskActivity.this.updateInterface();
            }
        };

        reminder1Listener = new TaskReminderPickerDialogFragment.OnTaskReminderSetListener() {
            public void onTaskReminderSet(TaskReminder reminder) {
                if (EditTaskActivity.this.selectedDateMode == ApplicationLogic.SINGLE_DATE) {
                    EditTaskActivity.this.currentTask.setSingleReminder(reminder);
                }
                else if (EditTaskActivity.this.selectedDateMode == ApplicationLogic.DATE_RANGE) {
                    EditTaskActivity.this.currentTask.setStartReminder(reminder);
                }

                EditTaskActivity.this.updateInterface();
            }
        };

        reminder2Listener = new TaskReminderPickerDialogFragment.OnTaskReminderSetListener() {
            public void onTaskReminderSet(TaskReminder reminder) {
                EditTaskActivity.this.currentTask.setEndReminder(reminder);
                EditTaskActivity.this.updateInterface();
            }
        };

        // Tags
        tagsSelectedListener = new TaskTagPickerDialogFragment.OnTaskTagsSelectedListener() {
            public void onTaskTagsSelected(List<TaskTag> tags) {
                EditTaskActivity.this.currentTask.setTags(tags);
                EditTaskActivity.this.updateInterface();
            }
        };

        if (mode != null && mode.equals("edit")) {
            // Get task data
            long taskId = intent.getLongExtra("task_id", 0);
            currentTask = applicationLogic.getTask(taskId);
        }
        else {
            long contextId = intent.getLongExtra("task_context_id", 0);

            currentTask = new Task();
            currentTask.setContextId(contextId);
        }

        if (savedInstanceState == null) {
            Calendar single = currentTask.getSingle();
            Calendar start = currentTask.getStart();
            Calendar end = currentTask.getEnd();

            if (single == null && start == null && end == null) selectedDateMode = ApplicationLogic.NO_DATE;
            else if (single == null) selectedDateMode = ApplicationLogic.DATE_RANGE;
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

            TaskTagPickerDialogFragment tagsFragment = (TaskTagPickerDialogFragment) (getFragmentManager()).findFragmentByTag("task_tags_picker");
            if (tagsFragment != null) {
                tagsFragment.setOnTaskTagsSelectedListener(tagsSelectedListener);
            }

            String title = savedInstanceState.getString("title");
            String description = savedInstanceState.getString("description");

            currentTask.setTitle(title);
            currentTask.setDescription(description);

            // Date mode
            selectedDateMode = savedInstanceState.getInt("selected_date_mode");

            // Dates and time
            long single = savedInstanceState.getLong("single", -1);
            long start = savedInstanceState.getLong("start", -1);
            long end = savedInstanceState.getLong("end", -1);

            if (single >= 0) {
                boolean ignoreSingleTime = savedInstanceState.getBoolean("ignore_single_time", false);
                String singleTimeZoneCode = savedInstanceState.getString("single_time_zone_code", null);
                long singleReminderId = savedInstanceState.getLong("single_reminder_id", -1);

                Calendar calSingle = Calendar.getInstance();
                calSingle.setTimeInMillis(single);

                if (singleTimeZoneCode != null) calSingle.setTimeZone(TimeZone.getTimeZone(singleTimeZoneCode));
                if (singleReminderId >= 0) currentTask.setSingleReminder(applicationLogic.getTaskReminder(singleReminderId));

                currentTask.setSingle(calSingle);
                currentTask.setIgnoreSingleTime(ignoreSingleTime);
            }

            if (start >= 0) {
                boolean ignoreStartTime = savedInstanceState.getBoolean("ignore_start_time", false);
                String startTimeZoneCode = savedInstanceState.getString("start_time_zone_code", null);
                long startReminderId = savedInstanceState.getLong("start_reminder_id", -1);

                Calendar calStart = Calendar.getInstance();
                calStart.setTimeInMillis(start);

                if (startTimeZoneCode != null) calStart.setTimeZone(TimeZone.getTimeZone(startTimeZoneCode));
                if (startReminderId >= 0) currentTask.setStartReminder(applicationLogic.getTaskReminder(startReminderId));

                currentTask.setStart(calStart);
                currentTask.setIgnoreSingleTime(ignoreStartTime);
            }

            if (end >= 0) {
                boolean ignoreEndTime = savedInstanceState.getBoolean("ignore_end_time", false);
                String endTimeZoneCode = savedInstanceState.getString("end_time_zone_code", null);
                long endReminderId = savedInstanceState.getLong("end_reminder_id", -1);

                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(end);

                if (endTimeZoneCode != null) calEnd.setTimeZone(TimeZone.getTimeZone(endTimeZoneCode));
                if (endReminderId >= 0) currentTask.setEndReminder(applicationLogic.getTaskReminder(endReminderId));

                currentTask.setEnd(calEnd);
                currentTask.setIgnoreEndTime(ignoreEndTime);
            }

            String[] tagNames = savedInstanceState.getStringArray("task_tags");
            LinkedList<TaskTag> tags = new LinkedList<TaskTag>();

            if (tagNames != null) {
                for (String name : tagNames) {
                    TaskTag tag = new TaskTag();
                    tag.setName(name);
                    tag.setContextId(currentTask.getContextId());

                    tags.add(tag);
                }
            }

            currentTask.setTags(tags);
        }

        updateInterface();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("selected_date_mode", selectedDateMode);

        outState.putString("title", currentTask.getTitle());
        outState.putString("description", currentTask.getDescription());

        // Selected dates and reminders.
        Calendar single = currentTask.getSingle();
        Calendar start = currentTask.getStart();
        Calendar end = currentTask.getEnd();

        if (single != null) {
            outState.putLong("single", single.getTimeInMillis());
            outState.putBoolean("ignore_single_time", currentTask.getIgnoreSingleTime());

            TimeZone singleTimeZone = single.getTimeZone();
            if (singleTimeZone != null) {
                String singleTimeZoneCode = singleTimeZone.getID();

                if (singleTimeZoneCode != null && !singleTimeZoneCode.isEmpty()) {
                    outState.putString("single_time_zone_code", singleTimeZoneCode);
                }
            }

            TaskReminder singleReminder = currentTask.getSingleReminder();
            if (singleReminder != null) outState.putLong("single_reminder_id", singleReminder.getId());
        }

        if (start != null) {
            outState.putLong("start", start.getTimeInMillis());
            outState.putBoolean("ignore_start_time", currentTask.getIgnoreStartTime());

            TimeZone startTimeZone = start.getTimeZone();
            if (startTimeZone != null) {
                String startTimeZoneCode = startTimeZone.getID();

                if (startTimeZoneCode != null && !startTimeZoneCode.isEmpty()) {
                    outState.putString("start_time_zone_code", startTimeZoneCode);
                }
            }

            TaskReminder startReminder = currentTask.getStartReminder();
            if (startReminder != null) outState.putLong("start_reminder_id", startReminder.getId());
        }

        if (end != null) {
            outState.putLong("end", end.getTimeInMillis());
            outState.putBoolean("ignore_end_time", currentTask.getIgnoreEndTime());

            TimeZone endTimeZone = end.getTimeZone();
            if (endTimeZone != null) {
                String endTimeZoneCode = endTimeZone.getID();

                if (endTimeZoneCode != null && !endTimeZoneCode.isEmpty()) {
                    outState.putString("end_time_zone_code", endTimeZoneCode);
                }
            }

            TaskReminder endReminder = currentTask.getEndReminder();
            if (endReminder != null) outState.putLong("end_reminder_id", endReminder.getId());
        }

        // Task tags
        List<TaskTag> tags = currentTask.getTags();
        int tagCount = tags.size();
        String[] tagNames = new String[tagCount];
        for (int i = 0; i < tagCount; i++) tagNames[i] = (tags.get(i)).getName();

        outState.putStringArray("task_tags", tagNames);
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
        title.setSelection((title.getText()).length());

        TextTool tool = new TextTool();
        Calendar now;

        if (selectedDateMode == ApplicationLogic.NO_DATE) {
            dateMode.setText(R.string.add_date);

            dateMode1.setVisibility(View.GONE);
            tableDate1.setVisibility(View.GONE);
            divider1.setVisibility(View.GONE);

            dateMode2.setVisibility(View.GONE);
            tableDate2.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);
        }
        else if (selectedDateMode == ApplicationLogic.SINGLE_DATE) {
            Calendar single = currentTask.getSingle();

            dateMode.setText(R.string.single_date);

            dateMode1.setVisibility(View.GONE);
            tableDate1.setVisibility(View.VISIBLE);
            timeZoneRow1.setVisibility(View.VISIBLE);
            reminderRow1.setVisibility(View.VISIBLE);
            divider1.setVisibility(View.VISIBLE);

            dateMode2.setVisibility(View.GONE);
            tableDate2.setVisibility(View.GONE);
            divider2.setVisibility(View.GONE);

            date1.setText(tool.getFormattedDate(single, false));
            time1.setVisibility(View.VISIBLE);

            if (currentTask.getIgnoreSingleTime()) time1.setText(R.string.add_time);
            else time1.setText(tool.getFormattedTime(single, false));

            TimeZone singleTimeZone = single.getTimeZone();
            now = Calendar.getInstance();
            boolean singleDaylight = singleTimeZone.inDaylightTime(now.getTime());
            timeZone1.setText((single.getTimeZone()).getDisplayName(singleDaylight, TimeZone.LONG));

            reminder1.setText(getReminderButtonText(currentTask.getSingleReminder()));
        }
        else {
            Calendar start = currentTask.getStart();
            Calendar end = currentTask.getEnd();

            dateMode.setText(R.string.date_range);

            // Start
            dateMode1.setVisibility(View.VISIBLE);
            tableDate1.setVisibility(View.VISIBLE);
            divider1.setVisibility(View.VISIBLE);

            // End
            dateMode2.setVisibility(View.VISIBLE);
            tableDate2.setVisibility(View.VISIBLE);
            divider2.setVisibility(View.VISIBLE);

            if (start == null) {
                date1.setText(R.string.add_date);
                time1.setText(R.string.add_time);
                time1.setVisibility(View.GONE);
                timeZoneRow1.setVisibility(View.GONE);
                reminderRow1.setVisibility(View.GONE);
            }
            else {
                date1.setText(tool.getFormattedDate(start, false));

                if (currentTask.getIgnoreStartTime()) time1.setText(R.string.add_time);
                else time1.setText(tool.getFormattedTime(start, false));

                time1.setVisibility(View.VISIBLE);
                timeZoneRow1.setVisibility(View.VISIBLE);

                TimeZone startTimeZone = start.getTimeZone();
                now = Calendar.getInstance();
                boolean startDaylight = startTimeZone.inDaylightTime(now.getTime());
                timeZone1.setText((start.getTimeZone()).getDisplayName(startDaylight, TimeZone.LONG));

                reminderRow1.setVisibility(View.VISIBLE);
                reminder1.setText(getReminderButtonText(currentTask.getStartReminder()));
            }

            if (end == null) {
                date2.setText(R.string.add_date);
                time2.setText(R.string.add_time);
                time2.setVisibility(View.GONE);
                timeZoneRow2.setVisibility(View.GONE);
                reminderRow2.setVisibility(View.GONE);
            }
            else {
                date2.setText(tool.getFormattedDate(end, false));

                if (currentTask.getIgnoreEndTime()) time2.setText(R.string.add_time);
                else time2.setText(tool.getFormattedTime(end, false));

                time2.setVisibility(View.VISIBLE);
                timeZoneRow2.setVisibility(View.VISIBLE);

                TimeZone endTimeZone = end.getTimeZone();
                now = Calendar.getInstance();
                boolean endDaylight = endTimeZone.inDaylightTime(now.getTime());
                timeZone2.setText((end.getTimeZone()).getDisplayName(endDaylight, TimeZone.LONG));

                reminderRow2.setVisibility(View.VISIBLE);
                reminder2.setText(getReminderButtonText(currentTask.getEndReminder()));
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
        Calendar end = currentTask.getEnd();

        // Check values
        boolean titleValid = (title.length() > 0);
        boolean datesValid;

        datesValid = selectedDateMode != ApplicationLogic.DATE_RANGE ||
            start == null ||
            end == null ||
            compareCalendars(end, currentTask.getIgnoreEndTime(), start, currentTask.getIgnoreStartTime()) >= 0;

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
            date = currentTask.getSingle();
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

        if (selectedDateMode == ApplicationLogic.SINGLE_DATE) time = currentTask.getSingle();
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
        Calendar date = currentTask.getEnd();

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
        Calendar time = currentTask.getEnd();

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
            reminder = currentTask.getSingleReminder();
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
        TaskReminder reminder = currentTask.getEndReminder();

        if (reminder != null) arguments.putLong("reminder_id", reminder.getId());

        fragment.setArguments(arguments);
        fragment.setOnTaskReminderSetListener(reminder2Listener);
        fragment.show(getFragmentManager(), "reminder_picker_2");
    }

    public void onTagsClicked(View view) {
        TaskTagPickerDialogFragment fragment = new TaskTagPickerDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putLong("task_context_id", currentTask.getContextId());

        // Current selected tags
        List<TaskTag> tags = currentTask.getTags();
        int tagCount = tags.size();
        String[] tagNames = new String[tagCount];
        for (int i = 0; i < tagCount; i++) tagNames[i] = (tags.get(i)).getName();

        arguments.putStringArray("initial_selected_tags", tagNames);
        fragment.setArguments(arguments);

        fragment.setOnTaskTagsSelectedListener(tagsSelectedListener);
        fragment.show(getFragmentManager(), "task_tags_picker");
    }
}