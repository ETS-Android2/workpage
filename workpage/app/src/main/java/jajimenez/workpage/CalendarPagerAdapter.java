package jajimenez.workpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Calendar;

public class CalendarPagerAdapter extends FragmentStatePagerAdapter {
    public CalendarPagerAdapter(FragmentManager fm) {
        super(fm);
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
        item.setArguments(arguments);

        return item;
    }

    @Override
    public int getCount() {
        int yearCount = MonthFragment.MAX_YEAR - MonthFragment.MIN_YEAR + 1;
        return yearCount * 12; // Month count
    }
}
