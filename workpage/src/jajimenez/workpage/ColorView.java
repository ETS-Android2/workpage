package jajimenez.workpage;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;

public class ColorView extends View {
    private String color;

    private int width;
    private int height;
    private Rect rect;
    private Paint paint;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        color = null;
        width = 0;
        height = 0;
        rect = null;
        paint = null;
    }

    public String getColor() {
        return color;
    }

    // Color must have an hexadecimal value, i.e. "#ffffff".
    public void setColor(String color) {
        this.color = color;
        
        int[] rgbColor = getRGBColor(color);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(255, rgbColor[0], rgbColor[1], rgbColor[2]);

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    private int[] getRGBColor(String hexColor) {
        int[] rgbColor = new int[3];

        // Red value.
        rgbColor[0] = Integer.valueOf(hexColor.substring(1, 3), 16);

        // Green value.
        rgbColor[1] = Integer.valueOf(hexColor.substring(3, 5), 16);

        // Blue value.
        rgbColor[2] = Integer.valueOf(hexColor.substring(5, 7), 16);

        return rgbColor;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        int xPadding = getPaddingLeft() + getPaddingRight();
        int yPadding = getPaddingTop() + getPaddingBottom();

        width = w - xPadding;
        height = h - yPadding;

        rect = new Rect(0, 0, width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rect != null && paint != null) canvas.drawRect(rect, paint);
    }
}
