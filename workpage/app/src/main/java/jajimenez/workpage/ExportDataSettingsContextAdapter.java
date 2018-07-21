package jajimenez.workpage;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataSettingsContextAdapter extends ArrayAdapter<TaskContext> {
    private Activity activity;
    private int resource;

    public ExportDataSettingsContextAdapter(Activity activity, int resource, List<TaskContext> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup)  {
        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        final TaskContext context = getItem(position);

        CheckBox contextCheckBox = itemView.findViewById(R.id.export_data_settings_context_item_check);
        TextView contextNameTextView = itemView.findViewById(R.id.export_data_settings_context_item_name);
        TextView openTaskCountTextView = itemView.findViewById(R.id.export_data_settings_context_item_open);
        TextView closedTaskCountTextView = itemView.findViewById(R.id.export_data_settings_context_item_closed);
        final Button tagsButton = itemView.findViewById(R.id.export_data_settings_context_item_tags);

        Resources resources = activity.getResources();
        ApplicationLogic logic = new ApplicationLogic(activity);

        contextCheckBox.setChecked(logic.isContextToExport(context));
        contextCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ApplicationLogic logic = new ApplicationLogic(ExportDataSettingsContextAdapter.this.activity);
                logic.setContextToExport(context, b);

                tagsButton.setEnabled(b);
            }
        });

        contextNameTextView.setText(context.getName());

        int openTaskCount = logic.getTaskCount(false, context);
        String openTaskCountText = resources.getQuantityString(R.plurals.open_task_count, openTaskCount, openTaskCount);
        openTaskCountTextView.setText(openTaskCountText);

        int closedTaskCount = logic.getTaskCount(true, context);
        String closedTaskCountText = resources.getQuantityString(R.plurals.closed_task_count, closedTaskCount, closedTaskCount);
        closedTaskCountTextView.setText(closedTaskCountText);

        tagsButton.setEnabled(contextCheckBox.isChecked());
        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ExportDataSettingsContextAdapter.this.activity, ExportDataTagsActivity.class);
                intent.putExtra("task_context_id", context.getId());
                ExportDataSettingsContextAdapter.this.activity.startActivity(intent);
            }
        });

        return itemView;
    }
}
