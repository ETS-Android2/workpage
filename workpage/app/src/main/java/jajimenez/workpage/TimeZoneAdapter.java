package jajimenez.workpage;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;

public class TimeZoneAdapter extends ArrayAdapter<TimeZone> {
    private Activity activity;
    private int resource;

    public TimeZoneAdapter(Activity activity, int resource, List<TimeZone> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        ApplicationLogic logic = new ApplicationLogic(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        TimeZone timeZone = getItem(position);

        TextView nameTextView = (TextView) itemView.findViewById(R.id.time_zone_list_item_name);
        TextView timeTextView = (TextView) itemView.findViewById(R.id.time_zone_list_item_time);
        TextView offsetTextView = (TextView) itemView.findViewById(R.id.time_zone_list_item_offset);
        ImageView dstImageView = (ImageView) itemView.findViewById(R.id.time_zone_list_item_dst);

        TextTool tool = new TextTool();
        Calendar now = Calendar.getInstance(timeZone);

        nameTextView.setText(tool.getTimeZoneName(timeZone, now));
        timeTextView.setText(tool.getTimeZoneInformation(activity, now));

        offsetTextView.setText(tool.getFormattedTimeZone(activity, timeZone, now, TextTool.LONG));

        if (timeZone.inDaylightTime(now.getTime())) dstImageView.setVisibility(View.VISIBLE);
        else dstImageView.setVisibility(View.GONE);

        return itemView;
    }
}