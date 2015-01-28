package jajimenez.workpage;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint;
import android.content.res.TypedArray;

public class ColorView extends View {
    private float borderWidth;
    private String borderColor;
    private String backgroundColor;

    private RectF borderRect;
    private Paint borderPaint;

    private RectF backgroundRect;
    private Paint backgroundPaint;

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        borderWidth = 0;
        borderColor = null;
        backgroundColor = null;

        borderRect = null;
        borderPaint = null;

        backgroundRect = null;
        backgroundPaint = null;

        TypedArray a = (context.getTheme()).obtainStyledAttributes(attrs, R.styleable.ColorView, 0, 0);

        try {
            borderWidth = a.getDimension(R.styleable.ColorView_borderWidth, 0);

            borderColor = a.getString(R.styleable.ColorView_borderColor);
            if (borderColor != null) borderPaint = setupBorderPaint(borderColor);

            backgroundColor = a.getString(R.styleable.ColorView_backgroundColor);
            if (backgroundColor != null) backgroundPaint = setupBackgroundPaint(backgroundColor);

        } finally {
            a.recycle();
        }
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public String getBorderColor() {
        return borderColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBorderWidth(float width) {
        borderWidth = width;

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    // Color must have an hexadecimal value, i.e. "#ffffff".
    public void setBorderColor(String color) {
        borderColor = color;
        borderPaint = setupBorderPaint(color);

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    // Color must have an hexadecimal value, i.e. "#ffffff".
    public void setBackgroundColor(String color) {
        backgroundColor = color;
        backgroundPaint = setupBackgroundPaint(color);

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    private Paint setupBorderPaint(String color) {
        Paint paint = setupPaint(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);

        return paint;
    }

    private Paint setupBackgroundPaint(String color) {
        Paint paint = setupPaint(color);
        paint.setStyle(Paint.Style.FILL);

        return paint;
    }

    private Paint setupPaint(String color) {
        int[] rgbColor = getRGBColor(color);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(255, rgbColor[0], rgbColor[1], rgbColor[2]);

        return paint;
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
        float leftPadding = (float) getPaddingLeft();
        float rightPadding = (float) getPaddingRight();
        float topPadding = (float) getPaddingTop();
        float bottomPadding = (float) getPaddingBottom();

        if (backgroundColor != null) {
            backgroundRect = new RectF(leftPadding, topPadding, ((float) w) - rightPadding, ((float) h) - bottomPadding);
        }

        if (borderWidth > 0 && borderColor != null) {
            borderRect = new RectF(leftPadding + (borderWidth/2), topPadding + (borderWidth/2), ((float) w) - rightPadding - (borderWidth/2), ((float) h) - bottomPadding - (borderWidth/2));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (backgroundRect != null && backgroundPaint != null) canvas.drawRect(backgroundRect, backgroundPaint);
        if (borderRect != null && borderPaint != null) canvas.drawRect(borderRect, borderPaint);
    }
}
