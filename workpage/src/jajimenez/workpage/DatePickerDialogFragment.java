package jajimenez.workpage;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;

public class DatePickerDialogFragment extends DialogFragment {
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener onDateSetListener;

    public DatePickerDialogFragment(Calendar calendar, DatePickerDialog.OnDateSetListener onDateSetListener) {
        super();

        this.calendar = calendar;
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
    }
}
