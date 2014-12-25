package jajimenez.workpage;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.TextView;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class ViewActivity extends Activity {
    private RadioButton viewOpenRadioButton;
    private RadioButton viewNowRadioButton;
    private RadioButton viewClosedRadioButton;
    private LinearLayout tagsLinearLayout;
    private TextView noTagsTextView;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;

    private String currentView;
    private List<TaskTag> currentFilterTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

        viewOpenRadioButton = (RadioButton) findViewById(R.id.view_open);
        viewNowRadioButton = (RadioButton) findViewById(R.id.view_now);
        viewClosedRadioButton = (RadioButton) findViewById(R.id.view_closed);
        tagsLinearLayout = (LinearLayout) findViewById(R.id.view_tags);
        noTagsTextView = (TextView) findViewById(R.id.view_noTags);

        (getActionBar()).setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_OK);

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        currentView = applicationLogic.getCurrentView();
        currentFilterTags = applicationLogic.getCurrentFilterTags();

        updateInterface();
    }

    private void updateInterface() {
        if (currentView.equals("open")) viewOpenRadioButton.setChecked(true);
        else if (currentView.equals("now")) viewNowRadioButton.setChecked(true);
        else if (currentView.equals("closed")) viewClosedRadioButton.setChecked(true);

        List<TaskTag> tags = applicationLogic.getAllTaskTags(currentTaskContext);
        CheckBox tagCheckBox;

        if (tags.size() > 0) {
            for (final TaskTag tag : tags) {
                tagCheckBox = new CheckBox(this);
                tagCheckBox.setText(tag.getName());
                tagCheckBox.setChecked(currentFilterTags.contains(tag));
                tagCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CheckBox tagCheckBox = (CheckBox) v;

                        if (tagCheckBox.isChecked()) ViewActivity.this.currentFilterTags.add(tag);
                        else ViewActivity.this.currentFilterTags.remove(tag);

                        ViewActivity.this.applicationLogic.setCurrentFilterTags(ViewActivity.this.currentFilterTags);
                    }
                });

                tagsLinearLayout.addView(tagCheckBox);
            }
        }
        else {
            noTagsTextView.setVisibility(View.VISIBLE);
        }
    }

    public void onViewRadioButtonClicked(View view) {
        if (viewOpenRadioButton.isChecked()) applicationLogic.setCurrentView("open"); 
        else if (viewNowRadioButton.isChecked()) applicationLogic.setCurrentView("now"); 
        else if (viewClosedRadioButton.isChecked()) applicationLogic.setCurrentView("closed"); 
    }
}
