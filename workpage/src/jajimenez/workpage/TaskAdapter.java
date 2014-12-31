package jajimenez.workpage;

import java.util.List;
import java.util.Calendar;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jajimenez.workpage.logic.DateTimeTool;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.data.model.Task;

public class TaskAdapter extends ArrayAdapter<Task> {
    private Activity activity;
    private int resource;

    public TaskAdapter(Activity activity, int resource, List<Task> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        TextView titleTextView = null;
        TextView details1TextView = null;
        TextView details2TextView = null;

        if (itemView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            itemView = inflater.inflate(resource, null);

            titleTextView = (TextView) itemView.findViewById(R.id.taskListItem_title);
            details1TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_1);
            details2TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_2);

            TaskItemViewTag viewTag = new TaskItemViewTag();

            viewTag.titleTextView = titleTextView;
            viewTag.details1TextView = details1TextView;
            viewTag.details2TextView = details2TextView;

            itemView.setTag(viewTag);
        }
        else {
            TaskItemViewTag viewTag = (TaskItemViewTag) itemView.getTag();

            titleTextView = viewTag.titleTextView;
            details1TextView = viewTag.details1TextView;
            details2TextView = viewTag.details2TextView;
        }

        Task task = getItem(position);

        String title = task.getTitle();
        Calendar start = task.getStart();
        Calendar deadline = task.getDeadline();
        List<TaskTag> tags = task.getTags();

        // Show title.
        titleTextView.setText(title);

        // Show Tags and Dates.
        int tagCount = tags.size();
        String tagsText = "";
        String datesText = (new DateTimeTool()).getTaskDatesText(activity, start, deadline);

        if (tags != null && tagCount > 0) {
            for (int i = 0; i < tagCount; i++) {
                tagsText += (tags.get(i)).getName();
                if (i < (tagCount - 1)) tagsText += ", ";
            }

            details1TextView.setText(tagsText);
            details2TextView.setText(datesText);
        }
        else {
            details1TextView.setText(datesText);
        }

        return itemView;
    }

    private class TaskItemViewTag {
        public TextView titleTextView;
        public TextView details1TextView;
        public TextView details2TextView;

        public TaskItemViewTag() {
            titleTextView = null;
            details1TextView = null;
            details2TextView = null;
        }
    }
}
