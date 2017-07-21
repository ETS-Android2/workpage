package jajimenez.workpage;

import java.io.File;

import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

public class DataImportConfirmationDialogFragment extends DialogFragment {
    private File from;
    private OnDataImportConfirmationListener onConfirmationListener;

    public DataImportConfirmationDialogFragment() {
        from = null;
        onConfirmationListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // "filePath" is an absolute file path
        String filePath = (getArguments()).getString("file_path");
        if (filePath != null && !filePath.equals("")) from = new File(filePath);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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