package org.feup.apm.notredame.chart;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.feup.apm.notredame.Database.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BarChartManager {
    public static final int BAR_RADIUS = 20;
    public static final int TEXT_SIZE = 12;
    public static final String BAR_COLOR = "#606060";
    public static final String HIGHLIGHT_COLOR = "#64BD45";
    private int lastSelectedIndex = -1;

    // Initializes a chart
    public void initializeChart(BarChart barChart, boolean isWeek, List<BarEntry> barEntries, CustomBarChartRender barChartRender) {
        // Set up the X and Y axis based on the type of data (week or day)
        setupXAxis(barChart, isWeek);
        setupYAxis(barChart, barEntries, isWeek);

        // Customize the appearance of the chart
        customizeChartAppearance(barChart, barChartRender);

        // Create the data set and add it to the chart
        BarDataSet barDataSet = createBarDataSet(barEntries, isWeek);
        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);

        // Animate bars in daily chart
        if (!isWeek) {
            barChart.animateY(1500);
        }

        barChart.invalidate(); // Refresh the chart
    }

    // Helper methods to initialize a chart
    public void setupXAxis(BarChart barChart, boolean isWeek) {
        XAxis xAxis = barChart.getXAxis();

        // Set labels based on the X axis
        if (isWeek) {

            String[] weekLabels = new String[]{"S", "M", "T", "W", "T", "F", "S"};
            xAxis.setValueFormatter(new IndexAxisValueFormatter(weekLabels));
            xAxis.setGranularity(1f);
        }else {

            xAxis.setGranularity(5f);
        }

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setTextSize(TEXT_SIZE);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setupYAxis(BarChart barChart, List<BarEntry> barEntries, boolean isWeek) {
        YAxis yAxis = barChart.getAxisLeft();

        if (isWeek) { // Weekly view: Determine maximum value and calculate steps

            float max = 0;
            for (BarEntry entry : barEntries) {
                max = Math.max(max, entry.getY());
            }
            float maxValue = (float) Math.ceil(max / 2) * 2;

            yAxis.setAxisMaximum(maxValue);
            yAxis.setAxisMinimum(0);
            yAxis.setLabelCount(3, true);

            yAxis.setValueFormatter(new YAxisFormatterHours());
        } else { // Daily view: Fixed values (0, 30, 60)

            yAxis.setAxisMaximum(60);
            yAxis.setAxisMinimum(0);
            yAxis.setLabelCount(3, true);

            yAxis.setValueFormatter(new YAxisFormatterMins());
        }

        yAxis.setTextSize(TEXT_SIZE);
        yAxis.setTypeface(Typeface.DEFAULT_BOLD);
        yAxis.setAxisMinimum(0f);
        barChart.getAxisRight().setEnabled(false);
    }

    // Custom formatters for the Y axis
    private static class YAxisFormatterHours extends com.github.mikephil.charting.formatter.ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return (int) value + "h"; // Add "h" to the value
        }
    }

    private static class YAxisFormatterMins extends com.github.mikephil.charting.formatter.ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return (int) value + "min"; // Add "h" to the value
        }
    }

    // Set up of all the settings related to the appearance of the chart
    public void customizeChartAppearance(BarChart barChart, CustomBarChartRender barChartRender) {
        barChartRender.setRadius(BAR_RADIUS);
        barChart.setRenderer(barChartRender);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setExtraBottomOffset(10f);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDragEnabled(false);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
    }

    // Creates the data set to use on the chart
    public BarDataSet createBarDataSet(List<BarEntry> barEntries, boolean isWeek) {
        String label = isWeek ? "Weekly Data" : "Hourly Data";
        BarDataSet barDataSet = new BarDataSet(barEntries, label);
        barDataSet.setColor(Color.parseColor(BAR_COLOR));
        barDataSet.setHighLightColor(Color.parseColor(HIGHLIGHT_COLOR));
        barDataSet.setDrawValues(false);
        return barDataSet;
    }


    // Updates the data in both charts
    public void updateChartsWithSelectedDate(BarChart weekChart, BarChart dayChart, String date, TextView txtDate, TextView weeklyTime, TextView goodPosture_secondScreen, DatabaseHelper dbHelper) {

        txtDate.setText(date);

        List<BarEntry> weekBarEntries, dailyBarEntries;

        weekBarEntries = getWeeklyDataFromDB(dbHelper, date);

//         Logging for debug
//        if (weekBarEntries != null && !weekBarEntries.isEmpty()) {
//            for (BarEntry entry : weekBarEntries) {
//                Log.d("WeekBarEntries", "X: " + entry.getX() + ", Y: " + entry.getY());
//            }
//            Log.d("WeekBarEntries", "=============================== ");
//        } else {
//            Log.d("WeekBarEntries", "No entries retrieved from the database.");
//        }


        // Check if all weekly data entries are zero
        boolean allZero = true;
        for (BarEntry entry : weekBarEntries) {
            if (entry.getY() != 0) {
                allZero = false;
                break;
            }
        }

        // Show a toast and exit if all entries are zero
        if (allZero) {
            Toast toast = Toast.makeText(weekChart.getContext(), "No data was recorded that week.", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 100); // Adjust Y-offset as needed
            toast.show();
            return;
        }

        // Get daily data
        dailyBarEntries = getDailyDataFromDB(dbHelper, date);

        // Update the charts with the new data (listener included)
        initializeChart(weekChart, true, weekBarEntries, new CustomBarChartRender(weekChart, weekChart.getAnimator(), weekChart.getViewPortHandler()));
        initializeChart(dayChart, false, dailyBarEntries, new CustomBarChartRender(dayChart, dayChart.getAnimator(), dayChart.getViewPortHandler()));

        addChartValueListener(weekChart, dayChart, txtDate, weeklyTime, goodPosture_secondScreen, dbHelper);

        // Get the day of the week index from the selected date
        int selectedDayIndex = getDayOfWeekIndex(date);

        // Highlight the bar corresponding to the selected day in the week chart
        if (selectedDayIndex >= 0 && selectedDayIndex < 7) {
            weekChart.highlightValue(selectedDayIndex, 0);
        }
    }

    // Helper methods to update the data in the charts
    public List<BarEntry> getWeeklyDataFromDB(DatabaseHelper dbHelper, String Date) {
        return dbHelper.getWeeklyGoodPostureTime(Date);
    }

    public List<BarEntry> getDailyDataFromDB(DatabaseHelper dbHelper, String date) {
        return dbHelper.getHourlyGoodPostureTime(date);
    }

    private int getDayOfWeekIndex(String date) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try {
            calendar.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

    // Sets up the click listener on the weekly chart
    public void addChartValueListener(BarChart weekChart, BarChart dayChart, TextView txtDate, TextView weeklyTime, TextView goodPosture_secondScreen, DatabaseHelper dbHelper) {

            weekChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    if (e instanceof BarEntry) {

                        BarEntry barEntry = (BarEntry) e;
                        int selectedDayIndex = (int) barEntry.getX();


                        // Get the corresponding date for the selected day based on the current date in txtDate
                        String currentDate = txtDate.getText().toString();
                        String selectedDate = getDateForSelectedDay(selectedDayIndex, currentDate);

                        if (!selectedDate.equals(currentDate)) {
                            List<BarEntry> dailyBarEntries = getDailyDataFromDB(dbHelper, selectedDate);

                            // Update the daily chart with the new data
                            initializeChart(dayChart, false, dailyBarEntries, new CustomBarChartRender(dayChart, dayChart.getAnimator(), dayChart.getViewPortHandler()));

                            txtDate.setText(selectedDate);
                        }

                        // Updates the time of good posture for the selected day
                        float timeInHours = barEntry.getY();

                        int hours = (int) timeInHours;
                        int minutes = (int) ((timeInHours - hours) * 60);

                        String formattedTime = String.format(Locale.getDefault(), "%02dh %02dmin", hours, minutes);

                        weeklyTime.setText(formattedTime);


                        // Updates the time of good posture for the total week
                        float weeklyPosture = totalGoodPostureTime(dbHelper.getWeeklyGoodPostureTime(selectedDate));

                        int weeklyHours = (int) weeklyPosture;
                        int weeklyMinutes = (int) ((weeklyPosture - weeklyHours) * 60);

                        String weeklyPostureText = String.format("%02dh %02dmin (weekly)", weeklyHours, weeklyMinutes);

                        goodPosture_secondScreen.setText(weeklyPostureText);


                        // Makes sure that something stays selected all the time (the last bar that was selected)
                        lastSelectedIndex = (int) barEntry.getX() - 1;
                    }
                }

                @Override
                public void onNothingSelected() {

                    // Makes sure that something stays selected all the time (the last bar that was selected)
                    weekChart.highlightValue(lastSelectedIndex + 1, 0);
                }
            });

            // Disable highlights on day chart
            dayChart.setOnChartValueSelectedListener(null);
            dayChart.setTouchEnabled(false);
            dayChart.highlightValue(null);

    }

    // Helper methods for the chart listener
    private String getDateForSelectedDay(int selectedDayIndex, String currentDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Date date = sdf.parse(currentDate);

            Calendar calendar = Calendar.getInstance();
            if (date != null) {
                calendar.setTime(date);
            }

            int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            int daysDifference = selectedDayIndex - (currentDayOfWeek - 1);

            calendar.add(Calendar.DAY_OF_YEAR, daysDifference);

            return sdf.format(calendar.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return currentDate;
        }
    }

    public float totalGoodPostureTime(List<BarEntry> barEntries) {
        float sum = 0;
        for (BarEntry entry : barEntries) {
            sum += entry.getY();
        }

        return sum;
    }

}

