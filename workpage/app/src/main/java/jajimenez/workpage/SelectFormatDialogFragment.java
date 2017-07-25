package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import jajimenez.workpage.logic.ApplicationLogic;

public class SelectFormatDialogFragment extends DialogFragment {
    private int selectedFormat;
    private OnNewFormatSelectedListener onNewFormatSelectedListener;

    public SelectFormatDialogFragment() {
        selectedFormat = ApplicationLogic.WORKPAGE_DATA;
        onNewFormatSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        selectedFormat = (getArguments()).getInt("selected_format", ApplicationLogic.WORKPAGE_DATA);

        Activity activity = getActivity();

        String[] formats = new String[2];
        formats[0] = activity.getString(R.string.workpage_data);
        formats[1] = activity.getString(R.string.csv);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.select_format);
        builder.setNegativeButton(R.string.cancel, null);

        builder.setSingleChoiceItems(formats, selectedFormat, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which != SelectFormatDialogFragment.this.selectedFormat
                    && SelectFormatDialogFragment.this.onNewFormatSelectedListener != null) {

                    SelectFormatDialogFragment.this.onNewFormatSelectedListener.onNewFormatSelected(which);
                }

                // Close the dialog.
                SelectFormatDialogFragment.this.dismiss();
            }
        });

        return builder.create();
    }

    public void setOnNewFormatSelectedListener(OnNewFormatSelectedListener listener) {
        onNewFormatSelectedListener = listener;
    }

    public static interface OnNewFormatSelectedListener {
        void onNewFormatSelected(int format);
    }
}