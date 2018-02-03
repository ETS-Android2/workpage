package jajimenez.workpage;

import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;
import jajimenez.workpage.logic.ApplicationLogic;

public class TaskTagPickerDialogFragment extends DialogFragment {
    private Activity activity;
    private ApplicationLogic applicationLogic;

    private TaskContext context;
    private List<TaskTag> contextTags;
    private List<TaskTag> selectedTags;

    private AutoCompleteTextView addTagAutoTextView;
    private ImageButton addTagButton;
    private LinearLayout addedTagsLinearLayout;

    private OnTaskTagsSelectedListener onTaskTagsSelectedListener;

    public TaskTagPickerDialogFragment() {
        onTaskTagsSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.task_tag_picker, null);

        addTagAutoTextView = view.findViewById(R.id.task_tag_picker_add_tag_autotextview);
        addTagButton = view.findViewById(R.id.task_tag_picker_add_tag_button);
        addedTagsLinearLayout = view.findViewById(R.id.task_tag_picker_added_tags);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setTitle(R.string.tags);

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (TaskTagPickerDialogFragment.this.onTaskTagsSelectedListener != null) {
                    TaskTagPickerDialogFragment.this.onTaskTagsSelectedListener.onTaskTagsSelected(TaskTagPickerDialogFragment.this.selectedTags);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        addTagAutoTextView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }

            public void afterTextChanged(Editable s) {
                TaskTagPickerDialogFragment.this.checkTagName();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        });

        addTagButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String tagName = ((addTagAutoTextView.getText()).toString()).trim();

                TaskTag tag = new TaskTag();
                tag.setName(tagName);
                tag.setContextId(TaskTagPickerDialogFragment.this.context.getId());

                // Add the new tag to the Selected Tags
                TaskTagPickerDialogFragment.this.selectedTags.add(tag);

                // Clear the text box
                TaskTagPickerDialogFragment.this.addTagAutoTextView.setText("");

                // Update user interface
                TaskTagPickerDialogFragment.this.updateInterface();
            }
        });

        applicationLogic = new ApplicationLogic(activity);

        Bundle arguments = getArguments();

        // Context tags
        long contextId = arguments.getLong("task_context_id", 1);
        context = applicationLogic.getTaskContext(contextId);
        contextTags = applicationLogic.getAllTaskTags(context);

        // Selected tags
        if (savedInstanceState == null) selectedTags = getInitialSelectedTags(arguments);
        else selectedTags = getSelectedTags(savedInstanceState);

        AlertDialog dialog = builder.create();

        if (selectedTags.size() == 0) {
            addTagAutoTextView.requestFocus();
            (dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        updateInterface();

        return dialog;
    }

    private List<TaskTag> getInitialSelectedTags(Bundle arguments) {
        String[] tagNames = arguments.getStringArray("initial_selected_tags");
        return getTags(tagNames);
    }

    private List<TaskTag> getSelectedTags(Bundle savedInstanceState) {
        String[] tagNames = savedInstanceState.getStringArray("selected_task_tags");
        return getTags(tagNames);
    }

    private List<TaskTag> getTags(String[] tagNames) {
        LinkedList<TaskTag> tags = new LinkedList<>();

        if (tagNames != null) {
            for (String name : tagNames) {
                TaskTag tag = new TaskTag();
                tag.setName(name);
                tag.setContextId(context.getId());

                tags.add(tag);
            }
        }

        return tags;
    }

    private void updateInterface() {
        setTagCompletionSuggestions();
        showTaskTagViews();
        checkTagName();
    }

    private void setTagCompletionSuggestions() {
        List<TaskTag> suggestedTags = new LinkedList<>(contextTags);
        suggestedTags.removeAll(selectedTags);

        ArrayAdapter<TaskTag> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, suggestedTags);
        addTagAutoTextView.setAdapter(adapter);
    }

    private void showTaskTagViews() {
        addedTagsLinearLayout.removeAllViews();

        for (final TaskTag tag : selectedTags) {
            final TaskTagView tagView = new TaskTagView(activity);
            tagView.setText(tag.getName());

            tagView.setOnRemoveIconClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Remove the tag from the Selected Tags
                    selectedTags.remove(tag);

                    // Update user interface
                    TaskTagPickerDialogFragment.this.updateInterface();
                    TaskTagPickerDialogFragment.this.checkTagName();
                }
            });

            // Add the tag view
            addedTagsLinearLayout.addView(tagView);
        }
    }

    private void checkTagName() {
        String tagName = ((addTagAutoTextView.getText()).toString()).trim();
        boolean enabled = (tagName.length() > 0);

        if (enabled) {
            // Auxiliar tag object
            TaskTag tag = new TaskTag();
            tag.setName(tagName);

            enabled = !selectedTags.contains(tag);
        }

        addTagButton.setEnabled(enabled);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Selected tags
        int tagCount = selectedTags.size();
        String[] tagNames = new String[tagCount];
        for (int i = 0; i < tagCount; i++) tagNames[i] = (selectedTags.get(i)).getName();

        outState.putStringArray("selected_task_tags", tagNames);

        super.onSaveInstanceState(outState);
    }

    public void setOnTaskTagsSelectedListener(OnTaskTagsSelectedListener listener) {
        onTaskTagsSelectedListener = listener;
    }

    public interface OnTaskTagsSelectedListener {
        void onTaskTagsSelected(List<TaskTag> tags);
    }
}
