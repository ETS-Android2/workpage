package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class ChangeTaskStatusDialogFragment extends DialogFragment {
    private Context context;
    private OnItemClickListener onItemClickListener;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public ChangeTaskStatusDialogFragment(Context context, List<Task> tasks) {
        this.context = context;
        onItemClickListener = null;

        applicationLogic = new ApplicationLogic(context);
        this.tasks = tasks;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Resources resources = context.getResources();
        int selectedTaskCount = tasks.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

                // Close the dialog.
                dialog.dismiss();

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
