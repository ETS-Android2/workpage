package jajimenez.workpage;

import android.os.Bundle;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

public class ColorPickerDialogFragment extends DialogFragment {
    private ColorPickerDialog colorDialog;
    private int initialColor;
    private OnColorSelectedListener onColorSelectedListener;

    public ColorPickerDialogFragment() {
        initialColor = 0xFFFFFFFF;
        onColorSelectedListener = null;
    }

    public ColorPickerDialogFragment(int initialColor) {
        this.initialColor = initialColor;
        onColorSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) initialColor = savedInstanceState.getInt("initial_color", 0xFFFFFFFF);

        colorDialog = new ColorPickerDialog(getActivity(), initialColor);
        colorDialog.setTitle(getString(R.string.select_color));

        colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.accept), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ColorPickerDialogFragment.this.onColorSelectedListener != null) {
                    ColorPickerDialogFragment.this.onColorSelectedListener.onColorSelected(ColorPickerDialogFragment.this.colorDialog.getColor());
                }
            }
        });

        colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing.
            }
        });

        if (savedInstanceState != null) {
            int currentColor = savedInstanceState.getInt("current_color", 0xFFFFFFFF);
            colorDialog.setColor(currentColor);
        }

        return colorDialog;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("initial_color", initialColor);
        outState.putInt("current_color", colorDialog.getColor());
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        onColorSelectedListener = listener;
    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }
}
