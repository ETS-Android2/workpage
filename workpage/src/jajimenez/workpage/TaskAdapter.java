package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.Calendar;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.GradientDrawable;

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

        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        titleTextView = (TextView) itemView.findViewById(R.id.taskListItem_title);
        details1TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_1);
        details2TextView = (TextView) itemView.findViewById(R.id.taskListItem_details_2);

        Task task = getItem(position);

        String title = task.getTitle();
        Calendar start = task.getStart();
        Calendar deadline = task.getDeadline();
        List<TaskTag> tags = task.getTags();

        // Show title.
        titleTextView.setText(title);

        // Show Tags and Dates.
        // Set background color based on tag colors.
        int tagCount;
        if (tags == null) tagCount = 0;
        else tagCount = tags.size();

        String tagsText = "";
        String datesText = (new DateTimeTool()).getTaskDatesText(activity, start, deadline);

        if (tagCount > 0) {
            LinkedList<String> colors = new LinkedList<String>();

            for (int i = 0; i < tagCount; i++) {
                // Tag name.
                TaskTag tag = tags.get(i);

                tagsText += tag.getName();
                if (i < (tagCount - 1)) tagsText += ", ";

                // Tag color.
                //String color = tag.getColor();
                //if (color != null) colors.add(color);
            }

            // Task dates.
            details1TextView.setText(tagsText);
            details2TextView.setText(datesText);

            // Task color.
            //int colorCount = colors.size();
            //int[] intColors = new int[colorCount];

            // "Color.parseColor" converts the hexadecimal color to int-color.
            //for (int i = 0; i < colorCount; i++) intColors[i] = Color.parseColor(colors.get(i));

            /*Drawable drawable = null;

            if (colorCount == 1) drawable = new ColorDrawable(intColors[0]);
            else if (colorCount >= 2) drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intColors);

            if (drawable != null) {
                drawable.setAlpha(64);
                itemView.setBackground(drawable);
            }*/
        }
        else {
            details1TextView.setText(datesText);
        }

        return itemView;
    }
}
