package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CheckBox;
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

    private boolean saveButtonEnabled;

    private ApplicationLogic applicationLogic;
    private TaskContext context;
    private List<TaskContext> contexts;

    public EditTaskContextDialogFragment() {
        onTaskContextSavedListener = null;
        context = null;
        contexts = new LinkedList<TaskContext>();
    }

    /*public EditTaskContextDialogFragment(TaskContext context, List<TaskContext> contexts) {
        onTaskContextSavedListener = null;
        this.context = context;
        this.contexts = contexts;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        Bundle arguments = getArguments();
        long contextId = arguments.getLong("context_id", -1);

        if (contextId == -1) context = new TaskContext();
        else context = applicationLogic.getTaskContext(contextId);

        /*int contextsCount = 0;
        if (contextsIds != null) contextsCount = contextsIds.length;

        if (contextsCount > 0) {
            for (int i = 0; i < contextsCount; i++) {
                TaskContext c = new TaskContext();
                c.setId(contextsIds[i]);
                c.setName(contextsNames[i]);

                contexts.add(c);
            }
        }*/

        contexts = applicationLogic.getAllTaskContexts();
        //if (savedInstanceState == null) {
        //    saveButtonEnabled = (context.getId() >= 0);
        //} else {
            /*long contextId = savedInstanceState.getLong("context_id");
            long contextOrder = savedInstanceState.getLong("context_order");

            long[] contextsIds = savedInstanceState.getLongArray("contexts_ids");
            String[] contextsNames = savedInstanceState.getStringArray("contexts_names");

            context = new TaskContext();
            context.setId(contextId);
            context.setOrder(contextOrder);*/
            
            /*int contextsCount = 0;
            if (contextsIds != null) contextsCount = contextsIds.length;

            if (contextsCount > 0) {
                for (int i = 0; i < contextsCount; i++) {
                    TaskContext c = new TaskContext();
                    c.setId(contextsIds[i]);
                    c.setName(contextsNames[i]);

                    contexts.add(c);
                }
            }*/

        //    saveButtonEnabled = savedInstanceState.getBoolean("save_button_enabled");
        //}

        //activity = getActivity();
        //applicationLogic = new ApplicationLogic(activity);

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

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        int contextsCount = contexts.size();
        long[] contextsIds = new long[contextsCount];
        String[] contextsNames = new String[contextsCount];

        for (int i = 0; i < contextsCount; i++) {
            TaskContext c = contexts.get(i);

            contextsIds[i] = c.getId();
            contextsNames[i] = c.getName();
        }

        outState.putLong("context_id", context.getId());
        outState.putLong("context_order", context.getOrder());

        outState.putLongArray("contexts_ids", contextsIds);
        outState.putStringArray("contexts_names", contextsNames);
        outState.putBoolean("save_button_enabled", (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).isEnabled());
    }*/

    /*@Override
    public void onStart() {
        super.onStart();
        (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(saveButtonEnabled);
    }*/

    public void setOnTaskContextSavedListener(OnTaskContextSavedListener listener) {
        onTaskContextSavedListener = listener;
    }

    public static interface OnTaskContextSavedListener {
        void onTaskContextSaved();
    }
}
