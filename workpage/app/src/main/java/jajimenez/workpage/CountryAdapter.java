package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jajimenez.workpage.data.model.Country;

public class CountryAdapter extends ArrayAdapter<Country> {
    private Activity activity;
    private int resource;

    public CountryAdapter(Activity activity, int resource, List<Country> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        Country country = getItem(position);

        TextView countryTextView = (TextView) itemView;
        countryTextView.setText(country.getName());

        return itemView;
    }
}
