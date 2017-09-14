package jajimenez.workpage;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.*;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskTag;

public class EditTaskTagDialogFragment extends DialogFragment {
    private Activity activity;
    private AlertDialog dialog;
    private ImageButton colorImageButton;
    private EditText nameEditText;
    private OnTaskTagSavedListener onTaskTagSavedListener;

    private ColorPickerDialogFragment.OnColorSelectedListener colorSelectedListener;
    private ColorPickerDialogFragment.OnNoColorSelectedListener noColorSelectedListener;

    private ApplicationLogic applicationLogic;
    private TaskTag tag;
    private List<TaskTag> contextTags;

    public EditTaskTagDialogFragment() {
        onTaskTagSavedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        Bundle arguments = getArguments();

        long tagId = arguments.getLong("tag_id", -1);
        long contextId = arguments.getLong("context_id", -1);
        TaskContext context = applicationLogic.getTaskContext(contextId);
        contextTags = applicationLogic.getAllTaskTags(context);

        if (tagId == -1) {
            // New Tag mode
            tag = new TaskTag();
            tag.setContextId(contextId);
        }
        else {
            // Edit Tag mode
            tag = applicationLogic.getTaskTag(tagId);
            contextTags.remove(tag);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_tag, null);

        nameEditText = (EditText) view.findViewById(R.id.edit_task_tag_name);
        colorImageButton = (ImageButton) view.findViewById(R.id.edit_task_tag_color);

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

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                EditTaskTagDialogFragment.this.applicationLogic.saveTaskTag(EditTaskTagDialogFragment.this.tag);
                Toast.makeText(EditTaskTagDialogFragment.this.activity, R.string.tag_saved, Toast.LENGTH_SHORT).show();

                if (EditTaskTagDialogFragment.this.onTaskTagSavedListener != null) {
                    EditTaskTagDialogFragment.this.onTaskTagSavedListener.onTaskTagSaved();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        // Listeners
        colorSelectedListener = new ColorPickerDialogFragment.OnColorSelectedListener() {
            public void onColorSelected(int color) {
                String tagColor = String.format("#%06X", 0xFFFFFF & color);
                EditTaskTagDialogFragment.this.tag.setColor(tagColor);

                EditTaskTagDialogFragment.this.updateInterface();
            }
        };

        noColorSelectedListener = new ColorPickerDialogFragment.OnNoColorSelectedListener() {
            public void onNoColorSelected() {
                EditTaskTagDialogFragment.this.tag.setColor(null);
                EditTaskTagDialogFragment.this.updateInterface();
            }
        };

        colorImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
                fragment.setOnColorSelectedListener(EditTaskTagDialogFragment.this.colorSelectedListener);
                fragment.setOnNoColorSelectedListener(EditTaskTagDialogFragment.this.noColorSelectedListener);

                fragment.show(getFragmentManager(), "color_picker");
            }
        });

        nameEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                EditTaskTagDialogFragment.this.tag.setName(text);

                Button b = EditTaskTagDialogFragment.this.dialog.getButton(DialogInterface.BUTTON_POSITIVE);

                if (b != null) {
                    boolean enabled = (text.length() > 0
                            && !EditTaskTagDialogFragment.this.contextTags.contains(EditTaskTagDialogFragment.this.tag));

                    b.setEnabled(enabled);
                }
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        if (savedInstanceState != null) {
            ColorPickerDialogFragment colorPickerFragment = (ColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("color_picker");

            if (colorPickerFragment != null) {
                colorPickerFragment.setOnColorSelectedListener(colorSelectedListener);
                colorPickerFragment.setOnNoColorSelectedListener(noColorSelectedListener);
            }

            AdvancedColorPickerDialogFragment advancedColorPickerFragment = (AdvancedColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("advanced_color_picker");
            if (advancedColorPickerFragment != null) advancedColorPickerFragment.setOnColorSelectedListener(colorSelectedListener);
        }

        dialog = builder.create();
        updateInterface();

        return dialog;
    }

    private void updateInterface() {
        String color = tag.getColor();

        if (color != null && !color.equals("")) {
            colorImageButton.setImageResource(R.drawable.color);
            colorImageButton.setColorFilter(Color.parseColor(color));
        }
        else {
            colorImageButton.setImageResource(R.drawable.no_color);
        }

        nameEditText.setText(tag.getName());
    }

    public void setOnTaskTagSavedListener(OnTaskTagSavedListener listener) {
        onTaskTagSavedListener = listener;
    }

    public static interface OnTaskTagSavedListener {
        void onTaskTagSaved();
    }
}