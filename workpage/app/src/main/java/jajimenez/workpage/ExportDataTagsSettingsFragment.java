package jajimenez.workpage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataTagsSettingsFragment extends PreferenceFragment {
    private Activity activity;
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

        allPref = (CheckBoxPreference) findPreference("export_data_all");

        allPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int tagPrefCount = ExportDataTagsSettingsFragment.this.tags.size() + 2;

                for (int i = 1; i < tagPrefCount; i++) {
                    CheckBoxPreference p = (CheckBoxPreference) (ExportDataTagsSettingsFragment.this.getPreferenceScreen()).getPreference(i);
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

        (getPreferenceScreen()).addPreference(p);
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

            (getPreferenceScreen()).addPreference(p);
        }
    }

    private void updateAllPref(Preference changedPref, boolean newValue) {
        PreferenceScreen prefScreen = getPreferenceScreen();

        boolean checked = true;
        int tagPrefCount = tags.size() + 2;

        for (int i = 1; i < tagPrefCount && checked; i++) {
            CheckBoxPreference p = (CheckBoxPreference) prefScreen.getPreference(i);

            if (changedPref == null || p != changedPref) checked = p.isChecked();
            else checked = newValue;
        }

        allPref.setChecked(checked);
    }
}
