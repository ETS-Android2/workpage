package jajimenez.workpage;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.GridLayout;

public class ColorPickerDialogFragment extends DialogFragment {
    private OnColorSelectedListener onColorSelectedListener;
    private OnNoColorSelectedListener onNoColorSelectedListener;

    public ColorPickerDialogFragment() {
        onColorSelectedListener = null;
        onNoColorSelectedListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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

        builder.setPositiveButton(R.string.no_color, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (ColorPickerDialogFragment.this.onNoColorSelectedListener != null) {
                    ColorPickerDialogFragment.this.onNoColorSelectedListener.onNoColorSelected();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        onColorSelectedListener = listener;
    }

    public void setOnNoColorSelectedListener(OnNoColorSelectedListener listener) {
        onNoColorSelectedListener = listener;
    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }

    public interface OnNoColorSelectedListener {
        public void onNoColorSelected();
    }
}