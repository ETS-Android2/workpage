package jajimenez.workpage;

import java.util.List;

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
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskTag;

public class EditTaskTagDialogFragment extends DialogFragment {
    private Activity activity;
    private AlertDialog dialog;
    private EditText nameEditText;
    private OnTaskTagSavedListener onTaskTagSavedListener;

    private ApplicationLogic applicationLogic;
    private TaskTag tag;
    private List<TaskTag> contextTags;

    public EditTaskTagDialogFragment(TaskTag tag, List<TaskTag> contextTags) {
        onTaskTagSavedListener = null;
        this.tag = tag;
        this.contextTags = contextTags;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_tag, null);

        nameEditText = (EditText) view.findViewById(R.id.editTaskTag_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        if (tag.getId() < 0) {
            // New Tag mode
            builder.setTitle(R.string.new_tag);
        }
        else {
            // Edit Tag mode
            builder.setTitle(R.string.edit_tag);
            nameEditText.setText(tag.getName());
        }

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditTaskTagDialogFragment.this.nameEditText.getText()).toString()).trim();
                EditTaskTagDialogFragment.this.tag.setName(name);

                EditTaskTagDialogFragment.this.applicationLogic.saveTaskTag(EditTaskTagDialogFragment.this.tag);
                Toast.makeText(EditTaskTagDialogFragment.this.activity, R.string.tag_saved, Toast.LENGTH_SHORT).show();

                if (EditTaskTagDialogFragment.this.onTaskTagSavedListener != null) {
                    EditTaskTagDialogFragment.this.onTaskTagSavedListener.onTaskTagSaved();
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
                    // Auxiliar tag object.
                    TaskTag newTag = new TaskTag();
                    newTag.setName(text);

                    enabled = (newTag.equals(EditTaskTagDialogFragment.this.tag) || !EditTaskTagDialogFragment.this.contextTags.contains(newTag));
                }

                (EditTaskTagDialogFragment.this.dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(enabled);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        dialog = builder.create();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (tag.getId() < 0) {
            // New Tag mode
            (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(false);
        }
    }

    public void setOnTaskTagSavedListener(OnTaskTagSavedListener listener) {
        onTaskTagSavedListener = listener;
    }

    public static interface OnTaskTagSavedListener {
        void onTaskTagSaved();
    }
}
