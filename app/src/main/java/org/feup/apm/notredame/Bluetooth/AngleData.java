package org.feup.apm.notredame.Bluetooth;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AngleData {
    private float roll;
    private float pitch;
    private double overallAngle;
    private String date;
    private String time;

    public AngleData(float roll, float pitch, double overallAngle, Context context) {
        this.roll = roll;
        this.pitch = pitch;
        this.overallAngle = overallAngle;

        // Associate the angle with the respective date and time
        long currentTimestamp = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        this.date = dateFormat.format(new Date(currentTimestamp));
        this.time = timeFormat.format(new Date(currentTimestamp));
    }

    // Getters and setters
    public float getRoll() {
        return roll;
    }

    public float getPitch() {
        return pitch;
    }

    public double getOverallAngle() {
        return overallAngle;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setOverallAngle(double overallAngle) {
        this.overallAngle = overallAngle;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }
}