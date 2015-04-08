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
import jajimenez.workpage.data.model.TaskContext;

public class DeleteTaskContextDialogFragment extends DialogFragment {
    private Activity activity;
    private OnDeleteListener onDeleteListener;

    private ApplicationLogic applicationLogic;
    private List<TaskContext> contexts;

    public DeleteTaskContextDialogFragment() {
        onDeleteListener = null;
        contexts = new LinkedList<TaskContext>();
    }

    public DeleteTaskContextDialogFragment(List<TaskContext> contexts) {
        onDeleteListener = null;
        this.contexts = contexts;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            long[] contextIds = savedInstanceState.getLongArray("task_contexts");
            
            if (contextIds != null) {
                contexts = new LinkedList<TaskContext>();

                for (long id : contextIds) {
                    TaskContext context = new TaskContext();
                    context.setId(id);

                    contexts.add(context);
                }
            }
        }

        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        final Resources resources = activity.getResources();
        final int selectedContextCount = contexts.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_context, selectedContextCount, selectedContextCount));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DeleteTaskContextDialogFragment.this.applicationLogic.deleteTaskContexts(DeleteTaskContextDialogFragment.this.contexts);

                String text = resources.getQuantityString(R.plurals.context_deleted, selectedContextCount, selectedContextCount);
                Toast.makeText(DeleteTaskContextDialogFragment.this.activity, text, Toast.LENGTH_SHORT).show();

                if (DeleteTaskContextDialogFragment.this.onDeleteListener != null) {
                    DeleteTaskContextDialogFragment.this.onDeleteListener.onDelete();
                }

            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        int contextCount = contexts.size();
        long[] contextIds = new long[contextCount];
        for (int i = 0; i < contextCount; i++) contextIds[i] = (contexts.get(i)).getId();

        outState.putLongArray("task_contexts", contextIds);
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        onDeleteListener = listener;
    }

    public static interface OnDeleteListener {
        void onDelete();
    }
}
