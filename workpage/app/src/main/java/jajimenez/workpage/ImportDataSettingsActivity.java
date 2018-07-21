package jajimenez.workpage;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.JsonDataTool;
import jajimenez.workpage.data.model.Task;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class ImportDataSettingsActivity extends AppCompatActivity {
    private ListView list;

    JSONObject data;
    private List<Pair<TaskContext, List<TaskTag>>> contexts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_data_settings);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setSubtitle(R.string.select_contexts);

        ApplicationLogic logic = new ApplicationLogic(this);

        try {
            data = new JSONObject(logic.getDataToImport());
            contexts = logic.getContextsFromJson(data);
        } catch (JSONException e) {
            data = new JSONObject();
            contexts = new LinkedList<>();
        }

        list = findViewById(R.id.import_data_settings_list);
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CheckBox check = view.findViewById(R.id.import_data_settings_context_item_check);
                check.toggle();
            }
        });

        updateInterface();
    }

    private void updateInterface() {
        int contextCount = contexts.size();
        List<ImportDataSettingsContextAdapter.TaskContextItem> items = new ArrayList<>(contextCount);

        JsonDataTool tool = new JsonDataTool();

        for (Pair<TaskContext, List<TaskTag>> p: contexts) {
            TaskContext context = p.first;
            List<TaskTag> tags = p.second;

            List<Task> tasks = tool.getContextTasks(data, context);

            int openTaskCount = getTaskCount(context, tasks, false);
            int closedTaskCount = getTaskCount(context, tasks, true);

            items.add(new ImportDataSettingsContextAdapter.TaskContextItem(context, tags, openTaskCount, closedTaskCount));
        }

        ImportDataSettingsContextAdapter adapter = new ImportDataSettingsContextAdapter(this,
                R.layout.import_data_settings_context_item,
                items);

        list.setAdapter(adapter);
    }

    private int getTaskCount(TaskContext context, List<Task> contextTasks, boolean done) {
        int count = 0;

        List<TaskContext> contexts = (new JsonDataTool()).getContexts(data);
        int contextCount = contexts.size();
        boolean found = false;

        for (int i = 0; i < contextCount && !found; i++) {
            TaskContext c = contexts.get(i);

            if (c.equals(context)) {
                for (Task t: contextTasks) {
                    if (t.isDone() == done) count++;
                }

                found = true;
            }
        }

        return count;
    }

    public void onImportClicked(View view) {
        // Import data
        (new ImportDataTask()).execute();
    }

    private class ImportDataTask extends AsyncTask<Void, Void, Boolean> {
        protected void onPreExecute() {
            // Nothing to do
        }

        protected Boolean doInBackground(Void... parameters) {
            boolean success = true;

            ApplicationLogic logic = new ApplicationLogic(ImportDataSettingsActivity.this, false);

            try {
                logic.importData(ImportDataSettingsActivity.this.data);
            } catch (Exception e) {
                success = false;
            }

            logic.setNotifyDataChanges(true);
            logic.notifyDataChange();

            return success;
        }

        protected void onPostExecute(Boolean success) {
            if (success) {
                ApplicationLogic logic = new ApplicationLogic(ImportDataSettingsActivity.this);
                logic.clearImportPreferences();

                (Toast.makeText(ImportDataSettingsActivity.this, R.string.data_import_success, Toast.LENGTH_SHORT)).show();
                ImportDataSettingsActivity.this.finish();
            } else {
                (Toast.makeText(ImportDataSettingsActivity.this, R.string.data_import_error, Toast.LENGTH_SHORT)).show();
            }
        }
    }
}
