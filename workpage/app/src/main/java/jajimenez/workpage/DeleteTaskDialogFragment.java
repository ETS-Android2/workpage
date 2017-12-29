package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.Toast;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class DeleteTaskDialogFragment extends DialogFragment {
    private Activity activity;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public DeleteTaskDialogFragment() {
        tasks = new LinkedList<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
        final Resources resources = activity.getResources();

        long[] taskIds = (getArguments()).getLongArray("task_ids");

        if (taskIds != null) {
            for (long id : taskIds) {
                Task t = applicationLogic.getTask(id);
                tasks.add(t);
            }
        }

        final int selectedTaskCount = tasks.size();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_task, selectedTaskCount, selectedTaskCount));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeleteTaskDialogFragment.this.applicationLogic.deleteTasks(DeleteTaskDialogFragment.this.tasks);

                String text = resources.getQuantityString(R.plurals.task_deleted, selectedTaskCount, selectedTaskCount);
                Toast.makeText(DeleteTaskDialogFragment.this.activity, text, Toast.LENGTH_SHORT).show();
            }
        });

        return builder.create();
    }
}
