package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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

import jajimenez.workpage.data.model.TaskContext;
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
            tag = new TaskTag();
            tag.setContextId(contextId);
        }
        else {
            tag = applicationLogic.getTaskTag(tagId);
        }

        colorSelectedListener = new ColorPickerDialogFragment.OnColorSelectedListener() {
            public void onColorSelected(int color) {
                EditTaskTagDialogFragment.this.selectedColorView.setBackgroundColor(color);
            }
        };

        if (savedInstanceState == null) {
            saveButtonEnabled = (tag.getId() >= 0);
        }
        else {
            saveButtonEnabled = savedInstanceState.getBoolean("save_button_enabled");

            ColorPickerDialogFragment colorPickerFragment = (ColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("color_picker");
            if (colorPickerFragment != null) colorPickerFragment.setOnColorSelectedListener(colorSelectedListener);

            AdvancedColorPickerDialogFragment advancedColorPickerFragment = (AdvancedColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("advanced_color_picker");
            if (advancedColorPickerFragment != null) advancedColorPickerFragment.setOnColorSelectedListener(colorSelectedListener);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_task_tag, null);

        nameEditText = (EditText) view.findViewById(R.id.edit_task_tag_name);
        setColorCheckBox = (CheckBox) view.findViewById(R.id.edit_task_tag_set_color);
        selectedColorView = (ColorView) view.findViewById(R.id.edit_task_tag_selected);
        selectButton = (Button) view.findViewById(R.id.edit_task_tag_select);

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