package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.Color;

public class AdvancedColorPickerDialogFragment extends DialogFragment {
    private ColorView selectedColorView;
    private EditText redEditText;
    private EditText greenEditText;
    private EditText blueEditText;

    private ColorPickerDialogFragment.OnColorSelectedListener onColorSelectedListener;

    public AdvancedColorPickerDialogFragment() {
        onColorSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.advanced_color_picker, null);

        selectedColorView = view.findViewById(R.id.advanced_color_picker_color);

        if (savedInstanceState != null) selectedColorView.setBackgroundColor(savedInstanceState.getInt("selected_color", 0xFFFFFFFF));

        redEditText = view.findViewById(R.id.advanced_color_picker_red);
        greenEditText = view.findViewById(R.id.advanced_color_picker_green);
        blueEditText = view.findViewById(R.id.advanced_color_picker_blue);

        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }

            public void afterTextChanged(Editable s) {
                AdvancedColorPickerDialogFragment.this.updateInterface();
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do
            }
        };

        // Events
        redEditText.addTextChangedListener(w);
        greenEditText.addTextChangedListener(w);
        blueEditText.addTextChangedListener(w);

        // Default values
        redEditText.setText(R.string.rgb_default);
        greenEditText.setText(R.string.rgb_default);
        blueEditText.setText(R.string.rgb_default);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setTitle(getString(R.string.select_color));

        builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (AdvancedColorPickerDialogFragment.this.onColorSelectedListener != null) {
                    AdvancedColorPickerDialogFragment.this.onColorSelectedListener.onColorSelected(AdvancedColorPickerDialogFragment.this.selectedColorView.getBackgroundColor());
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_color", selectedColorView.getBackgroundColor());
    }

    private void updateInterface() {
        String r1 = ((redEditText.getText()).toString()).trim();
        String g1 = ((greenEditText.getText()).toString()).trim();
        String b1 = ((blueEditText.getText()).toString()).trim();

        int r2 = 0;
        int g2 = 0;
        int b2 = 0;

        try {
            if (!r1.equals("")) r2 = Integer.parseInt(r1);
            if (r2 < 0 || r2 > 255) r2 = 0;
        }
        catch (NumberFormatException e) {
            // Do nothing.
        }

        try {
            if (!g1.equals("")) g2 = Integer.parseInt(g1);
            if (g2 < 0 || g2 > 255) g2 = 0;
        }
        catch (NumberFormatException e) {
            // Do nothing.
        }
        
        try {
            if (!b1.equals("")) b2 = Integer.parseInt(b1);
            if (b2 < 0 || b2 > 255) b2 = 0;
        }
        catch (NumberFormatException e) {
            // Do nothing.
        }

        selectedColorView.setBackgroundColor(Color.rgb(r2, g2, b2));
    }

    public void setOnColorSelectedListener(ColorPickerDialogFragment.OnColorSelectedListener listener) {
        onColorSelectedListener = listener;
    }
}
