package com.example.root.grayson.dateTime;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.root.grayson.R;

public class DateTimeDraw extends View {
    public Typeface mTypeface;
    public Paint mBackTextDate;
    public Paint mBackground;
    public Paint mBackText;
    public Paint mText;
    public int mHours;
    public int mMinutes;
    public int mSeconds;
    public int mWeekDay;
    public int mDate;

    public DateTimeDraw(Context context) {
        this(context, null);
    }

    public DateTimeDraw(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public DateTimeDraw(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @SuppressLint("DefaultLocale")
    public void setParameter(int hours,
                             int minutes,
                             int seconds,
                             int weekDay,
                             int date) {

        this.mHours = hours;
        this.mMinutes = minutes;
        this.mSeconds = seconds;
        this.mWeekDay = weekDay;
        this.mDate = date;

        invalidate();
    }
    //
    private void init() {
        Resources myResources = getResources();
        mTypeface = Typeface.createFromAsset(myResources.getAssets(),
                "fonts/pacifico_regular.ttf");
        mBackground = new Paint();
        mBackground.setColor(getResources().getColor(R.color.transparent, null));

        mText = new Paint();
        mText.setColor(getResources().getColor(R.color.colorText, null));
        mText.setTextAlign(Paint.Align.CENTER);
        mText.setTextSize(myResources.getDimension(R.dimen.clock_text_size));
        mText.setAntiAlias(true);
        mText.setTypeface(mTypeface);

        mBackText = new Paint();
        mBackText.setColor(myResources.getColor(R.color.colorTextSec,null));
        mBackText.setTextAlign(Paint.Align.CENTER);
        mBackText.setTextSize(myResources.getDimension(R.dimen.clock_text_size));
        mBackText.setAntiAlias(true);
        mBackText.setTypeface(mTypeface);

        mBackTextDate = new Paint();
        mBackTextDate.setColor(myResources.getColor(R.color.colorTextSec, null));
        mBackTextDate.setTextAlign(Paint.Align.CENTER);
        mBackTextDate.setTextSize(myResources.getDimension(R.dimen.date_text));
        mBackTextDate.setAntiAlias(true);
        mBackTextDate.setTypeface(mTypeface);
    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // get container dimensions
        float width = getWidth();
        float height = getHeight();

        //draw transparent background
        canvas.drawRect(
                0,
                0,
                this.getWidth(),
                this.getHeight(),
                mBackground);

        float centerX = width / 2f;
        float centerY = height / 2f;

        //setup timer
        int current_hour = mHours;
        String currentAmPm = "am";

        if (current_hour == 0) {
            current_hour = 12;
        }
        if (current_hour > 12) {
            current_hour = current_hour - 12;
            currentAmPm = "pm";
        }

        //time text to be draw
        @SuppressLint("DefaultLocale") String timeText = String.format("%02d:%02d:%02d "
                + currentAmPm, current_hour, mMinutes, mSeconds);
        //time shadow text to be draw
        @SuppressLint("DefaultLocale") String shadowText = String.format("%02d:%02d:%02d "
                + currentAmPm, current_hour, mMinutes, mSeconds);
        //setup days
        String weekOfDay = "";
        switch (mWeekDay) {
            case 1:
                weekOfDay = "mon";
                break;
            case 2:
                weekOfDay = "tue";
                break;
            case 3:
                weekOfDay = "wed";
                break;
            case 4:
                weekOfDay = "thu";
                break;
            case 5:
                weekOfDay = "fry";
                break;
            case 6:
                weekOfDay = "sat";
                break;
            case 7:
                weekOfDay = "sun";
                break;
        }
        // date text to be draw
        @SuppressLint("DefaultLocale") String dateText =
                String.format("Date: %S %d", weekOfDay, mDate);

        // date shadow text to be draw
        @SuppressLint("DefaultLocale") String shadowDate =
                String.format("Date: %S %d", weekOfDay, mDate);

        // draw shadow time text
        canvas.drawText(shadowText, centerX+5, centerY+5, mBackText);
        // set new text attributes
        mText.setColor(ContextCompat.getColor(getContext(), R.color.colorText));
        mText.setTextSize(getContext().getResources().getDimension(R.dimen.clock_text_small));
        // draw time text
        canvas.drawText(timeText, centerX, centerY, mText);
        //draw shadow date text
        canvas.drawText(shadowDate, centerX+3, centerY+24 + getContext()
                .getResources().getDimension(R.dimen.date_text), mBackTextDate);
        // set new text attributes
        mText.setColor(ContextCompat.getColor(getContext(), R.color.colorText));
        mText.setTextSize(getContext().getResources().getDimension(R.dimen.date_text));
        //draw date text
        canvas.drawText(dateText, centerX, centerY+21 + getContext()
                .getResources().getDimension(R.dimen.date_text), mText);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }
}
