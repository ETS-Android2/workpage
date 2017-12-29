package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import jajimenez.workpage.logic.ApplicationLogic;

public class DateModeDialogFragment extends DialogFragment {
    private OnDateModeSetListener onDateModeSetListener;

    public DateModeDialogFragment() {
        onDateModeSetListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedItem = (getArguments()).getInt("mode", 0);

        final int[] mode_values = new int[] {
                ApplicationLogic.NO_DATE,
                ApplicationLogic.SINGLE_DATE,
                ApplicationLogic.DATE_RANGE
        };

        String[] mode_names = new String[] {
            getString(R.string.no_date),
            getString(R.string.single_date),
            getString(R.string.date_range)
        };

        Activity activity = getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.date_mode);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(mode_names, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DateModeDialogFragment.this.onDateModeSetListener != null) {
                    // "which" is the index position of the selected item.
                    DateModeDialogFragment.this.onDateModeSetListener.onDateModeSet(mode_values[which]);
                }

                // Close the dialog.
                DateModeDialogFragment.this.dismiss();
            }
        });

        return builder.create();
    }

    public void setOnDateModeSetListener(OnDateModeSetListener listener) {
        onDateModeSetListener = listener;
    }

    public interface OnDateModeSetListener {
        void onDateModeSet(int mode);
    }
}
