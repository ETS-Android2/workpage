package jajimenez.workpage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DataImportConfirmationDialogFragment extends DialogFragment {
    private OnDataImportConfirmedListener onConfirmedListener;

    public DataImportConfirmationDialogFragment() {
        onConfirmedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.data_import_confirmation);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.import_data_1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DataImportConfirmationDialogFragment.this.onConfirmedListener != null) {
                    DataImportConfirmationDialogFragment.this.onConfirmedListener.onConfirmed();
                }
            }
        });

        return builder.create();
    }

    public void setOnDataImportConfirmedListener(OnDataImportConfirmedListener listener) {
        onConfirmedListener = listener;
    }

    public interface OnDataImportConfirmedListener {
        void onConfirmed();
    }
}
