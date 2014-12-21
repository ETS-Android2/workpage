package jajimenez.workpage;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RadioButton;

import jajimenez.workpage.logic.ApplicationLogic;

public class ViewActivity extends Activity {
    private RadioButton viewOpenRadioButton;
    private RadioButton viewNowRadioButton;
    private RadioButton viewClosedRadioButton;

    private ApplicationLogic applicationLogic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

        viewOpenRadioButton = (RadioButton) findViewById(R.id.view_open);
        viewNowRadioButton = (RadioButton) findViewById(R.id.view_now);
        viewClosedRadioButton = (RadioButton) findViewById(R.id.view_closed);

        applicationLogic = new ApplicationLogic(this);

        (getActionBar()).setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_OK);

        updateInterface();
    }

    private void updateInterface() {
        String currentView = applicationLogic.getCurrentView();

        if (currentView.equals("open")) viewOpenRadioButton.setChecked(true);
        else if (currentView.equals("now")) viewNowRadioButton.setChecked(true);
        else if (currentView.equals("closed")) viewClosedRadioButton.setChecked(true);
    }

    public void onViewRadioButtonClicked(View view) {
        if (viewOpenRadioButton.isChecked()) applicationLogic.setCurrentView("open"); 
        else if (viewNowRadioButton.isChecked()) applicationLogic.setCurrentView("now"); 
        else if (viewClosedRadioButton.isChecked()) applicationLogic.setCurrentView("closed"); 
    }
}
