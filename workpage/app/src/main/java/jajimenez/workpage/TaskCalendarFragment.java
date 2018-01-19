package jajimenez.workpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

public class TaskCalendarFragment extends Fragment implements TaskContainerFragment {
    private ViewPager pager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_calendar, container, false);

        pager = view.findViewById(R.id.task_calendar_pager);
        pager.setAdapter(new CalendarPagerAdapter(getFragmentManager()));
        pager.setCurrentItem(getInitialPageIndex());

        return view;
    }

    private int getInitialPageIndex() {
        int minYear = MonthFragment.MIN_YEAR; // Month 0 (January) of this year is the pager's item no. 0
        int maxYear = MonthFragment.MAX_YEAR;

        Calendar current = Calendar.getInstance();
        int currentYear = current.get(Calendar.YEAR);
        int currentMonth = current.get(Calendar.MONTH);

        if (currentYear < minYear) currentYear = minYear;
        else if (currentYear > maxYear) currentYear = maxYear;

        int yearIndex = currentYear - minYear;
        return ((yearIndex * 12) + currentMonth); // Item index for the current month
    }

    // Sets the position as the initial one (the one for the current date's month)
    public void resetIndex() {
        pager.setCurrentItem(getInitialPageIndex());
    }

    @Override
    public void setVisible(boolean visible) {
        View root = getView();

        if (visible) root.setVisibility(View.VISIBLE);
        else root.setVisibility(View.GONE);
    }
}
