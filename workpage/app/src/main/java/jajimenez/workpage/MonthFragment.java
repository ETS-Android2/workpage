package jajimenez.workpage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.logic.TextTool;

public class MonthFragment extends Fragment {
    // Minimum supported date
    public static final int MIN_YEAR = 1900;
    public static final int MIN_MONTH = Calendar.JANUARY;

    // Maximum supported date
    public static final int MAX_YEAR = 10000;
    public static final int MAX_MONTH = Calendar.DECEMBER;

    private TextView title;
    private TableLayout table;
    private LinearLayout currentSelectedDateCell;

    private Drawable defaultDateDrawable;
    private Drawable taskDateDrawable;
    private Map<TextView, Integer> dateTextColors;
    private Drawable defaultDateNumberDrawable;
    private Drawable selectedDateNumberDrawable;

    // Date to represent
    private int currentYear;
    private int currentMonth;
    private Calendar current;

    private Map<LinearLayout, Calendar> dates;
    private OnGetTasksListener onGetTasksListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();

        if (arguments != null) {
            currentYear = arguments.getInt("year", MIN_YEAR);
            currentMonth = arguments.getInt("month", MIN_MONTH);
        } else {
            currentYear = MIN_YEAR;
            currentMonth = MIN_MONTH;
        }

        if (currentYear < MIN_YEAR || (currentYear == MIN_YEAR && currentMonth < MIN_MONTH)) {
            throw new IllegalArgumentException("Date lower than the minimum date supported.");
        }

        if (currentYear > MAX_YEAR || (currentYear == MAX_YEAR && currentMonth > MAX_MONTH)) {
            throw new IllegalArgumentException("Date greater than the maximum date supported.");
        }

        TextTool textTool = new TextTool();
        DateTimeTool dateTool = new DateTimeTool();

        // Date to represent
        current = Calendar.getInstance();
        current.set(Calendar.YEAR, currentYear);
        current.set(Calendar.MONTH, currentMonth);
        current.set(Calendar.DAY_OF_MONTH, 1);
        dateTool.clearTimeFields(current);

        View view = inflater.inflate(R.layout.month, container, false);

        title = view.findViewById(R.id.month_title);
        title.setText(textTool.getMonthYearName(current));

        table = view.findViewById(R.id.month_table);

        // Drawables
        TableRow row = (TableRow) table.getChildAt(1);
        LinearLayout cell = (LinearLayout) row.getChildAt(0);
        TextView text = (TextView) cell.getChildAt(0);

        Resources resources = getResources();
        defaultDateDrawable = cell.getBackground();
        taskDateDrawable = resources.getDrawable(R.drawable.task_date);

        dateTextColors = new HashMap<>();
        defaultDateNumberDrawable = text.getBackground();
        selectedDateNumberDrawable = resources.getDrawable(R.drawable.selected_date_number);

        // Interface
        setupWeekDayViews();
        updateInterface();

