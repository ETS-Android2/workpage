package jajimenez.workpage;

import android.net.Uri;
import android.os.Bundle;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

public class DataImportConfirmationDialogFragment extends DialogFragment {
    private OnDataImportConfirmationListener onConfirmationListener;

    public DataImportConfirmationDialogFragment() {
        onConfirmationListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // "filePath" is an absolute file path
        String inputStr = (getArguments()).getString("input");
        final Uri input = Uri.parse(inputStr);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.data_import_confirmation);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.import_data, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DataImportConfirmationDialogFragment.this.onConfirmationListener != null) {
                    DataImportConfirmationDialogFragment.this.onConfirmationListener.onConfirmation(input);
                }
            }
        });

        return builder.create();
    }

    public void setOnDataImportConfirmationListener(OnDataImportConfirmationListener listener) {
        onConfirmationListener = listener;
    }

    public interface OnDataImportConfirmationListener {
        void onConfirmation(Uri input);
    }
}
