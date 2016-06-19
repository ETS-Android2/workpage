package jajimenez.workpage;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class ViewActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        (getActionBar()).setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_OK);

        // Display preference fragment.
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        (transaction.replace(android.R.id.content, new ViewSettingsFragment())).commit();
    }
}
