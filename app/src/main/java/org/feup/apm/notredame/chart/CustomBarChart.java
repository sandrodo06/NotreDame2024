package org.feup.apm.notredame.chart;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.github.mikephil.charting.charts.BarChart;

public class CustomBarChart extends BarChart {
    private GestureDetector swipeDetector;

    public CustomBarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setSwipeDetector(GestureDetector swipeDetector) {
        this.swipeDetector = swipeDetector;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (swipeDetector != null) {
            swipeDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
}

