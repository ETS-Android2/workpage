package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;

public class EditTaskContextDialogFragment extends DialogFragment {
    private Activity activity;
    private AlertDialog dialog;
    private EditText nameEditText;
    private OnTaskContextSavedListener onTaskContextSavedListener;

    private ApplicationLogic applicationLogic;
    private TaskContext context;
    private List<TaskContext> contexts;

    public EditTaskContextDialogFragment() {
        onTaskContextSavedListener = null;
        context = null;
        contexts = new LinkedList<TaskContext>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        Bundle arguments = getArguments();
        long contextId = arguments.getLong("context_id", -1);

        if (contextId == -1) context = new TaskContext();
        else context = applicationLogic.getTaskContext(contextId);

        contexts = applicationLogic.getAllTaskContexts();

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_context, null);

        nameEditText = (EditText) view.findViewById(R.id.edit_task_context_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        if (context.getId() < 0) {
            // New Context mode
            builder.setTitle(R.string.new_context);
        } else {
            // Edit Context mode
            builder.setTitle(R.string.edit_context);
            nameEditText.setText(context.getName());
        }

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditTaskContextDialogFragment.this.nameEditText.getText()).toString()).trim();

                EditTaskContextDialogFragment.this.context.setName(name);

                EditTaskContextDialogFragment.this.applicationLogic.saveTaskContext(EditTaskContextDialogFragment.this.context);
                Toast.makeText(EditTaskContextDialogFragment.this.activity, R.string.context_saved, Toast.LENGTH_SHORT).show();

                if (EditTaskContextDialogFragment.this.onTaskContextSavedListener != null) {
                    EditTaskContextDialogFragment.this.onTaskContextSavedListener.onTaskContextSaved();
                }
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                boolean enabled = (text.length() > 0);

                if (enabled) {
                    // Auxiliar context object.
                    TaskContext newContext = new TaskContext();
                    newContext.setName(text);

                    enabled = (newContext.equals(EditTaskContextDialogFragment.this.context)
                            || !EditTaskContextDialogFragment.this.contexts.contains(newContext));

                    (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(enabled);
                }

                (EditTaskContextDialogFragment.this.dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(enabled);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        dialog = builder.create();

        return dialog;
    }

    public void setOnTaskContextSavedListener(OnTaskContextSavedListener listener) {
        onTaskContextSavedListener = listener;
    }

    public static interface OnTaskContextSavedListener {
        void onTaskContextSaved();
    }
}