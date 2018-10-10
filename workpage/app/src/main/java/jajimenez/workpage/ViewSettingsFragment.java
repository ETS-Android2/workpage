package jajimenez.workpage;

import android.app.Activity;
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

public class ViewSettingsFragment extends PreferenceFragment {
    private Activity activity;
    private PreferenceGroup stateFilterPref;
    private ListPreference statePref;
    private PreferenceGroup tagFilterPref;
    private CheckBoxPreference allPref;

    private TaskContext currentContext;
    private List<TaskTag> tags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences
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

    @Override
    public void onDestroy() {
        super.onDestroy();

        ApplicationLogic logic = new ApplicationLogic(getActivity());
        logic.notifyDataChange();
    }

    private void addStatePreference() {
        statePref = new ListPreference(activity);

        statePref.setKey(ApplicationLogic.VIEW_STATE_FILTER_KEY_START + currentContext.getId());
        statePref.setEntries(R.array.view_state_filter_texts);
        statePref.setEntryValues(R.array.view_state_filter_keys);
        statePref.setDefaultValue("open");
        statePref.setTitle(getInitialStatePreferenceTitle());
        statePref.setDialogTitle(R.string.state_2);
        statePref.setOrder(0);

        statePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                CharSequence text = ViewSettingsFragment.this.getStatePreferenceValue(String.valueOf(newValue));
                statePref.setTitle(text);

                return true;
            }
        });

        stateFilterPref.addPreference(statePref);
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

    private void addNoTagPreference() {
        CheckBoxPreference p = new CheckBoxPreference(activity);
        p.setKey(ApplicationLogic.VIEW_TAG_FILTER_NO_TAG_KEY_START + currentContext.getId());
        p.setDefaultValue(true);
        p.setTitle(R.string.without_tags);
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
        int tagCount = tags.size();

        for (int i = 0; i < tagCount; i++) {
            TaskTag t = tags.get(i);
            CheckBoxPreference p = new CheckBoxPreference(activity);

            p.setKey(ApplicationLogic.VIEW_TAG_FILTER_KEY_START + t.getId());
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
        int tagPrefCount = tags.size() + 2;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) tagFilterPref.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
