package jajimenez.workpage;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class MainActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    public void onAboutItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onSwitchTaskContextSelected(MenuItem item) {
        final ApplicationLogic applicationLogic = new ApplicationLogic(this);

        TaskContext currentTaskContext = applicationLogic.getCurrentTaskContext();
        long currentTaskContextId = currentTaskContext.getId();

        int selectedItem = -1;

        final List<TaskContext> taskContexts = applicationLogic.getAllTaskContexts();
        int taskContextCount = taskContexts.size();
        String[] taskContextNames = new String[taskContextCount];
        TaskContext t = null;

        for (int i = 0; i < taskContextCount; i++) {
            t = taskContexts.get(i);
            taskContextNames[i] = t.getName();

            if (t.getId() == currentTaskContextId) selectedItem = i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.switch_task_context);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(taskContextNames, selectedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                applicationLogic.setCurrentTaskContext(taskContexts.get(id));
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
