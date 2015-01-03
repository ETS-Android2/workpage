package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskTag;

public class TaskTagAdapter extends ArrayAdapter<TaskTag> {
    private Activity activity;
    private int resource;

    public TaskTagAdapter(Activity activity, int resource, List<TaskTag> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        TextView nameTextView = null;
        TextView openTaskCountTextView = null;
        TextView closedTaskCountTextView = null;

        if (itemView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            itemView = inflater.inflate(resource, null);

            nameTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_name);
            openTaskCountTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_open);
            closedTaskCountTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_closed);

            TaskTagItemViewTag viewTag = new TaskTagItemViewTag();
            viewTag.nameTextView = nameTextView;
            viewTag.openTaskCountTextView = openTaskCountTextView;
            viewTag.closedTaskCountTextView = closedTaskCountTextView;

            itemView.setTag(viewTag);
        }
        else {
            TaskTagItemViewTag viewTag = (TaskTagItemViewTag) itemView.getTag();
            nameTextView = viewTag.nameTextView;
            openTaskCountTextView = viewTag.openTaskCountTextView;
            closedTaskCountTextView = viewTag.closedTaskCountTextView;
        }

        ApplicationLogic applicationLogic = new ApplicationLogic(activity);

        TaskTag tag = getItem(position);
        String name = tag.getName();

        long openTaskCount = applicationLogic.getTaskCount(false, tag);
        long closedTaskCount = applicationLogic.getTaskCount(true, tag);

        String openTaskCountText = activity.getString(R.string.open_task_count, openTaskCount);
        String closedTaskCountText = activity.getString(R.string.closed_task_count, closedTaskCount);

        // Show name and task counts.
        nameTextView.setText(name);
        openTaskCountTextView.setText(openTaskCountText);
        closedTaskCountTextView.setText(closedTaskCountText);

        return itemView;
    }

    private static class TaskTagItemViewTag {
        public TextView nameTextView;
        public TextView openTaskCountTextView;
        public TextView closedTaskCountTextView;

        public TaskTagItemViewTag() {
            nameTextView = null;
            openTaskCountTextView = null;
            closedTaskCountTextView = null;
        }
    }
}
