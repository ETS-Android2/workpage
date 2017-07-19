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

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class ChangeTaskStatusDialogFragment extends DialogFragment {
    private Activity activity;
    private OnItemClickListener onItemClickListener;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public ChangeTaskStatusDialogFragment() {
        onItemClickListener = null;
        tasks = new LinkedList<Task>();
    }

    /*public ChangeTaskStatusDialogFragment(List<Task> tasks) {
        onItemClickListener = null;
        this.tasks = tasks;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
        Resources resources = activity.getResources();

        long[] taskIds = (getArguments()).getLongArray("task_ids");

        if (taskIds != null) {
            for (long id : taskIds) {
                Task t = applicationLogic.getTask(id);
                tasks.add(t);
            }
        }

        /*if (savedInstanceState != null) {
            long[] taskIds = savedInstanceState.getLongArray("task_ids");
            
            if (taskIds != null) {
                int taskCount = taskIds.length;
                tasks = new LinkedList<Task>();

                for (int i = 0; i < taskCount; i++) {
                    Task task = applicationLogic.getTask(taskIds[i]);
                    tasks.add(task);
                }
            }
        }*/

        int selectedTaskCount = tasks.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(resources.getQuantityString(R.plurals.task_state, selectedTaskCount, selectedTaskCount));
        builder.setNegativeButton(R.string.cancel, null);

        String[] items = null;
        final boolean firstTaskDone = (tasks.get(0)).isDone();

        if (firstTaskDone) items = new String[] { getString(R.string.mark_as_open) };
        else items = new String[] { getString(R.string.mark_as_closed) };

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

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        int taskCount = tasks.size();
        long[] taskIds = new long[taskCount];

        for (int i = 0; i < taskCount; i++) {
            taskIds[i] = (tasks.get(i)).getId();
        }

        outState.putLongArray("task_ids", taskIds);
    }*/

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public static interface OnItemClickListener {
        void onItemClick();
    }
}