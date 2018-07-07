package com.example.root.grayson;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

public class ImageShapeContainer extends android.support.v7.widget.AppCompatImageView {

    public static float radius = 18.0f;
    private Bitmap bitmap;

    public ImageShapeContainer(Context context) {
        super(context);
    }

    public ImageShapeContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageShapeContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        @SuppressLint("DrawAllocation") Path clipPath = new Path();
        @SuppressLint("DrawAllocation") RectF rect = new RectF(
                0,
                0,
                this.getWidth(),
                this.getHeight());
        clipPath.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (bitmap == null) {
            createWindowFrame();
        }
        canvas.drawBitmap(
                bitmap,
                0,
                0,
                null);
    }

    protected void createWindowFrame() {
        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas osCanvas = new Canvas(bitmap);
        RectF outerRectangle = new RectF(
                0,
                0,
                 getWidth(),
                 getHeight());
        RectF innerRectangle = new RectF(
                34,
                34,
                getWidth() - 34,
                getHeight() - 34);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(getResources().getColor(R.color.colorPrimary, null));
        paint.setAlpha(144);
        // draw color rect
        osCanvas.drawRect(outerRectangle, paint);
        paint.setColor(Color.TRANSPARENT);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
        // draw inner transparent rect to create e hole
        osCanvas.drawRoundRect(innerRectangle, radius, radius, paint);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        bitmap = null;
    }


}