package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class SwitchTaskContextDialogFragment extends DialogFragment {
    private Activity activity;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
        currentTaskContext = applicationLogic.getCurrentTaskContext();

        long currentTaskContextId = currentTaskContext.getId();
        int selectedItem = -1;

        final List<TaskContext> taskContexts = applicationLogic.getAllTaskContexts();
        int taskContextCount = taskContexts.size();
        String[] taskContextNames = new String[taskContextCount];
        TaskContext t;

        for (int i = 0; i < taskContextCount; i++) {
            t = taskContexts.get(i);
            taskContextNames[i] = t.getName();

            if (t.getId() == currentTaskContextId) selectedItem = i;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.switch_task_context);
        builder.setNeutralButton(R.string.edit_contexts, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(SwitchTaskContextDialogFragment.this.activity, EditTaskContextsActivity.class);
                startActivity(intent);

                // Close the dialog
                SwitchTaskContextDialogFragment.this.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(taskContextNames, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // "which" is the index position of the selected item.
                TaskContext selectedTaskContext = taskContexts.get(which);

                if (selectedTaskContext.getId() != SwitchTaskContextDialogFragment.this.currentTaskContext.getId()) { 
                    SwitchTaskContextDialogFragment.this.applicationLogic.setCurrentTaskContext(selectedTaskContext);
                }

                // Close the dialog
                SwitchTaskContextDialogFragment.this.dismiss();
            }
        });

        return builder.create();
    }
}
