package jajimenez.workpage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.widget.TimePicker;
import android.app.TimePickerDialog;

public class TimePickerDialogFragment extends DialogFragment {
    private OnTimeSetListener timeSetListener;
    private OnNoTimeSetListener noTimeSetListener;

    public TimePickerDialogFragment() {
        timeSetListener = null;
        noTimeSetListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        int hour = arguments.getInt("hour", 0);
        int minute = arguments.getInt("minute", 0);
        boolean includeNoTimeButton = arguments.getBoolean("include_no_time_button", false);

        LayoutInflater inflater = activity.getLayoutInflater();
        final TimePicker picker = (TimePicker) inflater.inflate(R.layout.time_picker, null);

        picker.setCurrentHour(hour);
        picker.setCurrentMinute(minute);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(picker);

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (TimePickerDialogFragment.this.timeSetListener != null) {
                    TimePickerDialogFragment.this.timeSetListener.onTimeSet(picker.getCurrentHour(), picker.getCurrentMinute());
                }
            }
        });

        if (includeNoTimeButton) {
            builder.setNeutralButton(R.string.no_time, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (TimePickerDialogFragment.this.noTimeSetListener != null) {
                        TimePickerDialogFragment.this.noTimeSetListener.onNoTimeSet();
                    }
                }
            });
        }

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    public void setOnTimeSetListener(OnTimeSetListener listener) {
        timeSetListener = listener;
    }

    public void setOnNoTimeSetListener(OnNoTimeSetListener listener) {
        noTimeSetListener = listener;
    }

    public static interface OnTimeSetListener {
        void onTimeSet(int hour, int minute);
    }

    public static interface OnNoTimeSetListener {
        void onNoTimeSet();
    }
}