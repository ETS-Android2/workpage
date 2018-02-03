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
import android.view.WindowManager;
import android.widget.Button;
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
    private Button positiveButton;

    private ApplicationLogic applicationLogic;
    private TaskContext context;
    private List<TaskContext> contexts;

    public EditTaskContextDialogFragment() {
        context = null;
        contexts = new LinkedList<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
        contexts = applicationLogic.getAllTaskContexts();

        Bundle arguments = getArguments();
        long contextId = arguments.getLong("context_id", -1);

        if (contextId == -1) {
            // New Context mode
            context = new TaskContext();
        }
        else {
            // Edit Context mode
            context = applicationLogic.getTaskContext(contextId);
            contexts.remove(context);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_context, null);

        nameEditText = view.findViewById(R.id.edit_task_context_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        if (contextId == -1) {
            // New Context mode
            builder.setTitle(R.string.new_context);

        } else {
            // Edit Context mode
            builder.setTitle(R.string.edit_context);
        }

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditTaskContextDialogFragment.this.nameEditText.getText()).toString()).trim();

                EditTaskContextDialogFragment.this.context.setName(name);

                EditTaskContextDialogFragment.this.applicationLogic.saveTaskContext(EditTaskContextDialogFragment.this.context);
                Toast.makeText(EditTaskContextDialogFragment.this.activity, R.string.context_saved, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                EditTaskContextDialogFragment.this.context.setName(text);

                // The buttons can be accessed only when the dialog is shown, but not on the dialog creation.
                if (positiveButton != null) updateNameEditText();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        });

        dialog = builder.create();
        (dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        updateInterface();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        updateNameEditText();
    }

    private void updateInterface() {
        nameEditText.setText(context.getName());
        nameEditText.setSelection((nameEditText.getText()).length());
        nameEditText.requestFocus();
    }

    private void updateNameEditText() {
        positiveButton.setEnabled((context.getName()).length() > 0  && !contexts.contains(context));
    }
}
