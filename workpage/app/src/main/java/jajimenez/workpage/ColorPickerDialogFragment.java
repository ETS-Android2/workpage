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
import android.widget.GridLayout;
import android.graphics.Color;

public class ColorPickerDialogFragment extends DialogFragment {
    //private int selectedColor;
    private OnColorSelectedListener onColorSelectedListener;

    public ColorPickerDialogFragment() {
        onColorSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        /*if (savedInstanceState != null) {
            //selectedColor = savedInstanceState.getInt("selected_color", 0xFFFFFFFF);

            //AdvancedColorPickerDialogFragment advancedColorPickerFragment = (AdvancedColorPickerDialogFragment) (getFragmentManager()).findFragmentByTag("advanced_color_picker");
            //if (advancedColorPickerFragment != null && onColorSelectedListener != null) advancedColorPickerFragment.setOnColorSelectedListener(onColorSelectedListener);
        }*/

        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.color_picker, null);

        GridLayout colorsGridLayout = (GridLayout) view.findViewById(R.id.color_picker_colors);
        int colorCount = colorsGridLayout.getChildCount();

        for (int i = 0; i < colorCount; i++) {
            ColorView c = (ColorView) colorsGridLayout.getChildAt(i);
            c.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    ColorView c = (ColorView) view;

                    if (ColorPickerDialogFragment.this.onColorSelectedListener != null) {
                        ColorPickerDialogFragment.this.onColorSelectedListener.onColorSelected(c.getBackgroundColor());
                    }

                    ColorPickerDialogFragment.this.dismiss();
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setTitle(getString(R.string.select_color));

        builder.setNeutralButton(R.string.other_color, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                AdvancedColorPickerDialogFragment fragment = new AdvancedColorPickerDialogFragment();
                fragment.setOnColorSelectedListener(ColorPickerDialogFragment.this.onColorSelectedListener);
                fragment.show(getFragmentManager(), "advanced_color_picker");
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_color", selectedColor);
    }*/

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        onColorSelectedListener = listener;
    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }
}