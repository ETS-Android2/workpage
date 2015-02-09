package jajimenez.workpage;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class DatePickerDialogFragment extends DialogFragment {
    private DatePickerDialog dialog;
    private Calendar calendar;
    private DatePickerDialog.OnDateSetListener onDateSetListener;

    public DatePickerDialogFragment() {
        dialog = null;
        calendar = Calendar.getInstance();
        onDateSetListener = null;
    }

    public DatePickerDialogFragment(Calendar calendar, DatePickerDialog.OnDateSetListener onDateSetListener) {
        dialog = null;
        this.calendar = calendar;
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year = 0;
        int month = 0;
        int day = 0;

        if (savedInstanceState == null) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            year = savedInstanceState.getInt("year");
            month = savedInstanceState.getInt("month");
            day = savedInstanceState.getInt("day");
        }

        dialog = new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);

        return dialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        DatePicker picker = dialog.getDatePicker();

        outState.putInt("year", picker.getYear());
        outState.putInt("month", picker.getYear());
        outState.putInt("day", picker.getDayOfMonth());
    }
}
