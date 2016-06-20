package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.ListPreference;
import android.preference.CheckBoxPreference;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class ViewSettingsFragment extends PreferenceFragment {
    private Activity activity;
    private PreferenceGroup stateFilterPref;
    private PreferenceGroup tagFilterPref;
    private CheckBoxPreference allPref;

    private TaskContext currentContext;
    private List<TaskTag> tags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences.
        addPreferencesFromResource(R.xml.view_preferences);

        activity = getActivity();
        ApplicationLogic logic = new ApplicationLogic(activity);

        currentContext = logic.getCurrentTaskContext();
        tags = logic.getAllTaskTags(currentContext);

        stateFilterPref = (PreferenceGroup) findPreference("view_state_filter");
        addStatePreference();

        tagFilterPref = (PreferenceGroup) findPreference("view_tag_filter");
        allPref = (CheckBoxPreference) findPreference("view_tag_filter_all");

        allPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tagPrefCount = ViewSettingsFragment.this.tags.size() + 2;

                for (int i = 1; i < tagPrefCount; i++) {
                    CheckBoxPreference p = (CheckBoxPreference) ViewSettingsFragment.this.tagFilterPref.getPreference(i);
                    p.setChecked((Boolean) newValue);
                }

                return true;
            }
        });

        addNoTagPreference();
        addTagPreferences();

        updateAllPref(null, false);
    }

    private void addStatePreference() {
        ListPreference p = new ListPreference(activity);

        p.setKey("view_state_filter_state_context_" + currentContext.getId());
        p.setEntries(R.array.view_state_filter_texts);
        p.setEntryValues(R.array.view_state_filter_keys);
        p.setDefaultValue("open");
        p.setTitle(R.string.state_1);
        p.setSummary("%s");
        p.setOrder(0);

        stateFilterPref.addPreference(p);
    }

    private void addNoTagPreference() {
        CheckBoxPreference p = new CheckBoxPreference(activity);
        p.setKey("view_tag_filter_notag_context_" + currentContext.getId());
        p.setDefaultValue(true);
        p.setTitle(R.string.no_tag);
        p.setOrder(1);

        p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ViewSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);

                return true;
            }
        });

        tagFilterPref.addPreference(p);
    }

    private void addTagPreferences() {
        int tagCount = ViewSettingsFragment.this.tags.size();

        for (int i = 0; i < tagCount; i++) {
            TaskTag t = tags.get(i);
            CheckBoxPreference p = new CheckBoxPreference(activity);

            p.setKey("view_tag_filter_tag_" + t.getId());
            p.setDefaultValue(true);
            p.setTitle(t.getName());
            p.setOrder(i + 2);

            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ViewSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);

                    return true;
                }
            });

            tagFilterPref.addPreference(p);
        }
    }

    private void updateAllPref(Preference changedPref, boolean newValue) {
        boolean checked = true;
        int tagPrefCount = ViewSettingsFragment.this.tags.size() + 1;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) tagFilterPref.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else if (p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
