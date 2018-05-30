package jajimenez.workpage;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

public class ExportDataTagsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ApplicationLogic logic = new ApplicationLogic(this);
        // ActionBar bar = getSupportActionBar();
        // if (bar != null) bar.setSubtitle((logic.getCurrentTaskContext()).getName());

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setSubtitle(R.string.select_tags);

        // Display preference fragment
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        (transaction.replace(android.R.id.content, new ExportDataTagsSettingsFragment())).commit();
    }
}
