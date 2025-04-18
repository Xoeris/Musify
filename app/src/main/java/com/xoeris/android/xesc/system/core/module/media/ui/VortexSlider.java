package com.xoeris.android.xesc.system.core.module.media.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.internal.view.SupportMenu;
import com.xoeris.android.musify.R;

@SuppressWarnings("all")
public class VortexSlider extends View {
    private int max;
    private OnSeekBarChangeListener onSeekBarChangeListener;
    private float progress;
    private int progressColor;
    private Paint progressPaint;
    private int thumbColor;
    private int thumbCornerRadius;
    private Paint thumbPaint;
    private int thumbRadius;
    private int trackColor;
    private int trackHeight;
    private Paint trackPaint;

    public interface OnSeekBarChangeListener {
        void onProgressChanged(VortexSlider vortexSlider, int i, boolean z);

        void onStartTrackingTouch(VortexSlider vortexSlider);

        void onStopTrackingTouch(VortexSlider vortexSlider);
    }

    @SuppressLint("RestrictedApi")
    public VortexSlider(Context context) {
        super(context);
        this.max = 100;
        this.progress = 0;
        this.trackColor = -7829368;
        this.progressColor = -16776961;
        this.thumbColor = SupportMenu.CATEGORY_MASK;
        this.trackHeight = 10;
        this.thumbRadius = 20;
        this.thumbCornerRadius = 25;
        init(null);
    }

    @SuppressLint("RestrictedApi")
    public VortexSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.max = 100;
        this.progress = 0;
        this.trackColor = -7829368;
        this.progressColor = -16776961;
        this.thumbColor = SupportMenu.CATEGORY_MASK;
        this.trackHeight = 10;
        this.thumbRadius = 20;
        this.thumbCornerRadius = 25;
        init(attrs);
    }

    @SuppressLint("RestrictedApi")
    public VortexSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.max = 100;
        this.progress = 0;
        this.trackColor = -7829368;
        this.progressColor = -16776961;
        this.thumbColor = SupportMenu.CATEGORY_MASK;
        this.trackHeight = 10;
        this.thumbRadius = 20;
        this.thumbCornerRadius = 25;
        init(attrs);
    }

    @SuppressLint("RestrictedApi")
    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SeekBarView);
            this.max = a.getInteger(R.styleable.SeekBarView_max, 100);
            this.progress = a.getInteger(R.styleable.SeekBarView_progress, 0);
            this.trackColor = a.getColor(R.styleable.SeekBarView_trackColor, -7829368);
            this.progressColor = a.getColor(R.styleable.SeekBarView_progressColor, -16776961);
            this.thumbColor = a.getColor(R.styleable.SeekBarView_thumbColor, SupportMenu.CATEGORY_MASK);
            this.trackHeight = a.getDimensionPixelSize(R.styleable.SeekBarView_trackHeight, this.trackHeight);
            this.thumbRadius = a.getDimensionPixelSize(R.styleable.SeekBarView_thumbRadius, this.thumbRadius);
            a.recycle();
        }
        this.trackPaint = new Paint(1);
        this.trackPaint.setColor(this.trackColor);
        this.trackPaint.setStrokeWidth(this.trackHeight);
        this.progressPaint = new Paint(1);
        this.progressPaint.setColor(this.progressColor);
        this.progressPaint.setStrokeWidth(this.trackHeight);
        this.thumbPaint = new Paint(1);
        this.thumbPaint.setColor(this.thumbColor);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int centerY = getHeight() / 2;
        canvas.drawLine(this.thumbRadius, centerY, width - this.thumbRadius, centerY, this.trackPaint);
        float progressWidth = (this.progress / this.max) * (width - (this.thumbRadius * 2));
        canvas.drawLine(this.thumbRadius, centerY, this.thumbRadius + progressWidth, centerY, this.progressPaint);
        float thumbLeft = (this.thumbRadius + progressWidth) - this.thumbRadius;
        float thumbTop = centerY - this.thumbRadius;
        float thumbRight = thumbLeft + (this.thumbRadius * 2);
        float thumbBottom = this.thumbRadius + centerY;
        canvas.drawRoundRect(thumbLeft, thumbTop, thumbRight, thumbBottom, this.thumbCornerRadius, this.thumbCornerRadius, this.thumbPaint);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        int width = getWidth();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.onSeekBarChangeListener != null) {
                    this.onSeekBarChangeListener.onStartTrackingTouch(this);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (this.onSeekBarChangeListener != null) {
                    this.onSeekBarChangeListener.onStopTrackingTouch(this);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                break;
            default:
                return super.onTouchEvent(event);
        }
        float x = event.getX();
        this.progress = ((Math.max(this.thumbRadius, Math.min(x, width - this.thumbRadius)) - this.thumbRadius) / (width - (this.thumbRadius * 2))) * this.max;
        if (this.onSeekBarChangeListener != null) {
            this.onSeekBarChangeListener.onProgressChanged(this, (int)this.progress, true);
        }
        invalidate();
        return true;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        this.onSeekBarChangeListener = listener;
    }

    public int getProgress() {
        return (int)this.progress;
    }

    public void setProgress(float progress) {
        this.progress = Math.min(progress, this.max);
        invalidate();
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }
}
