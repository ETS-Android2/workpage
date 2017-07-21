package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;

public class EditDescriptionDialogFragment extends DialogFragment {
    private EditText descriptionEditText;
    private OnOkButtonClickedListener onOkButtonClickedListener;

    private String description;

    public EditDescriptionDialogFragment() {
        onOkButtonClickedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        description = (getArguments()).getString("description", "");
        Activity activity = getActivity();

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.edit_description, null);

        descriptionEditText = (EditText) view.findViewById(R.id.edit_description_description);
        descriptionEditText.setText(description);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        builder.setTitle(R.string.description);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (EditDescriptionDialogFragment.this.onOkButtonClickedListener != null) {
                    String description = (EditDescriptionDialogFragment.this.descriptionEditText.getText()).toString();
                    EditDescriptionDialogFragment.this.onOkButtonClickedListener.onOkButtonClicked(description);
                }
            }
        });

        return builder.create();
    }

    public void setOnOkButtonClickedListener(OnOkButtonClickedListener listener) {
        onOkButtonClickedListener = listener;
    }

    public static interface OnOkButtonClickedListener {
        void onOkButtonClicked(String description);
    }
}