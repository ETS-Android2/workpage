package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.Toast;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class DeleteTaskDialogFragment extends DialogFragment {
    private Context context;
    private OnDeleteListener onDeleteListener;

    private ApplicationLogic applicationLogic;
    private List<Task> tasks;

    public DeleteTaskDialogFragment(Context context, List<Task> tasks) {
        this.context = context;
        onDeleteListener = null;

        applicationLogic = new ApplicationLogic(context);
        this.tasks = tasks;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Resources resources = context.getResources();
        final int selectedTaskCount = tasks.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_task, selectedTaskCount, selectedTaskCount));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteTaskDialogFragment.this.applicationLogic.deleteTasks(DeleteTaskDialogFragment.this.tasks);

                String text = resources.getQuantityString(R.plurals.task_deleted, selectedTaskCount, selectedTaskCount);
                Toast.makeText(DeleteTaskDialogFragment.this.context, text, Toast.LENGTH_SHORT).show();

                // Close the dialog.
                dialog.dismiss();

                if (DeleteTaskDialogFragment.this.onDeleteListener != null) {
                    DeleteTaskDialogFragment.this.onDeleteListener.onDelete();
                }

            }
        });

        return builder.create();
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public static interface OnDeleteListener {
        void onDelete();
    }
}
