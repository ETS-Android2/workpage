package jajimenez.workpage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import jajimenez.workpage.logic.ApplicationLogic;

public class SwitchInterfaceModeDialogFragment extends DialogFragment {
    private ApplicationLogic applicationLogic;

    private OnInterfaceModeChangedListener onInterfaceModeChangedListener;
    private int currentMode;

    public SwitchInterfaceModeDialogFragment() {
        onInterfaceModeChangedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        currentMode = applicationLogic.getInterfaceMode();

        int selectedItem = 0;
        if (currentMode == ApplicationLogic.INTERFACE_MODE_CALENDAR) selectedItem = 1;

        final int[] modeValues = new int[] {
                ApplicationLogic.INTERFACE_MODE_LIST,
                ApplicationLogic.INTERFACE_MODE_CALENDAR
        };

        String[] modeNames = new String[] {
                activity.getString(R.string.list),
                activity.getString(R.string.calendar)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(R.string.mode);
        builder.setNegativeButton(R.string.cancel, null);
        builder.setSingleChoiceItems(modeNames, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // "which" is the index position of the selected item.
                int selectedMode = modeValues[which];

                if (selectedMode != SwitchInterfaceModeDialogFragment.this.currentMode) {
                    SwitchInterfaceModeDialogFragment.this.applicationLogic.setInterfaceMode(selectedMode);

                    if (SwitchInterfaceModeDialogFragment.this.onInterfaceModeChangedListener != null) {
                        SwitchInterfaceModeDialogFragment.this.onInterfaceModeChangedListener.onModeChanged();
                    }
                }

                // Close the dialog.
                SwitchInterfaceModeDialogFragment.this.dismiss();
            }
        });

        return builder.create();
    }

    public void setOnInterfaceModeChangedListener(OnInterfaceModeChangedListener listener) {
        onInterfaceModeChangedListener = listener;
    }

    public interface OnInterfaceModeChangedListener {
        void onModeChanged();
    }
}
