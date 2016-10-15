package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.GridLayout;
import android.graphics.Color;

public class ColorPickerDialogFragment extends DialogFragment {
    private GridLayout colorsGridLayout;

    private int selectedColor;
    private OnColorSelectedListener onColorSelectedListener;

    private Resources resources;

    /*private static final int RGB_R = 0;
    private static final int RGB_G = 1;
    private static final int RGB_B = 2;*/

    public ColorPickerDialogFragment() {
        //selectedColor = 0xFFFFFFFF;
        onColorSelectedListener = null;
    }

    /*public ColorPickerDialogFragment(int selectedColor) {
        this.selectedColor = selectedColor;
        onColorSelectedListener = null;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) selectedColor = savedInstanceState.getInt("selected_color", 0xFFFFFFFF);

        Activity activity = getActivity();
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.color_picker, null);

        colorsGridLayout = (GridLayout) view.findViewById(R.id.colorPicker_colors);
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
                    //selectedColor = c.getBackgroundColor();
                    //ColorPickerDialogFragment.this.updateInterface();
                }
            });
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setTitle(getString(R.string.select_color));

        /*builder.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (ColorPickerDialogFragment.this.onColorSelectedListener != null) {
                    ColorPickerDialogFragment.this.onColorSelectedListener.onColorSelected(ColorPickerDialogFragment.this.selectedColor);
                }
            }
        });*/

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_color", selectedColor);
    }

    /*private void updateInterface() {
        int colorCount = colorsGridLayout.getChildCount();

        for (int i = 0; i < colorCount; i++) {
            ColorView c = (ColorView) colorsGridLayout.getChildAt(i);
            int backgroundColor = c.getBackgroundColor();
            
            if (backgroundColor == selectedColor) {
                c.setBorderColor(Color.parseColor(resources.getString(R.color.color_view_border)));
            }
            else {
                c.setBorderColor(backgroundColor);
            }
        }
    }*/

    /*private int getRgbValue(int color, int component) {
        int value = 0;

        switch (component) {
            case RGB_R:
                value = (color >> 16) & 0xFF;
                break;

            case RGB_G:
                value = (color >> 8) & 0xFF;
                break;

            default:
                value = (color >> 0) & 0xFF;
        }

        return value;
    }*/

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        onColorSelectedListener = listener;
    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }
}
