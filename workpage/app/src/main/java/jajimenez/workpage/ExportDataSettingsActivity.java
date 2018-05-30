package jajimenez.workpage;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataSettingsActivity extends AppCompatActivity {
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_data_settings);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setSubtitle(R.string.select_contexts);

        list = findViewById(R.id.export_data_settings_list);
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CheckBox check = view.findViewById(R.id.export_data_settings_context_item_check);
                check.toggle();
            }
        });

        updateInterface();
    }

    private void updateInterface() {
        ApplicationLogic logic = new ApplicationLogic(this);
        List<TaskContext> contexts = logic.getAllTaskContexts();

        ExportDataSettingsContextAdapter adapter = new ExportDataSettingsContextAdapter(this,
                R.layout.export_data_settings_context_item,
                contexts);

        list.setAdapter(adapter);
    }
}
