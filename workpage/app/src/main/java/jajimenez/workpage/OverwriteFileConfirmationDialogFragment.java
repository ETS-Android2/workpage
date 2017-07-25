package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;

public class OverwriteFileConfirmationDialogFragment extends DialogFragment {
    private OnOverwriteConfirmationListener onConfirmationListener;

    public OverwriteFileConfirmationDialogFragment() {
        onConfirmationListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String fileName = (getArguments()).getString("file_name", "");

        Activity activity = getActivity();
        final Resources resources = activity.getResources();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(resources.getString(R.string.file_overwrite_confirmation, fileName));
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (OverwriteFileConfirmationDialogFragment.this.onConfirmationListener != null) {
                    OverwriteFileConfirmationDialogFragment.this.onConfirmationListener.onConfirmation();
                }
            }
        });

        return builder.create();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("file_name", fileName);
        super.onSaveInstanceState(outState);
    }*/

    public void setOnOverwriteConfirmationListener(OnOverwriteConfirmationListener listener) {
        onConfirmationListener = listener;
    }

    public static interface OnOverwriteConfirmationListener {
        void onConfirmation();
    }
}