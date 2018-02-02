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
import android.widget.EditText;

public class EditDescriptionDialogFragment extends DialogFragment {
    private EditText descriptionEditText;
    private OnDialogClosedListener onDialogClosedListener;

    private String description;

    public EditDescriptionDialogFragment() {
        onDialogClosedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        description = (getArguments()).getString("description", "");
        Activity activity = getActivity();

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_description, null);

        descriptionEditText = view.findViewById(R.id.edit_description_description);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        builder.setTitle(R.string.description);
        builder.setNegativeButton(R.string.accept, null);

        Dialog dialog = builder.create();
        (dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        updateInterface();

        return dialog;
    }

    private void updateInterface() {
        descriptionEditText.setText(description);
        descriptionEditText.setSelection((descriptionEditText.getText()).length());
        descriptionEditText.requestFocus();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDialogClosedListener != null) {
            String description = (descriptionEditText.getText()).toString();
            onDialogClosedListener.onDialogClosed(description);
        }
    }

    public void setOnDialogClosedListener(OnDialogClosedListener listener) {
        onDialogClosedListener = listener;
    }

    public interface OnDialogClosedListener {
        void onDialogClosed(String description);
    }
}
