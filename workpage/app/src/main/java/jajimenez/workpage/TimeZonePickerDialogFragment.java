package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.text.Editable;
import android.text.TextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.logic.ApplicationLogic;

public class TimeZonePickerDialogFragment extends DialogFragment {
    public static final int COUNTRY = 0;
    public static final int TIME_ZONE = 1;

    private Activity activity;
    private EditText countryEditText;
    private ImageButton clearImageButton;
    private ListView list;

    private int mode;
    private Country selectedCountry;

    private OnTimeZoneSelectedListener onTimeZoneSelectedListener;

    private ApplicationLogic applicationLogic;

    public TimeZonePickerDialogFragment() {
        onTimeZoneSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);

        if (savedInstanceState == null) mode = COUNTRY;
        else mode = savedInstanceState.getInt("mode", COUNTRY);

        selectedCountry = null;

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.time_zone_picker, null);

        countryEditText = (EditText) view.findViewById(R.id.time_zone_picker_country);
        clearImageButton = (ImageButton) view.findViewById(R.id.time_zone_picker_clear);
        list = (ListView) view.findViewById(R.id.time_zone_picker_list);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);

        countryEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                if (!((s.toString()).trim()).isEmpty()) {
                    TimeZonePickerDialogFragment.this.mode = TimeZonePickerDialogFragment.this.COUNTRY;
                }

                TimeZonePickerDialogFragment.this.updateInterface();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        clearImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                countryEditText.setText("");
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView l, View v, int position, long id) {
                if (TimeZonePickerDialogFragment.this.mode == COUNTRY) {
                    TimeZonePickerDialogFragment.this.selectedCountry = (Country) l.getItemAtPosition(position);
                    TimeZonePickerDialogFragment.this.mode = TIME_ZONE;
                    TimeZonePickerDialogFragment.this.countryEditText.setText("");

                    TimeZonePickerDialogFragment.this.updateInterface();
                }
                else {
                    TimeZone selectedTimeZone = (TimeZone) l.getItemAtPosition(position);
                    if (onTimeZoneSelectedListener != null) onTimeZoneSelectedListener.onTimeZoneSelected(selectedTimeZone);

                    TimeZonePickerDialogFragment.this.dismiss();
                }
            }
        });

        updateInterface();

        return builder.create();
    }

    private void updateInterface() {
        String countryName = ((countryEditText.getText()).toString()).trim();

        if (countryName.isEmpty()) clearImageButton.setVisibility(View.GONE);
        else clearImageButton.setVisibility(View.VISIBLE);

        if (mode == COUNTRY) {
            List<Country> countries;
            
            if (countryName.isEmpty()) countries = new ArrayList<Country>();
            else countries = applicationLogic.searchCountries(countryName);
            
            CountryAdapter adapter = new CountryAdapter(activity, R.layout.country_list_item, countries);
            list.setAdapter(adapter);
        }
        else {
            TimeZoneAdapter adapter = new TimeZoneAdapter(activity, R.layout.time_zone_list_item, applicationLogic.getTimeZones(selectedCountry));
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mode", mode);
    }

    public void setOnTimeZoneSelectedListener(OnTimeZoneSelectedListener listener) {
        onTimeZoneSelectedListener = listener;
    }

    public static interface OnTimeZoneSelectedListener {
        void onTimeZoneSelected(TimeZone t);
    }
}