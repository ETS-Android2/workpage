package jajimenez.workpage;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.widget.TimePicker;
import android.app.TimePickerDialog;

public class TimePickerDialogFragment extends DialogFragment {
    private TimePickerDialog dialog;
    private Calendar calendar;
    private OnTimeSetListener onTimeSetListener;

    public TimePickerDialogFragment() {
        dialog = null;
        calendar = Calendar.getInstance();

        onTimeSetListener = null;
    }

    /*public TimePickerDialogFragment(Calendar calendar) {
        this.dialog = null;
        this.calendar = calendar;
        this.onTimeSetListener = null;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        long time = arguments.getLong("time");
        calendar.setTimeInMillis(time);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        dialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                // There is a bug in DatePickerDialog and TimePickerDialog
                // that makes always onDateSet/onTimeSet methods be
                // called twice (when the "Done" button is clicked and
                // when the dialog is dismissed). A workaround is to
                // check if the view is visible.
                if (view.isShown() && TimePickerDialogFragment.this.onTimeSetListener != null) {
                    TimePickerDialogFragment.this.onTimeSetListener.onTimeSet(hourOfDay, minute);
                }
            }
        
        }, hour, minute, true);

        return dialog;
    }

    public void setOnTimeSetListener(OnTimeSetListener listener) {
        onTimeSetListener = listener;
    }

    public static interface OnTimeSetListener {
        void onTimeSet(int hour, int minute);
    }
}