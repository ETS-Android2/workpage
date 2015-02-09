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
    private OnDeleteListener onDeleteListener;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public DeleteTaskDialogFragment() {
        onDeleteListener = null;
        tasks = new LinkedList<Task>();
    }

    public DeleteTaskDialogFragment(List<Task> tasks) {
        onDeleteListener = null;
        this.tasks = tasks;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long[] taskIds = savedInstanceState.getLongArray("tasks");
            
            if (taskIds != null) {
                tasks = new LinkedList<Task>();

                for (long id : taskIds) {
                    Task task = new Task();
                    task.setId(id);

                    tasks.add(task);
                }
            }
        }

        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        final Resources resources = activity.getResources();
        final int selectedTaskCount = tasks.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_task, selectedTaskCount, selectedTaskCount));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeleteTaskDialogFragment.this.applicationLogic.deleteTasks(DeleteTaskDialogFragment.this.tasks);

                String text = resources.getQuantityString(R.plurals.task_deleted, selectedTaskCount, selectedTaskCount);
                Toast.makeText(DeleteTaskDialogFragment.this.activity, text, Toast.LENGTH_SHORT).show();

                if (DeleteTaskDialogFragment.this.onDeleteListener != null) {
                    DeleteTaskDialogFragment.this.onDeleteListener.onDelete();
                }

            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        int taskCount = tasks.size();
        long[] taskIds = new long[taskCount];
        for (int i = 0; i < taskCount; i++) taskIds[i] = (tasks.get(i)).getId();

        outState.putLongArray("tasks", taskIds);

        super.onSaveInstanceState(outState);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public static interface OnDeleteListener {
        void onDelete();
    }
}
