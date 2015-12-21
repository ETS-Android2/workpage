package jajimenez.workpage;

import android.preference.PreferenceFragment;
import android.os.Bundle;

public class CsvSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences.
        addPreferencesFromResource(R.xml.csv_preferences);
    }
}
