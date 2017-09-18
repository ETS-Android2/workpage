package jajimenez.workpage;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

import jajimenez.workpage.logic.ApplicationLogic;

public class ViewActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_OK);

        ApplicationLogic logic = new ApplicationLogic(this);
        ActionBar bar = getSupportActionBar();
        bar.setSubtitle((logic.getCurrentTaskContext()).getName());

        // Display preference fragment.
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        (transaction.replace(android.R.id.content, new ViewSettingsFragment())).commit();
    }
}