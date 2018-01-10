package jajimenez.workpage;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.view.View;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.text.Editable;
import android.text.TextWatcher;

import java.text.Normalizer;
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
    private Dialog dialog;
    private EditText countryEditText;
    private ImageButton clearImageButton;
    private ListView list;

    private int mode;
    private Calendar date;

    private List<Country> countries;
    private int countryCount;
    private ArrayList<String> countryNames;
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

        Bundle arguments = getArguments();

        date = Calendar.getInstance();
        long dateTime = arguments.getLong("date", date.getTimeInMillis());
        date.setTimeInMillis(dateTime);

        countries = applicationLogic.getAllCountries();
        Collections.sort(countries, new CountryComparator());

        countryCount = countries.size();
        countryNames = getNormalizedCountryNames(countries);

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

        countryEditText = view.findViewById(R.id.time_zone_picker_country);
        clearImageButton = view.findViewById(R.id.time_zone_picker_clear);
        list = view.findViewById(R.id.time_zone_picker_list);

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
                // Nothing to do
            }

            public void afterTextChanged(Editable s) {
                if (!((s.toString()).trim()).isEmpty()) {
                    TimeZonePickerDialogFragment.this.mode = TimeZonePickerDialogFragment.COUNTRY;
                }

                TimeZonePickerDialogFragment.this.updateInterface();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
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

        dialog = builder.create();
        (dialog.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        updateInterface();

        return dialog;
    }

    private ArrayList<String> getNormalizedCountryNames(List<Country> countries) {
        if (countries == null) throw new NullPointerException("'countries' is null.");
        ArrayList<String> result = new ArrayList<>(countries.size());

        for (Country c: countries) {
            String normalizedName = getNormalizedString(c.getName());
            result.add(normalizedName);
        }

        return result;
    }

    private String getNormalizedString(String s) {
        String result = "";

        if (s != null && !s.isEmpty()) {
            result = (s.trim()).toLowerCase();
            result = Normalizer.normalize(result, Normalizer.Form.NFD);
            result = result.replaceAll("\\p{M}", "");
        }

        return result;
    }

    private void updateInterface() {
        String countryName = ((countryEditText.getText()).toString()).trim();

        if (countryName.isEmpty()) clearImageButton.setVisibility(View.GONE);
        else clearImageButton.setVisibility(View.VISIBLE);

        if (mode == COUNTRY) {
            List<Country> countriesFound;
            
            if (countryName.isEmpty()) countriesFound = new ArrayList<>();
            else countriesFound = searchCountries(countryName);
            
            CountryAdapter adapter = new CountryAdapter(activity, R.layout.country_list_item, countriesFound);
            list.setAdapter(adapter);
        }
        else {
            hideKeyboard();

            List<TimeZone> timeZones = applicationLogic.getTimeZones(selectedCountry);
            Collections.sort(timeZones, new TimeZoneComparator());

            TimeZoneAdapter adapter = new TimeZoneAdapter(activity, R.layout.time_zone_list_item, timeZones);
            adapter.setDate(date);
            list.setAdapter(adapter);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("mode", mode);
        if (selectedCountry != null) outState.putLong("selected_country", selectedCountry.getId());

        super.onSaveInstanceState(outState);
    }

    private List<Country> searchCountries(String countryName) {
        List<Country> result = new ArrayList<>(countryNames.size());

        if (countryName != null && !countryName.isEmpty()) {
            String name2 = getNormalizedString(countryName);
            int length2 = name2.length();

            for (int i = 0; i < countryCount; i++) {
                String name1 = countryNames.get(i);

                if (length2 == 1) {
                    String firstLetter1 = name1.substring(0, 1);

                    if (firstLetter1.equals(name2)) {
                        Country c = countries.get(i);
                        result.add(c);
                    }
                } else if (name1.contains(name2)) {
                    Country c = countries.get(i);
                    result.add(c);
                }
            }
        }

        return result;
    }

    private void hideKeyboard() {
        View v = dialog.getCurrentFocus();

        if (v != null) {
            InputMethodManager m = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            m.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void setOnTimeZoneSelectedListener(OnTimeZoneSelectedListener listener) {
        onTimeZoneSelectedListener = listener;
    }

    public interface OnTimeZoneSelectedListener {
        void onTimeZoneSelected(TimeZone t);
    }
}
