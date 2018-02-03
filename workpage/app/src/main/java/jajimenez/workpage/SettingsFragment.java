package jajimenez.workpage;

import android.preference.PreferenceFragment;
import android.os.Bundle;

import jajimenez.workpage.logic.ApplicationLogic;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ApplicationLogic logic = new ApplicationLogic(getActivity());
        logic.notifyDataChange();
    }
}
