package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.io.File;

public class FileAdapter extends ArrayAdapter<File> {
    private Activity activity;
    private int resource;

    public FileAdapter(Activity activity, int resource, List<File> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup) {
        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        TextView fileNameTextView = (TextView) itemView.findViewById(R.id.fileListItem_name);

        File file = getItem(position);
        String fileName = file.getName();

        // Show file name.
        fileNameTextView.setText(fileName);

        return itemView;
    }
}
