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
import android.graphics.Color;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskTag;

public class EditTaskTagDialogFragment extends DialogFragment {
    private Activity activity;
    private AlertDialog dialog;
    private EditText nameEditText;
    private CheckBox setColorCheckBox;
    private ColorView selectedColorView;
    private Button selectButton;
    private OnTaskTagSavedListener onTaskTagSavedListener;

    private boolean saveButtonEnabled;
    private ColorPickerDialogFragment.OnColorSelectedListener colorSelectedListener;

    private ApplicationLogic applicationLogic;
    private TaskTag tag;
    private List<TaskTag> contextTags;

    public EditTaskTagDialogFragment() {
        tag = null;
        contextTags = new LinkedList<TaskTag>();
        onTaskTagSavedListener = null;
    }

    public EditTaskTagDialogFragment(TaskTag tag, List<TaskTag> contextTags) {
        this.tag = tag;
        this.contextTags = contextTags;
        this.onTaskTagSavedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        colorSelectedListener = new ColorPickerDialogFragment.OnColorSelectedListener() {
            public void onColorSelected(int color) {
                EditTaskTagDialogFragment.this.selectedColorView.setBackgroundColor(color);
            }
        };

        if (savedInstanceState == null) {
            saveButtonEnabled = (tag.getId() >= 0);
        } else {
            long tagId = savedInstanceState.getLong("tag_id");
            long tagContextId = savedInstanceState.getLong("tag_context_id");

            long[] contextTagIds = savedInstanceState.getLongArray("context_task_tag_ids");
            String[] contextTagNames = savedInstanceState.getStringArray("context_task_tag_names");

            tag = new TaskTag();
            tag.setId(tagId);
            tag.setContextId(tagContextId);
            
            int contextTagCount = 0;
            if (contextTagIds != null) contextTagCount = contextTagIds.length;

            if (contextTagCount > 0) {
                for (int i = 0; i < contextTagCount; i++) {
                    TaskTag tag = new TaskTag();
                    tag.setId(contextTagIds[i]);
                    tag.setName(contextTagNames[i]);

                    contextTags.add(tag);
                }
            }

            saveButtonEnabled = savedInstanceState.getBoolean("save_button_enabled");

            ColorPickerDialogFragment colorPickerFragment = (ColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("color_picker");
            if (colorPickerFragment != null) colorPickerFragment.setOnColorSelectedListener(colorSelectedListener);
        }

        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_tag, null);

        nameEditText = (EditText) view.findViewById(R.id.editTaskTag_name);
        setColorCheckBox = (CheckBox) view.findViewById(R.id.editTaskTag_setColor);
        selectedColorView = (ColorView) view.findViewById(R.id.editTaskTag_selected);
        selectButton = (Button) view.findViewById(R.id.editTaskTag_select);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        if (tag.getId() < 0) {
            // New Tag mode
            builder.setTitle(R.string.new_tag);
            selectButton.setEnabled(false);
        }
        else {
            // Edit Tag mode
            builder.setTitle(R.string.edit_tag);
            nameEditText.setText(tag.getName());

            boolean setColor;

            if (savedInstanceState == null) {
                String color = tag.getColor();
                setColor = (color != null && !color.equals(""));

                if (setColor) selectedColorView.setBackgroundColor(Color.parseColor(color));
            } else {
                int selectedColor = savedInstanceState.getInt("selected_color", 0xFFFFFFFF);
                selectedColorView.setBackgroundColor(selectedColor);

                setColor = savedInstanceState.getBoolean("set_color");
            }

            setColorCheckBox.setChecked(setColor);
            selectButton.setEnabled(setColor);
        }

        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String name = ((EditTaskTagDialogFragment.this.nameEditText.getText()).toString()).trim();
                
                // Convert the int-color to hexadecimal color.
                String color;

                if (EditTaskTagDialogFragment.this.setColorCheckBox.isChecked()) {
                    color = String.format("#%06X", 0xFFFFFF & EditTaskTagDialogFragment.this.selectedColorView.getBackgroundColor());
                } else {
                    color = null;
                }

                EditTaskTagDialogFragment.this.tag.setName(name);
                EditTaskTagDialogFragment.this.tag.setColor(color);

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

        setColorCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                EditTaskTagDialogFragment.this.selectButton.setEnabled(isChecked);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ColorPickerDialogFragment fragment = new ColorPickerDialogFragment(EditTaskTagDialogFragment.this.selectedColorView.getBackgroundColor());
                ColorPickerDialogFragment fragment = new ColorPickerDialogFragment();
                fragment.setOnColorSelectedListener(EditTaskTagDialogFragment.this.colorSelectedListener);
                fragment.show(getFragmentManager(), "color_picker");
            }
        });

        dialog = builder.create();

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        int contextTagCount = contextTags.size();
        long[] contextTagIds = new long[contextTagCount];
        String[] contextTagNames = new String[contextTagCount];

        for (int i = 0; i < contextTagCount; i++) {
            TaskTag contextTag = contextTags.get(i);

            contextTagIds[i] = contextTag.getId();
            contextTagNames[i] = contextTag.getName();
        }

        outState.putLong("tag_id", tag.getId());
        outState.putLong("tag_context_id", tag.getContextId());

        outState.putLongArray("context_task_tag_ids", contextTagIds);
        outState.putStringArray("context_task_tag_names", contextTagNames);
        outState.putBoolean("set_color", setColorCheckBox.isChecked());
        outState.putInt("selected_color", selectedColorView.getBackgroundColor());
        outState.putBoolean("save_button_enabled", (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).isEnabled());
    }

    @Override
    public void onStart() {
        super.onStart();
        (dialog.getButton(DialogInterface.BUTTON_POSITIVE)).setEnabled(saveButtonEnabled);
    }

    public void setOnTaskTagSavedListener(OnTaskTagSavedListener listener) {
        onTaskTagSavedListener = listener;
    }

    public static interface OnTaskTagSavedListener {
        void onTaskTagSaved();
    }
}