        return view;
    }

    private void setupWeekDayViews() {
        TextTool textTool = new TextTool();
        TableRow row = (TableRow) table.getChildAt(0);

        String[] names = textTool.getWeekDayShortNames();
        int count = names.length;

        for (int i = 0; i < count; i++) {
            TextView cell = (TextView) row.getChildAt(i);
            cell.setText(names[i]);
        }
    }

    private void updateInterface() {
        final Resources resources = getResources();
        DateTimeTool dateTool = new DateTimeTool();

        dates = new HashMap<>();

        List<Task> tasks = null;
        if (onGetTasksListener != null) tasks = onGetTasksListener.getTasks();

        int currentMonthDayCount = current.getActualMaximum(Calendar.DAY_OF_MONTH);

        // We get the cell indexes of the first day and the last day of the month
        int currentFirstDayIndex = current.get(Calendar.DAY_OF_WEEK) - 1;
        int currentLastDayIndex = currentFirstDayIndex + currentMonthDayCount - 1;

        // Previous month
        Calendar prev = (Calendar) current.clone();
        prev.add(Calendar.MONTH, -1);
        int prevMonthDayCount = prev.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Later month
        Calendar later = (Calendar) current.clone();
        later.add(Calendar.MONTH, 1);

        // Today
        Calendar today = Calendar.getInstance();
        dateTool.clearTimeFields(today);

        int i = 0;
        int rowCount = 7; // 7 = 1 header row + 6 regular rows
        int cellsPerRow = 7;

        for (int r = 1; r < rowCount; r++) {
            TableRow row = (TableRow) table.getChildAt(r);

            for (int c = 0; c < cellsPerRow; c++) {
                final LinearLayout cell = (LinearLayout) row.getChildAt(c);
                final TextView cellText = cell.findViewById(R.id.month_cell_day);

                cell.setOnClickListener(new LinearLayout.OnClickListener() {
                    public void onClick(View view) {
                        cellText.setBackground(MonthFragment.this.selectedDateNumberDrawable);
                        cellText.setTextColor(resources.getColor(R.color.selected_date_text));

                        if (MonthFragment.this.currentSelectedDateCell != null &&
                                MonthFragment.this.currentSelectedDateCell != cell) {
                            TextView t = (TextView) MonthFragment.this.currentSelectedDateCell.getChildAt(0);

                            t.setBackground(MonthFragment.this.defaultDateNumberDrawable);
                            t.setTextColor(dateTextColors.get(t));
                        }

                        // MonthFragment.this.selectedDate = dates.get(cell);

                        // if (MonthFragment.this.onDateSelectedListener != null) {
                        //     MonthFragment.this.onDateSelectedListener.onDateSelected(MonthFragment.this.selectedDate);
                        // }

                        MonthFragment.this.currentSelectedDateCell = cell;
                    }
                });

                int monthDay;
                Calendar date;

                if (i < currentFirstDayIndex) {
                    // The day belongs to the previous month
                    monthDay = prevMonthDayCount - currentFirstDayIndex + i + 1;
                    date = getDate(prev, monthDay);

                    if (date.getTimeInMillis() == today.getTimeInMillis()) {
                        cellText.setTextColor(resources.getColor(R.color.today));
                        cellText.setTypeface(null, Typeface.BOLD);
                    } else {
                        cellText.setTextColor(resources.getColor(R.color.disabled_text_color));
                        cellText.setTypeface(null, Typeface.NORMAL);
                    }

                } else if (i > currentLastDayIndex) {
                    // The day belong to the next month
                    monthDay = i - (currentFirstDayIndex + currentMonthDayCount) + 1;
                    date = getDate(later, monthDay);

                    if (date.getTimeInMillis() == today.getTimeInMillis()) {
                        cellText.setTextColor(resources.getColor(R.color.today));
                        cellText.setTypeface(null, Typeface.BOLD);
                    } else {
                        cellText.setTextColor(resources.getColor(R.color.disabled_text_color));
                        cellText.setTypeface(null, Typeface.NORMAL);
                    }

                } else {
                    // The day belongs to the current month
                    monthDay = i - currentFirstDayIndex + 1;
                    date = getDate(current, monthDay);

                    if (date.getTimeInMillis() == today.getTimeInMillis()) {
                        cellText.setTextColor(resources.getColor(R.color.today));
                        cellText.setTypeface(null, Typeface.BOLD);
                    } else {
                        cellText.setTextColor(resources.getColor(R.color.text_color));
                        cellText.setTypeface(null, Typeface.NORMAL);
                    }
                }

                cellText.setText(String.valueOf(monthDay));
                dateTextColors.put(cellText, cellText.getCurrentTextColor());
                dates.put(cell, date);

                if ((getDateTasks(tasks, date)).size() > 0) cell.setBackground(taskDateDrawable);
                else cell.setBackground(defaultDateDrawable);

                i++;
            }
        }
    }

    private Calendar getDate(Calendar c, int monthDay) {
        Calendar date = (Calendar) c.clone();
        date.set(Calendar.DAY_OF_MONTH, monthDay);

        return date;
    }

    private List<Task> getDateTasks(List<Task> tasks, Calendar date) {
        List<Task> dateTasks = new LinkedList<>();
        DateTimeTool tool = new DateTimeTool();

        if (tasks != null && date != null) {
            Calendar date2 = (Calendar) date.clone();
            tool.clearTimeFields(date2);
            long dateTime = date2.getTimeInMillis();

            for (Task t: tasks) {
                if (t != null) {
                    Calendar single = t.getSingle();

                    if (single != null) {
                        tool.clearTimeFields(single);

                        if (single.getTimeInMillis() == dateTime) {
                            dateTasks.add(t);
                        }
                    }

                    Calendar start = t.getStart();
                    Calendar end = t.getEnd();

                    if (start != null && end != null) {
                        tool.clearTimeFields(start);
                        tool.clearTimeFields(end);

                        if (start.getTimeInMillis() <= dateTime && end.getTimeInMillis() >= dateTime) {
                            dateTasks.add(t);
                        }
                    } else if (start != null) {
                        tool.clearTimeFields(start);

                        if (start.getTimeInMillis() == dateTime) {
                            dateTasks.add(t);
                        }

                    } else if (end != null) {
                        tool.clearTimeFields(end);

                        if (end.getTimeInMillis() == dateTime) {
                            dateTasks.add(t);
                        }
                    }
                }
            }
        }

        return dateTasks;
    }

    public void setOnGetTasksListener(OnGetTasksListener listener) {
        onGetTasksListener = listener;
    }

    public interface OnGetTasksListener {
        List<Task> getTasks();
    }
}
