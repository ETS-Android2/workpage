package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.res.Resources;
import android.graphics.Color;

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
        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        ColorView colorView = itemView.findViewById(R.id.task_tag_list_item_color);
        TextView nameTextView = itemView.findViewById(R.id.task_tag_list_item_name);
        TextView openTaskCountTextView = itemView.findViewById(R.id.task_tag_list_item_open);
        TextView closedTaskCountTextView = itemView.findViewById(R.id.task_tag_list_item_closed);

        ApplicationLogic applicationLogic = new ApplicationLogic(activity);

        TaskTag tag = getItem(position);
        String color = tag.getColor();
        String name = tag.getName();

        int openTaskCount = applicationLogic.getTaskCount(false, tag);
        int closedTaskCount = applicationLogic.getTaskCount(true, tag);

        Resources resources = activity.getResources();

        String openTaskCountText = resources.getQuantityString(R.plurals.on_open_task_count, openTaskCount, openTaskCount);
        String closedTaskCountText = resources.getQuantityString(R.plurals.on_closed_task_count, closedTaskCount, closedTaskCount);

        // Show color, name and task counts.
        if (color != null && !(color.trim()).equals("")) {
            colorView.setVisibility(View.VISIBLE);

            // "Color.parseColor" converts the hexadecimal color to int-color
            colorView.setBackgroundColor(Color.parseColor(color));
        }

        nameTextView.setText(name);
        openTaskCountTextView.setText(openTaskCountText);
        closedTaskCountTextView.setText(closedTaskCountText);

        return itemView;
    }
}
