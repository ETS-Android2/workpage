package jajimenez.workpage;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

public class DatePickerDialogFragment extends DialogFragment {
    private OnDateSetListener dateSetListener;
    private OnNoDateSetListener noDateSetListener;

    public DatePickerDialogFragment() {
        dateSetListener = null;
        noDateSetListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        Bundle arguments = getArguments();

        int year = arguments.getInt("year", 0);
        int month = arguments.getInt("month", 0);
        int day = arguments.getInt("day", 0);
        boolean includeNoDateButton = arguments.getBoolean("include_no_date_button", false);

        LayoutInflater inflater = activity.getLayoutInflater();
        final DatePicker picker = (DatePicker) inflater.inflate(R.layout.date_picker, null);

        picker.init(year, month, day, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(picker);

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (DatePickerDialogFragment.this.dateSetListener != null) {
                    DatePickerDialogFragment.this.dateSetListener.onDateSet(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
                }
            }
        });

        if (includeNoDateButton) {
            builder.setNeutralButton(R.string.no_date, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (DatePickerDialogFragment.this.noDateSetListener != null) {
                        DatePickerDialogFragment.this.noDateSetListener.onNoDateSet();
                    }
                }
            });
        }

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    public void setOnDateSetListener(OnDateSetListener listener) {
        dateSetListener = listener;
    }

    public void setOnNoDateSetListener(OnNoDateSetListener listener) {
        noDateSetListener = listener;
    }

    public static interface OnDateSetListener {
        void onDateSet(int year, int month, int day);
    }

    public static interface OnNoDateSetListener {
        void onNoDateSet();
    }
}