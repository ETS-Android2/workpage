package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.LinearLayout;
import android.widget.CheckBox;
import android.widget.TextView;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class ViewActivity extends Activity {
    private RadioButton openRadioButton;
    private RadioButton doableTodayRadioButton;
    private RadioButton closedRadioButton;
    private LinearLayout tagsLinearLayout;
    private CheckBox allCheckBox;
    private CheckBox noTagCheckBox;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;

    private List<TaskTag> contextTags;

    private String currentView;
    private boolean includeTasksWithNoTag;
    private List<TaskTag> currentFilterTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);

        openRadioButton = (RadioButton) findViewById(R.id.view_open);
        doableTodayRadioButton = (RadioButton) findViewById(R.id.view_doableToday);
        closedRadioButton = (RadioButton) findViewById(R.id.view_closed);
        allCheckBox = (CheckBox) findViewById(R.id.view_all);
        noTagCheckBox = (CheckBox) findViewById(R.id.view_noTag);
        tagsLinearLayout = (LinearLayout) findViewById(R.id.view_tags);

        (getActionBar()).setDisplayHomeAsUpEnabled(true);
        setResult(RESULT_OK);

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        currentView = applicationLogic.getCurrentView();
        includeTasksWithNoTag = applicationLogic.getIncludeTasksWithNoTag();
        currentFilterTags = applicationLogic.getCurrentFilterTags();

        updateInterface();

        openRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) applicationLogic.setCurrentView("open");
            }
        });

        doableTodayRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) applicationLogic.setCurrentView("doable_today");
            }
        });

        closedRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) applicationLogic.setCurrentView("closed");
            }
        });
    }

    private void updateInterface() {
        if (currentView.equals("open")) openRadioButton.setChecked(true);
        else if (currentView.equals("doable_today")) doableTodayRadioButton.setChecked(true);
        else if (currentView.equals("closed")) closedRadioButton.setChecked(true);

        contextTags = applicationLogic.getAllTaskTags(currentTaskContext);
        CheckBox tagCheckBox = null;
        final int tagCount = contextTags.size();

        if (tagCount > 0) {
            for (final TaskTag tag : contextTags) {
                tagCheckBox = new CheckBox(this);
                tagCheckBox.setText(tag.getName());
                tagCheckBox.setChecked(currentFilterTags.contains(tag));
                tagCheckBox.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox tagCheckBox = (CheckBox) v;

                        if (tagCheckBox.isChecked()) ViewActivity.this.currentFilterTags.add(tag);
                        else ViewActivity.this.currentFilterTags.remove(tag);

                        ViewActivity.this.allCheckBox.setChecked(ViewActivity.this.includeTasksWithNoTag &&
                            ViewActivity.this.currentFilterTags.size() == tagCount);

                        // Save settings.
                        ViewActivity.this.applicationLogic.setCurrentFilterTags(ViewActivity.this.currentFilterTags);
                    }
                });

                tagsLinearLayout.addView(tagCheckBox);
            }
        }

        allCheckBox.setChecked(includeTasksWithNoTag && currentFilterTags.size() == tagCount);
        noTagCheckBox.setChecked(includeTasksWithNoTag);
    }

    public void onOpenDescriptionTextViewClicked(View view) {
        openRadioButton.setChecked(true);
    }

    public void onDoableTodayDescriptionTextViewClicked(View view) {
        doableTodayRadioButton.setChecked(true);
    }

    public void onClosedDescriptionTextViewClicked(View view) {
        closedRadioButton.setChecked(true);
    }

    public void onAllCheckBoxClicked(View view) {
        boolean all = allCheckBox.isChecked();
        int count = tagsLinearLayout.getChildCount();

        for (int i = 1; i < count; i++) {
            CheckBox c = (CheckBox) tagsLinearLayout.getChildAt(i);
            c.setChecked(all);
        }

        if (all) {
            includeTasksWithNoTag = true;
            currentFilterTags = contextTags;
        }
        else {
            includeTasksWithNoTag = false;
            currentFilterTags = new LinkedList<TaskTag>();
        }

        // Save settings.
        applicationLogic.setIncludeTasksWithNoTag(includeTasksWithNoTag);
        applicationLogic.setCurrentFilterTags(currentFilterTags);
    }

    public void onNoTagCheckBoxClicked(View view) {
        includeTasksWithNoTag = noTagCheckBox.isChecked();
        allCheckBox.setChecked(includeTasksWithNoTag && currentFilterTags.size() == contextTags.size());

        // Save settings.
        applicationLogic.setIncludeTasksWithNoTag(includeTasksWithNoTag);
    }
}
