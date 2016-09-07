package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.preference.ListPreference;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class CsvSettingsFragment extends PreferenceFragment {
    private ListPreference taskContextPref;
    private List<TaskContext> contexts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences.
        addPreferencesFromResource(R.xml.csv_preferences);

        Activity activity = getActivity();
        ApplicationLogic logic = new ApplicationLogic(activity);
        contexts = logic.getAllTaskContexts();

        taskContextPref = (ListPreference) findPreference("csv_task_context_to_export");
        addContexts();
    }

    private void addContexts() {
        String value = taskContextPref.getValue();
        boolean validValue = (value != null && !value.isEmpty());

        int contextCount = contexts.size();

        String[] entryValues = new String[contextCount];
        String[] entries = new String[contextCount];

        boolean contextFound = false;

        for (int i = 0; i < contextCount; i++) {
            TaskContext c = contexts.get(i);

            String name = c.getName();
            long id = c.getId();

            entryValues[i] = String.valueOf(id);
            entries[i] = name;

            if (validValue && !contextFound) contextFound = value.equals(String.valueOf(id));
        }

        taskContextPref.setEntryValues(entryValues);
        taskContextPref.setEntries(entries);

        if (!contextFound) {
            long firstContextId = (contexts.get(0)).getId();
            taskContextPref.setValue(String.valueOf(firstContextId));
        }
    }
}
