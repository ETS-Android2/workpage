package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class SwitchTaskContextDialogFragment extends DialogFragment {
    private Activity activity;
    private OnNewCurrentTaskContextSetListener onNewCurrentTaskContextSetListener;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;

    public SwitchTaskContextDialogFragment() {
        onNewCurrentTaskContextSetListener = null;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
        currentTaskContext = applicationLogic.getCurrentTaskContext();

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

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.switch_task_context);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(taskContextNames, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // "which" is the index position of the selected item.
                TaskContext selectedTaskContext = taskContexts.get(which);

                if (selectedTaskContext.getId() != SwitchTaskContextDialogFragment.this.currentTaskContext.getId()) { 
                    String newCurrentView = "open";
                    List<TaskTag> newCurrentFilterTags = new LinkedList<TaskTag>();

                    SwitchTaskContextDialogFragment.this.applicationLogic.setCurrentTaskContext(selectedTaskContext);
                    SwitchTaskContextDialogFragment.this.applicationLogic.setCurrentView(newCurrentView);
                    SwitchTaskContextDialogFragment.this.applicationLogic.setCurrentFilterTags(newCurrentFilterTags);


                    if (SwitchTaskContextDialogFragment.this.onNewCurrentTaskContextSetListener != null) {
                        SwitchTaskContextDialogFragment.this.onNewCurrentTaskContextSetListener.onNewCurrentTaskContextSet(selectedTaskContext,
                            newCurrentView,
                            newCurrentFilterTags);
                    }
                }

                // Close the dialog.
                SwitchTaskContextDialogFragment.this.dismiss();
            }
        });

        return builder.create();
    }

    public void setOnNewCurrentTaskContextSetListener(OnNewCurrentTaskContextSetListener listener) {
        onNewCurrentTaskContextSetListener = listener;
    }

    public static interface OnNewCurrentTaskContextSetListener {
        void onNewCurrentTaskContextSet(TaskContext newCurrentTaskContext, String newCurrentView, List<TaskTag> newCurrentFilterTags);
    }
}
