package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class TaskCalendarFragment extends Fragment implements TaskContainerFragment {
    private ViewPager pager;
    private int initialIndex;
    private List<Task> tasks;

    private AppBroadcastReceiver appBroadcastReceiver;
    private LoadTasksDBTask tasksDbTask = null;
    private MonthFragment.OnGetTasksListener onGetTasksListener;

    public TaskCalendarFragment() {
        tasks = new LinkedList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_calendar, container, false);
        pager = view.findViewById(R.id.task_calendar_pager);

        if (savedInstanceState == null) initialIndex = getInitialPageIndex();
        else initialIndex = savedInstanceState.getInt("index", getInitialPageIndex());

        onGetTasksListener = new MonthFragment.OnGetTasksListener() {
            @Override
            public List<Task> getTasks() {
                return TaskCalendarFragment.this.tasks;
            }
        };

        // Initial task load
        loadTasks();

        // Broadcast receiver
        registerBroadcastReceiver();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Broadcast receiver
        unregisterBroadcastReceiver();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("index", pager.getCurrentItem());
    }

    private void registerBroadcastReceiver() {
        appBroadcastReceiver = new AppBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter(ApplicationLogic.ACTION_DATA_CHANGED);

        (LocalBroadcastManager.getInstance(getContext())).registerReceiver(appBroadcastReceiver, intentFilter);
    }

    private void unregisterBroadcastReceiver() {
        (LocalBroadcastManager.getInstance(getContext())).unregisterReceiver(appBroadcastReceiver);
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

    private void loadTasks() {
        if (tasksDbTask == null || tasksDbTask.getStatus() == AsyncTask.Status.FINISHED) {
            tasksDbTask = new LoadTasksDBTask();
            tasksDbTask.execute();
        }
    }

    private class AppBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null && action.equals(ApplicationLogic.ACTION_DATA_CHANGED)) {
                // Get the tasks
                TaskCalendarFragment.this.loadTasks();
            }
        }
    }

    private class LoadTasksDBTask extends AsyncTask<Void, Void, List<Task>> {
        private int index;

        protected void onPreExecute() {
            TaskCalendarFragment.this.pager.setEnabled(false);

            if (TaskCalendarFragment.this.pager.getAdapter() == null) {
                index = TaskCalendarFragment.this.initialIndex;
            } else {
                index = TaskCalendarFragment.this.pager.getCurrentItem();
            }
        }

        protected List<Task> doInBackground(Void... parameters) {
            List<Task> tasks = new LinkedList<>();

            try {
                ApplicationLogic applicationLogic = new ApplicationLogic(TaskCalendarFragment.this.getContext());
                TaskContext currentTaskContext = applicationLogic.getCurrentTaskContext();

                // View filters
                String viewStateFilter = applicationLogic.getViewStateFilter();
                boolean includeTasksWithNoTag = applicationLogic.getIncludeTasksWithNoTag();
                List<TaskTag> currentFilterTags = applicationLogic.getCurrentFilterTags();

                switch (viewStateFilter) {
                    case "open":
                        tasks = applicationLogic.getOpenTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                        break;
                    case "doable_today":
                        tasks = applicationLogic.getDoableTodayTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                        break;
                    default:
                        tasks = applicationLogic.getClosedTasksByTags(currentTaskContext,
                                includeTasksWithNoTag,
                                currentFilterTags);
                }
            }
            catch (Exception e) {
                // Nothing to do
            }

            return tasks;
        }

        protected void onPostExecute(List<Task> tasks) {
            try {
                TaskCalendarFragment.this.tasks = tasks;

                CalendarPagerAdapter adapter = new CalendarPagerAdapter((getActivity()).getSupportFragmentManager());
                adapter.setOnGetTasksListener(TaskCalendarFragment.this.onGetTasksListener);

                TaskCalendarFragment.this.pager.setAdapter(adapter);
                TaskCalendarFragment.this.pager.setCurrentItem(index);

                TaskCalendarFragment.this.pager.setEnabled(true);
            } catch (Exception e) {
                // Nothing to do
            }
        }
    }
}
