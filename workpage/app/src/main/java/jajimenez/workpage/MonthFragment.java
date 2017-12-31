package jajimenez.workpage;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
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
    private Drawable selectedDateDrawable;

    // Date to represent
    private int currentYear;
    private int currentMonth;
    private Calendar current;

    private Map<LinearLayout, Calendar> dates;
    private OnGetTasksListener onGetTasksListener;
    
    // private Calendar selectedDate;
    // private OnDateSelectedListener onDateSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            // activity = (TaskListHostActivity) context;
        } catch (ClassCastException e) {
            // throw new ClassCastException(context.toString() +
            //         " must implement TaskListHostActivity");
        }
    }

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

        // Date to represent
        current = Calendar.getInstance();
        current.set(Calendar.YEAR, currentYear);
        current.set(Calendar.MONTH, currentMonth);
        current.set(Calendar.DAY_OF_MONTH, 1);

        dates = new HashMap<>();

        View view = inflater.inflate(R.layout.month, container, false);

        title = view.findViewById(R.id.month_title);
        title.setText(textTool.getMonthYearName(current));

        table = view.findViewById(R.id.month_table);

        TableRow secondRow = (TableRow) table.getChildAt(1);
        LinearLayout firstCell = (LinearLayout) secondRow.getChildAt(0);

        defaultDateDrawable = firstCell.getBackground();
        selectedDateDrawable = (getResources()).getDrawable(R.drawable.selected_date);

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
        List<Task> tasks;
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
                        cell.setBackground(MonthFragment.this.selectedDateDrawable);

                        if (MonthFragment.this.currentSelectedDateCell != null) {
                            MonthFragment.this.currentSelectedDateCell.setBackground(MonthFragment.this.defaultDateDrawable);
                        }

                        // MonthFragment.this.selectedDate = dates.get(cell);

                        // if (MonthFragment.this.onDateSelectedListener != null) {
                        //     MonthFragment.this.onDateSelectedListener.onDateSelected(MonthFragment.this.selectedDate);
                        // }

                        MonthFragment.this.currentSelectedDateCell = cell;
                    }
                });

                int monthDay;
                Resources resources = getResources();

                if (i < currentFirstDayIndex) {
                    // The day belongs to the previous month
                    monthDay = prevMonthDayCount - currentFirstDayIndex + i + 1;

                    cellText.setText(String.valueOf(monthDay));
                    cellText.setTextColor(resources.getColor(R.color.disabled_text_color));

                    dates.put(cell, getDate(prev, monthDay));

                } else if (i > currentLastDayIndex) {
                    // The day belong to the next month
                    monthDay = i - (currentFirstDayIndex + currentMonthDayCount) + 1;

                    cellText.setText(String.valueOf(monthDay));
                    cellText.setTextColor(resources.getColor(R.color.disabled_text_color));

                    dates.put(cell, getDate(later, monthDay));

                } else {
                    // The day belongs to the current month
                    monthDay = i - currentFirstDayIndex + 1;

                    cellText.setText(String.valueOf(monthDay));
                    cellText.setTextColor(resources.getColor(R.color.text_color));

                    dates.put(cell, getDate(current, monthDay));
                }

                i++;
            }
        }
    }

    private Calendar getDate(Calendar c, int monthDay) {
        Calendar date = (Calendar) c.clone();
        date.set(Calendar.DAY_OF_MONTH, monthDay);

        return date;
    }

    // public void setOnDateSelectedListener(OnDateSelectedListener listener) {
    //     onDateSelectedListener = listener;
    // }
    //
    // public interface OnDateSelectedListener {
    //     void onDateSelected(Calendar date);
    // }

    public void setOnGetTasksListener(OnGetTasksListener listener) {
        onGetTasksListener = listener;
    }

    public interface OnGetTasksListener {
        List<Task> getTasks();
    }
}
