package jajimenez.workpage;

import android.content.DialogInterface;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import jajimenez.workpage.data.model.Country;
import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.CountryComparator;
import jajimenez.workpage.logic.TimeZoneComparator;

public class TimeZonePickerDialogFragment extends DialogFragment {
    public static final int COUNTRY = 0;
    public static final int TIME_ZONE = 1;

    private Activity activity;
    private EditText countryEditText;
    private ImageButton clearImageButton;
    private ListView list;

    private int mode;
    private List<Country> countries;
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
        countries = applicationLogic.getAllCountries();
        selectedCountry = null;

        if (savedInstanceState == null) {
            mode = COUNTRY;
        }
        else {
            mode = savedInstanceState.getInt("mode", COUNTRY);
            long selectedCountryCode = savedInstanceState.getLong("selected_country", -1);

            if (selectedCountryCode > -1) selectedCountry = applicationLogic.getCountry(selectedCountryCode);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.time_zone_picker, null);

        countryEditText = (EditText) view.findViewById(R.id.time_zone_picker_country);
        clearImageButton = (ImageButton) view.findViewById(R.id.time_zone_picker_clear);
        list = (ListView) view.findViewById(R.id.time_zone_picker_list);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setTitle(R.string.select_time_zone);

        builder.setNeutralButton(R.string.local_time_zone, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TimeZone localTimeZone = (Calendar.getInstance()).getTimeZone();

                if (TimeZonePickerDialogFragment.this.onTimeZoneSelectedListener != null) {
                    TimeZonePickerDialogFragment.this.onTimeZoneSelectedListener.onTimeZoneSelected(localTimeZone);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

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

                    if (TimeZonePickerDialogFragment.this.onTimeZoneSelectedListener != null) {
                        TimeZonePickerDialogFragment.this.onTimeZoneSelectedListener.onTimeZoneSelected(selectedTimeZone);
                    }

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
            
            if (countryName.isEmpty()) {
                countries = new ArrayList<Country>();
            }
            else {
                countries = searchCountries(countryName);
                Collections.sort(countries, new CountryComparator());
            }
            
            CountryAdapter adapter = new CountryAdapter(activity, R.layout.country_list_item, countries);
            list.setAdapter(adapter);
        }
        else {
            List<TimeZone> timeZones = applicationLogic.getTimeZones(selectedCountry);
            Collections.sort(timeZones, new TimeZoneComparator());

            TimeZoneAdapter adapter = new TimeZoneAdapter(activity, R.layout.time_zone_list_item, timeZones);
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("mode", mode);
        if (selectedCountry != null) outState.putLong("selected_country", selectedCountry.getId());
    }

    private List<Country> searchCountries(String name) {
        List<Country> result = new ArrayList<Country>(countries.size());

        for (Country c : countries) {
            String name1 = (c.getName()).toLowerCase();
            String name2 = (name.trim()).toLowerCase();

            if (name1.contains(name2)) result.add(c);
        }

        return result;
    }

    public void setOnTimeZoneSelectedListener(OnTimeZoneSelectedListener listener) {
        onTimeZoneSelectedListener = listener;
    }

    public static interface OnTimeZoneSelectedListener {
        void onTimeZoneSelected(TimeZone t);
    }
}