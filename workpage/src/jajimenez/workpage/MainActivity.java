package jajimenez.workpage;

import java.util.List;
import java.util.ArrayList;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.content.Intent;
import android.content.DialogInterface;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class MainActivity extends Activity {
    private ApplicationLogic applicationLogic = null;
    private TaskContext currentTaskContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = this.applicationLogic.getCurrentTaskContext();

        updateInterface();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    private void updateInterface() {
        setTitle(currentTaskContext.getName());
    }

    public void onSwitchTaskContextItemSelected(MenuItem item) {
        DialogFragment fragment = new SwitchTaskContextDialogFragment();
        fragment.show(getFragmentManager(), "switch_task_context");
    }

    public void onNewTaskItemSelected(MenuItem item) {
        Intent intent = new Intent(this, EditTaskActivity.class);
        intent.putExtra("action", "new");
        intent.putExtra("task_context_id", currentTaskContext.getId());

        startActivity(intent);
    }

    public void onAboutItemSelected(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private class SwitchTaskContextDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            long currentTaskContextId = MainActivity.this.currentTaskContext.getId();
            int selectedItem = -1;

            final List<TaskContext> taskContexts = MainActivity.this.applicationLogic.getAllTaskContexts();
            int taskContextCount = taskContexts.size();
            String[] taskContextNames = new String[taskContextCount];
            TaskContext t = null;

            for (int i = 0; i < taskContextCount; i++) {
                t = taskContexts.get(i);
                taskContextNames[i] = t.getName();

                if (t.getId() == currentTaskContextId) selectedItem = i;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.switch_task_context);
            builder.setNegativeButton(R.string.cancel, null);
            builder.setSingleChoiceItems(taskContextNames, selectedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    TaskContext newCurrentTaskContext = taskContexts.get(id);

                    MainActivity.this.currentTaskContext = newCurrentTaskContext;
                    MainActivity.this.applicationLogic.setCurrentTaskContext(newCurrentTaskContext);

                    dialog.dismiss();
                    MainActivity.this.updateInterface();
                }
            });

            return builder.create();
        }
    }
}
