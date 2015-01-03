package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.Toast;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskTag;

public class DeleteTaskTagDialogFragment extends DialogFragment {
    private Activity activity;
    private OnDeleteListener onDeleteListener;

    private ApplicationLogic applicationLogic;
    private List<TaskTag> tags;

    public DeleteTaskTagDialogFragment(List<TaskTag> tags) {
        onDeleteListener = null;
        this.tags = tags;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        final Resources resources = activity.getResources();
        final int selectedTagCount = tags.size();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(resources.getQuantityString(R.plurals.delete_selected_tag, selectedTagCount, selectedTagCount));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DeleteTaskTagDialogFragment.this.applicationLogic.deleteTaskTags(DeleteTaskTagDialogFragment.this.tags);

                String text = resources.getQuantityString(R.plurals.tag_deleted, selectedTagCount, selectedTagCount);
                Toast.makeText(DeleteTaskTagDialogFragment.this.activity, text, Toast.LENGTH_SHORT).show();

                if (DeleteTaskTagDialogFragment.this.onDeleteListener != null) {
                    DeleteTaskTagDialogFragment.this.onDeleteListener.onDelete();
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
