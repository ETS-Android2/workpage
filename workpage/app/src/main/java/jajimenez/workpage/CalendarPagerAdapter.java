package jajimenez.workpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;

public class CalendarPagerAdapter extends FragmentStatePagerAdapter {
    private FragmentManager manager;
    private List<Task> tasks;

    public CalendarPagerAdapter(FragmentManager fm) {
        super(fm);
        manager = fm;
    }

    public void setTasks(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<>();
        this.tasks = tasks;

        List<Fragment> fragments = manager.getFragments();

        if (fragments != null) {
            for (Fragment f: fragments) {
                if (f instanceof MonthFragment) {
                    MonthFragment fragment = (MonthFragment) f;

                    fragment.setTasks(tasks);
                    fragment.updateInterface();
                }
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, MonthFragment.MIN_YEAR);
        c.set(Calendar.MONTH, MonthFragment.MIN_MONTH);
        c.add(Calendar.MONTH, position);

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);

        Bundle arguments = new Bundle();
        arguments.putInt("year", year);
        arguments.putInt("month", month);

        MonthFragment item = new MonthFragment();
        item.setTasks(tasks);
        item.setArguments(arguments);

        return item;
    }

    @Override
    public int getCount() {
        int yearCount = MonthFragment.MAX_YEAR - MonthFragment.MIN_YEAR + 1;
        return yearCount * 12; // Month count
    }
}
