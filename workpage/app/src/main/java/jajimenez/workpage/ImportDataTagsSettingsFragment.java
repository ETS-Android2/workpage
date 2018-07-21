package jajimenez.workpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.logic.ApplicationLogic;

public class ImportDataTagsSettingsFragment extends PreferenceFragment {
    private Activity activity;

    private PreferenceGroup statePrefGroup;
    private ListPreference statePref;
    private PreferenceGroup tagPrefGroup;
    private CheckBoxPreference allPref;

    private long contextId;
    private long[] tagIds;
    private String[] tagNames;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences
        addPreferencesFromResource(R.xml.import_data_preferences);

        activity = getActivity();

        Intent intent = activity.getIntent();
        contextId = intent.getLongExtra("task_context_id", -1);
        tagNames = intent.getStringArrayExtra("task_tag_names");
        tagIds = intent.getLongArrayExtra("task_tag_ids");

        statePrefGroup = (PreferenceGroup) findPreference("import_data_state");
        tagPrefGroup = (PreferenceGroup) findPreference("import_data_tags");

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

        statePref.setKey(ApplicationLogic.IMPORT_DATA_TASK_STATE_KEY_START + contextId);
        statePref.setEntries(R.array.export_data_state_texts);
        statePref.setEntryValues(R.array.export_data_state_keys);
        statePref.setDefaultValue("all");
        statePref.setTitle(getInitialStatePreferenceTitle());
        statePref.setDialogTitle(R.string.state_2);
        statePref.setOrder(0);

        statePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CharSequence text = ImportDataTagsSettingsFragment.this.getStatePreferenceValue(String.valueOf(newValue));
                statePref.setTitle(text);

                return true;
            }
        });

        statePrefGroup.addPreference(statePref);
    }

    private CharSequence getInitialStatePreferenceTitle() {
        ApplicationLogic logic = new ApplicationLogic(activity);

        TaskContext c = new TaskContext();
        c.setId(contextId);
        int state = logic.getTaskStateToImport(c);

        return (statePref.getEntries())[state];
    }

    private CharSequence getStatePreferenceValue(String key) {
        int index = statePref.findIndexOfValue(key);
        return (statePref.getEntries())[index];
    }

    private void addAllPreference() {
        allPref = (CheckBoxPreference) findPreference("import_data_all");

        allPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tagPrefCount = ImportDataTagsSettingsFragment.this.tagNames.length + 2;

                for (int i = 1; i < tagPrefCount; i++) {
                    CheckBoxPreference p = (CheckBoxPreference) ImportDataTagsSettingsFragment.this.tagPrefGroup.getPreference(i);
                    p.setChecked((Boolean) newValue);
                }

                return true;
            }
        });
    }

    private void addNoTagPreference() {
        CheckBoxPreference p = new CheckBoxPreference(activity);

        p.setKey(ApplicationLogic.IMPORT_DATA_NOTAG_CONTEXT_KEY_START + contextId);
        p.setDefaultValue(true);
        p.setTitle(R.string.without_tags);
        p.setOrder(1);

        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ImportDataTagsSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);

                return true;
            }
        });

        tagPrefGroup.addPreference(p);
    }

    private void addTagPreferences() {
        for (int i = 0; i < tagIds.length; i++) {
            CheckBoxPreference p = new CheckBoxPreference(activity);

            p.setKey(ApplicationLogic.IMPORT_DATA_TAG_KEY_START + tagIds[i]);
            p.setDefaultValue(true);
            p.setTitle(tagNames[i]);
            p.setOrder(i + 2);

            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ImportDataTagsSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);
                    return true;
                }
            });

            tagPrefGroup.addPreference(p);
        }
    }

    private void updateAllPref(Preference changedPref, boolean newValue) {
        boolean checked = true;
        int tagPrefCount = tagNames.length + 2;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) tagPrefGroup.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
