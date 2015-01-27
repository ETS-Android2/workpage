package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.res.Resources;

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
        ColorView colorView = null;
        TextView nameTextView = null;
        TextView openTaskCountTextView = null;
        TextView closedTaskCountTextView = null;

        if (itemView == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            itemView = inflater.inflate(resource, null);

            colorView = (ColorView) itemView.findViewById(R.id.taskTagListItem_color);
            nameTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_name);
            openTaskCountTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_open);
            closedTaskCountTextView = (TextView) itemView.findViewById(R.id.taskTagListItem_closed);

            TaskTagItemViewTag viewTag = new TaskTagItemViewTag();
            viewTag.colorView = colorView;
            viewTag.nameTextView = nameTextView;
            viewTag.openTaskCountTextView = openTaskCountTextView;
            viewTag.closedTaskCountTextView = closedTaskCountTextView;

            itemView.setTag(viewTag);
        }
        else {
            TaskTagItemViewTag viewTag = (TaskTagItemViewTag) itemView.getTag();
            colorView = viewTag.colorView;
            nameTextView = viewTag.nameTextView;
            openTaskCountTextView = viewTag.openTaskCountTextView;
            closedTaskCountTextView = viewTag.closedTaskCountTextView;
        }

        ApplicationLogic applicationLogic = new ApplicationLogic(activity);

        TaskTag tag = getItem(position);
        String color = tag.getColor();
        String name = tag.getName();

        int openTaskCount = applicationLogic.getTaskCount(false, tag);
        int closedTaskCount = applicationLogic.getTaskCount(true, tag);

        Resources resources = activity.getResources();

        String openTaskCountText = resources.getQuantityString(R.plurals.open_task_count, openTaskCount, openTaskCount);
        String closedTaskCountText = resources.getQuantityString(R.plurals.closed_task_count, closedTaskCount, closedTaskCount);

        // Show color, name and task counts.
        if (color != null && !(color.trim()).equals("")) {
            colorView.setVisibility(View.VISIBLE);
            colorView.setColor(color);
        }

        nameTextView.setText(name);
        openTaskCountTextView.setText(openTaskCountText);
        closedTaskCountTextView.setText(closedTaskCountText);

        return itemView;
    }

    private static class TaskTagItemViewTag {
        public ColorView colorView;
        public TextView nameTextView;
        public TextView openTaskCountTextView;
        public TextView closedTaskCountTextView;

        public TaskTagItemViewTag() {
            colorView = null;
            nameTextView = null;
            openTaskCountTextView = null;
            closedTaskCountTextView = null;
        }
    }
}
