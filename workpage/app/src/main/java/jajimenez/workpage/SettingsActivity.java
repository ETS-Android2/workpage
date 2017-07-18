package jajimenez.workpage;

import android.support.v7.app.AppCompatActivity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //(getActionBar()).setDisplayHomeAsUpEnabled(true);

        // Display preference fragment.
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        (transaction.replace(android.R.id.content, new SettingsFragment())).commit();
    }
}