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
import jajimenez.workpage.data.model.TaskContext;

public class TaskContextAdapter extends ArrayAdapter<TaskContext> {
    private Activity activity;
    private int resource;

    public TaskContextAdapter(Activity activity, int resource, List<TaskContext> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup)  {
        TextView nameTextView;
        TextView openTaskCountTextView;
        TextView closedTaskCountTextView;

        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        nameTextView = itemView.findViewById(R.id.task_context_list_item_name);
        openTaskCountTextView = itemView.findViewById(R.id.task_context_list_item_open);
        closedTaskCountTextView = itemView.findViewById(R.id.task_context_list_item_closed);

        ApplicationLogic applicationLogic = new ApplicationLogic(activity);

        TaskContext context = getItem(position);
        String name = context.getName();

        int openTaskCount = applicationLogic.getTaskCount(false, context);
        int closedTaskCount = applicationLogic.getTaskCount(true, context);

        Resources resources = activity.getResources();

        String openTaskCountText = resources.getQuantityString(R.plurals.open_task_count, openTaskCount, openTaskCount);
        String closedTaskCountText = resources.getQuantityString(R.plurals.closed_task_count, closedTaskCount, closedTaskCount);

        nameTextView.setText(name);
        openTaskCountTextView.setText(openTaskCountText);
        closedTaskCountTextView.setText(closedTaskCountText);

        return itemView;
    }
}
