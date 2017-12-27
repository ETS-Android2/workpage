package jajimenez.workpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
// import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;

public class TaskCalendarFragment extends Fragment implements TaskContainerFragment {
    private ViewPager pager;

    // private List<Task> tasks;

    public TaskCalendarFragment() {
        // tasks = new LinkedList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_calendar, container, false);

        pager = view.findViewById(R.id.task_calendar_pager);
        pager.setAdapter(new CalendarPagerAdapter((getActivity()).getSupportFragmentManager()));
        setCurrentMonth();

        return view;
    }

    private int getCurrentMonthIndex() {
        int minYear = MonthFragment.MIN_YEAR; // Month 0 (January) of this year is the pager's item no. 0
        int maxYear = MonthFragment.MAX_YEAR;

        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH);

        if (currentYear < minYear) currentYear = minYear;
        else if (currentYear > maxYear) currentYear = maxYear;

        // Item index for the current month
        int yearIndex = currentYear - minYear;
        int monthIndex = (yearIndex * 12) + currentMonth;

        return monthIndex;
    }

    private void updateInterface() {
    }

    public void setCurrentMonth() {
        pager.setCurrentItem(getCurrentMonthIndex());
    }

    @Override
    public void setVisible(boolean visible) {
        View root = getView();

        if (visible) root.setVisibility(View.VISIBLE);
        else root.setVisibility(View.GONE);
    }

    @Override
    public void setEnabled(boolean enabled) {
        pager.setEnabled(enabled);
    }

    @Override
    public void setTasks(List<Task> tasks) {
        // if (tasks == null) tasks = new LinkedList<>();

        // this.tasks = tasks;
        updateInterface();
    }
}
