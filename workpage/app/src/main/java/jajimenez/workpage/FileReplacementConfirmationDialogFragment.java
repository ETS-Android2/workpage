package jajimenez.workpage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

public class FileReplacementConfirmationDialogFragment extends DialogFragment {
    private Uri output;
    private OnFileReplacementConfirmationListener onConfirmationListener;

    public FileReplacementConfirmationDialogFragment() {
        onConfirmationListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String outputStr = (getArguments()).getString("output_uri");
        output = Uri.parse(outputStr);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.file_replacement_confirmation);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setPositiveButton(R.string.replace, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (FileReplacementConfirmationDialogFragment.this.output != null &&
                    FileReplacementConfirmationDialogFragment.this.onConfirmationListener != null) {
                    FileReplacementConfirmationDialogFragment.this.onConfirmationListener.onConfirmed(output);
                }
            }
        });

        return builder.create();
    }

    public void setOnFileReplacementConfirmationListener(OnFileReplacementConfirmationListener listener) {
        onConfirmationListener = listener;
    }

    public interface OnFileReplacementConfirmationListener {
        void onConfirmed(Uri output);
    }
}
