package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.CheckBoxPreference;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class ViewSettingsFragment extends PreferenceFragment {
    private PreferenceGroup tagsPref;
    private CheckBoxPreference allPref;
    private CheckBoxPreference noTagPref;

    private List<TaskTag> tags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences.
        addPreferencesFromResource(R.xml.view_preferences);

        ApplicationLogic logic = new ApplicationLogic(getActivity());
        tags = logic.getAllTaskTags(logic.getCurrentTaskContext());

        tagsPref = (PreferenceGroup) findPreference("view_tag_filter");
        allPref = (CheckBoxPreference) findPreference("view_tag_filter_all");

        allPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tagPrefCount = ViewSettingsFragment.this.tags.size() + 1;

                for (int i = 1; i < tagPrefCount; i++) {
                    CheckBoxPreference p = (CheckBoxPreference) ViewSettingsFragment.this.tagsPref.getPreference(i);
                    p.setChecked((Boolean) newValue);
                }

                return true;
            }
        });

        noTagPref = (CheckBoxPreference) findPreference("view_tag_filter_notag");

        noTagPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ViewSettingsFragment.this.updateAllPref(preference, (Boolean) newValue);

                return true;
            }
        });

        addTagPreferences();
        updateAllPref(null, false);
    }

    private void addTagPreferences() {
        int tagCount = ViewSettingsFragment.this.tags.size();

        for (int i = 0; i < tagCount; i++) {
            TaskTag t = tags.get(i);
            CheckBoxPreference p = new CheckBoxPreference(getActivity());

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

            tagsPref.addPreference(p);
        }
    }

    private void updateAllPref(Preference changedPref, boolean newValue) {
        boolean checked = true;
        int tagPrefCount = ViewSettingsFragment.this.tags.size() + 1;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) tagsPref.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else if (p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
