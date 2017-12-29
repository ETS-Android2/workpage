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

import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;

public class TimeZoneAdapter extends ArrayAdapter<TimeZone> {
    private Activity activity;
    private int resource;

    private Calendar date = null;

    public TimeZoneAdapter(Activity activity, int resource, List<TimeZone> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        ApplicationLogic logic = new ApplicationLogic(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        TimeZone timeZone = getItem(position);
        Country country = logic.getCountry(timeZone.getID());

        TextView nameTextView = itemView.findViewById(R.id.time_zone_list_item_name);
        TextView nowTextView = itemView.findViewById(R.id.time_zone_list_item_now);
        ImageView dstImageView = itemView.findViewById(R.id.time_zone_list_item_dst);
        TextView countryTextView = itemView.findViewById(R.id.time_zone_list_item_country);

        TextTool tool = new TextTool();
        Calendar now = Calendar.getInstance(timeZone);

        nameTextView.setText(tool.getTimeZoneName(timeZone, date));
        nowTextView.setText(tool.getTimeZoneInformation(activity, now));

        if (timeZone.inDaylightTime(now.getTime())) dstImageView.setVisibility(View.VISIBLE);
        else dstImageView.setVisibility(View.GONE);

        countryTextView.setText(country.getName());

        return itemView;
    }
}
