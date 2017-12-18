package jajimenez.workpage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;

public class TaskCalendarFragment extends Fragment implements TaskContainerFragment {
    private CalendarView calendar;

    private List<Task> tasks;

    public TaskCalendarFragment() {
        tasks = new LinkedList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.task_calendar, container, false);
        calendar = view.findViewById(R.id.task_calendar_calendar);

        return view;
    }

    private void updateInterface() {
    }

    @Override
    public void setEnabled(boolean enabled) {
        calendar.setEnabled(enabled);
    }

    @Override
    public void setTasks(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<>();

        this.tasks = tasks;
        updateInterface();
    }
}
