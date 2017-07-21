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
    private int borderColor;
    private int backgroundColor;

    private RectF borderRect;
    private Paint borderPaint;

    private RectF backgroundRect;
    private Paint backgroundPaint;

    public ColorView(Context context) {
        super(context);

        borderWidth = 0;

        borderRect = null;
        borderPaint = null;

        backgroundRect = null;
        backgroundPaint = null;
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        borderWidth = 0;

        borderRect = null;
        borderPaint = null;

        backgroundRect = null;
        backgroundPaint = null;

        TypedArray a = (context.getTheme()).obtainStyledAttributes(attrs, R.styleable.ColorView, 0, 0);

        try {
            borderWidth = a.getDimension(R.styleable.ColorView_borderWidth, 0);

            borderColor = a.getColor(R.styleable.ColorView_borderColor, 0xFFFFFFFF);
            borderPaint = setupBorderPaint(borderColor);

            backgroundColor = a.getColor(R.styleable.ColorView_backgroundColor, 0xFFFFFFFF);
            backgroundPaint = setupBackgroundPaint(backgroundColor);
        } finally {
            a.recycle();
        }
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public int getBorderColor() {
        return borderColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBorderWidth(float width) {
        borderWidth = width;

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    public void setBorderColor(int color) {
        borderColor = color;
        borderPaint = setupBorderPaint(color);

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        backgroundPaint = setupBackgroundPaint(color);

        // The following two methods must be called after any change
        // to the view properties that might change its appearance.
        invalidate();
        requestLayout();
    }

    private Paint setupBorderPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setColor(color);

        return paint;
    }

    private Paint setupBackgroundPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);

        return paint;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        float leftPadding = (float) getPaddingLeft();
        float rightPadding = (float) getPaddingRight();
        float topPadding = (float) getPaddingTop();
        float bottomPadding = (float) getPaddingBottom();

        backgroundRect = new RectF(leftPadding, topPadding, ((float) w) - rightPadding, ((float) h) - bottomPadding);

        if (borderWidth > 0) {
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