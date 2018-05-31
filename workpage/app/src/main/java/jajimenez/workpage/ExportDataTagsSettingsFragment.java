package jajimenez.workpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataTagsSettingsFragment extends PreferenceFragment {
    private Activity activity;

    private PreferenceGroup statePrefGroup;
    private ListPreference statePref;
    private PreferenceGroup tagPrefGroup;
    private CheckBoxPreference allPref;

    private TaskContext context;
    private List<TaskTag> tags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences
        addPreferencesFromResource(R.xml.export_data_preferences);

        activity = getActivity();
        ApplicationLogic logic = new ApplicationLogic(activity);

        Intent intent = activity.getIntent();
        long contextId = intent.getLongExtra("task_context_id", -1);
        context = logic.getTaskContext(contextId);

        tags = logic.getAllTaskTags(context);

        statePrefGroup = (PreferenceGroup) findPreference("export_data_state");
        tagPrefGroup = (PreferenceGroup) findPreference("export_data_tags");

        addStatePreference();
        addAllPreference();
        addNoTagPreference();
        addTagPreferences();
        updateAllPref(null, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ApplicationLogic logic = new ApplicationLogic(getActivity());
        logic.notifyDataChange();
    }

    private void addStatePreference() {
        statePref = new ListPreference(activity);

        statePref.setKey("export_data_state_context_" + context.getId());
        statePref.setEntries(R.array.export_data_state_texts);
        statePref.setEntryValues(R.array.export_data_state_keys);
        statePref.setDefaultValue("all");
        statePref.setTitle(getInitialStatePreferenceTitle());
        statePref.setDialogTitle(R.string.state_2);
        statePref.setOrder(0);

        statePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CharSequence text = ExportDataTagsSettingsFragment.this.getStatePreferenceValue(String.valueOf(newValue));
                statePref.setTitle(text);

                return true;
            }
        });

        statePrefGroup.addPreference(statePref);
    }

    private CharSequence getInitialStatePreferenceTitle() {
        ApplicationLogic logic = new ApplicationLogic(activity);
        String currentStateKey = logic.getViewStateFilter();

        return getStatePreferenceValue(currentStateKey);
    }

    private CharSequence getStatePreferenceValue(String key) {
        int index = statePref.findIndexOfValue(key);
        return (statePref.getEntries())[index];
    }

    private void addAllPreference() {
        allPref = (CheckBoxPreference) findPreference("export_data_all");

        allPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tagPrefCount = ExportDataTagsSettingsFragment.this.tags.size() + 2;

                for (int i = 1; i < tagPrefCount; i++) {
                    CheckBoxPreference p = (CheckBoxPreference) ExportDataTagsSettingsFragment.this.tagPrefGroup.getPreference(i);
                    p.setChecked((Boolean) newValue);
                }

                return true;
            }
        });
    }

    private void addNoTagPreference() {
        CheckBoxPreference p = new CheckBoxPreference(activity);
        p.setKey("export_data_notag_context_" + context.getId());
        p.setDefaultValue(true);
        p.setTitle(R.string.without_tags);
        p.setOrder(1);

        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ExportDataTagsSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);

                return true;
            }
        });

        tagPrefGroup.addPreference(p);
    }

    private void addTagPreferences() {
        int tagCount = tags.size();

        for (int i = 0; i < tagCount; i++) {
            TaskTag t = tags.get(i);
            CheckBoxPreference p = new CheckBoxPreference(activity);

            p.setKey("export_data_tag_" + t.getId());
            p.setDefaultValue(true);
            p.setTitle(t.getName());
            p.setOrder(i + 2);

            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ExportDataTagsSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);
                    return true;
                }
            });

            tagPrefGroup.addPreference(p);
        }
    }

    private void updateAllPref(Preference changedPref, boolean newValue) {
        boolean checked = true;
        int tagPrefCount = tags.size() + 2;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) tagPrefGroup.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
