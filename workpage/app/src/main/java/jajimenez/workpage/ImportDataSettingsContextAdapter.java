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
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.ImportDataSettingsContextAdapter.TaskContextItem;

public class ImportDataSettingsContextAdapter extends ArrayAdapter<TaskContextItem> {
    private Activity activity;
    private int resource;

    public ImportDataSettingsContextAdapter(Activity activity, int resource, List<TaskContextItem> items) {
        super(activity, resource, items);

        this.activity = activity;
        this.resource = resource;
    }

    @Override
    public View getView(int position, View itemView, ViewGroup parentViewGroup)  {
        LayoutInflater inflater = activity.getLayoutInflater();
        itemView = inflater.inflate(resource, null);

        TaskContextItem item = getItem(position);

        final TaskContext context = item.getContext();
        List<TaskTag> tags = item.getTags();
        int openTaskCount = item.getOpenTaskCount();
        int closedTaskCount = item.getClosedTaskCount();

        CheckBox contextCheckBox = itemView.findViewById(R.id.import_data_settings_context_item_check);
        TextView contextNameTextView = itemView.findViewById(R.id.import_data_settings_context_item_name);
        TextView openTaskCountTextView = itemView.findViewById(R.id.import_data_settings_context_item_open);
        TextView closedTaskCountTextView = itemView.findViewById(R.id.import_data_settings_context_item_closed);
        final Button tagsButton = itemView.findViewById(R.id.import_data_settings_context_item_tags);

        Resources resources = activity.getResources();
        ApplicationLogic logic = new ApplicationLogic(activity);

        contextCheckBox.setChecked(logic.isContextToImport(context));
        contextCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                ApplicationLogic logic = new ApplicationLogic(ImportDataSettingsContextAdapter.this.activity);
                logic.setContextToImport(context, b);

                tagsButton.setEnabled(b);
            }
        });

        contextNameTextView.setText(context.getName());

        String openTaskCountText = resources.getQuantityString(R.plurals.open_task_count, openTaskCount, openTaskCount);
        openTaskCountTextView.setText(openTaskCountText);

        String closedTaskCountText = resources.getQuantityString(R.plurals.closed_task_count, closedTaskCount, closedTaskCount);
        closedTaskCountTextView.setText(closedTaskCountText);

        int tagCount = tags.size();
        final long[] tagIds = new long[tagCount];
        final String[] tagNames = new String[tagCount];

        for (int i = 0; i < tagCount; i++) {
            TaskTag t = tags.get(i);

            tagIds[i] = t.getId();
            tagNames[i] = t.getName();
        }

        tagsButton.setEnabled(contextCheckBox.isChecked());
        tagsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImportDataSettingsContextAdapter.this.activity, ImportDataTagsActivity.class);
                intent.putExtra("task_context_id", context.getId());
                intent.putExtra("task_tag_ids", tagIds);
                intent.putExtra("task_tag_names", tagNames);

                ImportDataSettingsContextAdapter.this.activity.startActivity(intent);
            }
        });

        return itemView;
    }

    public static class TaskContextItem {
        private TaskContext context;
        private List<TaskTag> tags;
        private int openTaskCount;
        private int closedTaskCount;

        public TaskContextItem(TaskContext context, List<TaskTag> tags, int openTaskCount, int closedTaskCount) {
            this.context = context;
            this.tags = tags;
            this.openTaskCount = openTaskCount;
            this.closedTaskCount = closedTaskCount;
        }

        public TaskContext getContext() {
            return context;
        }

        public List<TaskTag> getTags() {
            return tags;
        }

        public int getOpenTaskCount() {
            return openTaskCount;
        }

        public int getClosedTaskCount() {
            return closedTaskCount;
        }
    }
}
