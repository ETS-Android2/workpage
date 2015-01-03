package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class ChangeTaskStatusDialogFragment extends DialogFragment {
    private Activity activity;
    private OnItemClickListener onItemClickListener;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public ChangeTaskStatusDialogFragment(List<Task> tasks) {
        onItemClickListener = null;
        this.tasks = tasks;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        Resources resources = activity.getResources();
        int selectedTaskCount = tasks.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(resources.getQuantityString(R.plurals.task_status, selectedTaskCount, selectedTaskCount));
        builder.setNegativeButton(R.string.cancel, null);

        String[] items = null;
        final boolean firstTaskDone = (tasks.get(0)).isDone();

        if (firstTaskDone) items = new String[] { getString(R.string.mark_as_not_done) };
        else items = new String[] { getString(R.string.mark_as_done) };

        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Switch tasks status.
                boolean done = !firstTaskDone;

                for (Task task : ChangeTaskStatusDialogFragment.this.tasks) {
                    task.setDone(done);
                    ChangeTaskStatusDialogFragment.this.applicationLogic.saveTask(task);
                }

                if (ChangeTaskStatusDialogFragment.this.onItemClickListener != null) {
                    ChangeTaskStatusDialogFragment.this.onItemClickListener.onItemClick();
                }
            }
        });

        return builder.create();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public static interface OnItemClickListener {
        void onItemClick();
    }
}
