package jajimenez.workpage;

import java.io.File;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;

public class DataImportConfirmationDialogFragment extends DialogFragment {
    private File from;
    private OnDataImportConfirmationListener onConfirmationListener;

    public DataImportConfirmationDialogFragment() {
        from = null;
        onConfirmationListener = null;
    }

    public DataImportConfirmationDialogFragment(File from) {
        this.from = from;
        onConfirmationListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        final Resources resources = activity.getResources();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(R.string.data_import_confirmation);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.import_data, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DataImportConfirmationDialogFragment.this.onConfirmationListener != null) {
                    DataImportConfirmationDialogFragment.this.onConfirmationListener.onConfirmation(DataImportConfirmationDialogFragment.this.from);
                }
            }
        });

        return builder.create();
    }

    public void setOnDataImportConfirmationListener(OnDataImportConfirmationListener listener) {
        onConfirmationListener = listener;
    }

    public static interface OnDataImportConfirmationListener {
        void onConfirmation(File from);
    }
}
