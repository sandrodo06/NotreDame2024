package org.feup.apm.notredame.main;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarEntry;

import androidx.activity.EdgeToEdge;
import androidx.core.app.ActivityCompat;

import org.feup.apm.notredame.Bluetooth.AngleData;
import org.feup.apm.notredame.Bluetooth.BLEManager;
import org.feup.apm.notredame.Bluetooth.BluetoothDataParser;
import org.feup.apm.notredame.Database.DatabaseHelper;
import org.feup.apm.notredame.PostureMonitor;
import org.feup.apm.notredame.R;
import org.feup.apm.notredame.chart.BarChartManager;
import org.feup.apm.notredame.chart.CustomBarChart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Queue;

public class MainActivity extends BaseActivity {

    private DatabaseHelper dbHelper;
    private BLEManager bleManager;
    private ViewFlipper viewFlipper;
    private GestureDetector swipeDetector;
    private BarChart barChartWeek, barChartDay;
    private BarChartManager barChartManager;
    private TextView overallAngleText, angleCaption, txtDate, weeklyTime, goodPosture_firstScreen, goodPosture_secondScreen, txtBattery;
    private ImageView angleBubble, noConnectionIcon, connectLed;
    private LinearLayout datePickerButton, deviceBtn, reconnectionBtn;
    private final ArrayList<AngleData> angleDataList = new ArrayList<>();
    private ScrollView scrollView;
    private final Queue<AngleData> angleBuffer = new LinkedList<>();
    private int sampleCount = 0;
    private PostureMonitor postureMonitor;


    private static final String TAG = "BT";
    private static final int SAMPLES_PER_SECOND = 10; // BLE sending rate
    private static final int BLOCK_DURATION_SECONDS = 10; // Block duration in seconds
    private static final int BUFFER_SIZE = SAMPLES_PER_SECOND * BLOCK_DURATION_SECONDS;
    private static final int ANGLE_LOWER_BOUND = 0;
    private static final int ANGLE_HIGHER_BOUND = 30;

    private static final String PREFERENCE_NAME = "app_preferences";
    private static final String VIBRATION_KEY = "vibration_enabled";



    // Handler that receives the bluetooth data, making it available on the main activity
    private final Handler dataHandler = new Handler(message -> {
        switch (message.what) {
            case 1: // IMU data
                float[] imuData = (float[]) message.obj;
                float roll = imuData[0];
                float pitch = imuData[1];
                double overallAngle = imuData[2];

                // Adds angle to the buffer so that it can be processed
                AngleData angleData = new AngleData(roll, pitch, overallAngle, this);
                angleDataList.add(angleData);
                addAngleToBuffer(angleData);

                Log.d("retrievedData", "Roll: " + roll + ", Pitch: " + pitch + ", Overall Angle: " + overallAngle);

                break;

            case 2: // Battery data
                float[] batteryData = (float[]) message.obj;
                float battery = batteryData[0];
                int batteryInt = (int) battery;

                saveBatteryPercentage(batteryInt); // Updates battery data in the device tab

                break;

            default:
                Log.w(TAG, "Unknown data received");
        }
        return true;
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Setup of the variables
        dbHelper = new DatabaseHelper(this);
        postureMonitor = new PostureMonitor(this);
        overallAngleText = findViewById(R.id.overallAngleText);
        angleCaption = findViewById(R.id.angleCaption);
        angleBubble = findViewById(R.id.angleBubble);
        noConnectionIcon = findViewById(R.id.noConnectionIcon);
        reconnectionBtn = findViewById(R.id.reconnectionBtn);
        deviceBtn = findViewById(R.id.DeviceBtn);
        connectLed = findViewById(R.id.connectLed);
        goodPosture_firstScreen = findViewById(R.id.goodPosture_firstScreen);
        goodPosture_secondScreen = findViewById(R.id.goodPosture_secondScreen);
        txtDate = findViewById(R.id.txtDate);
        weeklyTime = findViewById(R.id.weeklyTime);
        datePickerButton = findViewById(R.id.datePickerButton);

        // =========================================================================================
        // Toolbar setup
        setupNavigation();

        // =========================================================================================
        // Main page buttons

        deviceBtn.setOnClickListener(v -> {
                Intent intent = new Intent(this, DeviceActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
        });

        reconnectionBtn.setOnClickListener(v -> {
            // Tries to connect to bluetooth
            if (checkPermissions()) {
                startBleProcess();
            }
        });


        // =========================================================================================
        // Swipe down arrow setup

        ImageView swipeDownArrow = findViewById(R.id.swipeDownArrow);

        swipeDownArrow.setOnClickListener(v -> {

            // Show the second view
            viewFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
            viewFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
            viewFlipper.showNext();
        });

        // Arrow's animation
        ObjectAnimator animation = ObjectAnimator.ofFloat(swipeDownArrow, "translationY", 0f, 10f);
        animation.setDuration(150);
        animation.setRepeatMode(ObjectAnimator.REVERSE);
        animation.setRepeatCount(2);

        Handler handler = new Handler();

        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                // Wait for a bit, then restart the animation
                handler.postDelayed(() -> animation.start(), 2500);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animation.start();


        // =========================================================================================
        // Swipe down gesture to get the the second view of the ViewFlipper

        viewFlipper = findViewById(R.id.ViewFlipper);
        scrollView = findViewById(R.id.scrollView);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        swipeDetector = new GestureDetector(this, new SwipeListener());

        // Set touch listener
        viewFlipper.setOnTouchListener((v, event) -> {

            swipeDetector.onTouchEvent(event);
            return true;
        });

        // Set touch listener even in the ScrollView
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                swipeDetector.onTouchEvent(event);
                return false;
            }
        });


        // =========================================================================================
        // Bar chart setup

        // Weekly bar chart setup
        barChartWeek = findViewById(R.id.barChartWeek);
        ((CustomBarChart) barChartWeek).setSwipeDetector(swipeDetector);

        // Daily bar chart setup
        barChartDay = findViewById(R.id.barChartDay);
        ((CustomBarChart) barChartDay).setSwipeDetector(swipeDetector);

        // Initialize the charts with data
        barChartManager = new BarChartManager();

        barChartManager.updateChartsWithSelectedDate(barChartWeek, barChartDay, getCurrentDate(),txtDate, weeklyTime, goodPosture_secondScreen, dbHelper);

        // Button click listener to choose the date
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(barChartManager);
            }
        });

        // =========================================================================================
        // Statistics card setup

        // Get the total time with good posture that week
        float dailyPosture = totalGoodPostureTime(dbHelper.getHourlyGoodPostureTime(getCurrentDate()));

        int dailyHours = (int) (dailyPosture / 60);
        int dailyMinutes = (int) (dailyPosture % 60);

        String dailyPostureText = String.format("%02dh %02dmin", dailyHours, dailyMinutes);

        // Update the statistics card
        goodPosture_firstScreen.setText(dailyPostureText);


        // =========================================================================================
        // Starts bluetooth connections

        // Check and request permissions
        if (checkPermissions()) {
            connectLed.setColorFilter(getResources().getColor(R.color.blue));
            startBleProcess();
        }
    }

    // =========================================================================================
    // Sets up the swipe down/up gesture

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class SwipeListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();

            if (Math.abs(diffY) > Math.abs(diffX)) { // Ensure it's a vertical swipe
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY < 0) {

                        viewFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_bottom);
                        viewFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_top);
                        // Swipe Up
                        if (viewFlipper.getDisplayedChild() == 0) {
                            viewFlipper.showNext(); // Move to the second view
                        }
                    } else if (diffY > 0) {

                        viewFlipper.setInAnimation(MainActivity.this, R.anim.slide_in_top);
                        viewFlipper.setOutAnimation(MainActivity.this, R.anim.slide_out_bottom);
                        // Swipe Down
                        if (viewFlipper.getDisplayedChild() == 1) {
                            viewFlipper.showPrevious(); // Move back to the first view
                        }
                    }
                    return true;
                }
            }
            return false;
        }
    }

    // =========================================================================================
    // Bluetooth related methods

    // Checks and requests permissions
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                }, 1);

                return false;
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // For Android 6 to 11
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, 1);

                return false;
            }
        }
        return true;
    }

    // Handle the permission results
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            // if all the permissions are granted, start the bluetooth connection
            if (allGranted) {
                startBleProcess();
            } else {
                Log.e(TAG, "Permissions not granted");
            }
        }
    }

    // Starts the bluetooth connection
    private void startBleProcess() {
        connectLed.setColorFilter(getResources().getColor(R.color.blue)); // changes the connectLed state to blue (indicating pairing)

        Log.d(TAG, "startBleProcess called");
        String deviceAddress = "84:71:27:AC:A1:F5";

        BluetoothDevice device = getBluetoothDevice(deviceAddress);
        if (device != null) {
            bleManager = new BLEManager(this, dataHandler);
            bleManager.connect(device)
                    .retry(3, 100)
                    .done(connectedDevice -> {
                        Log.d(TAG, "Connected to device: " + connectedDevice.getAddress());

                        // Updates the connectLed
                        saveConnectionStatus(true);
                        updateConnectLed(true);
                    })
                    .fail((failedDevice, status) -> {
                        Log.e(TAG, "Failed to connect: " + status);

                        // Updates the connectedLed
                        saveConnectionStatus(false);
                        updateConnectLed(false);

                    })
                    .enqueue();
        } else {
            Log.e(TAG, "Bluetooth device not found");
        }
    }

    private BluetoothDevice getBluetoothDevice(String address) {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        return bluetoothManager.getAdapter().getRemoteDevice(address);
    }

    // Updates the connectedLed status and the battery state in the Device Activity

    private void updateConnectLed(boolean buttonisConnected) {
        if (buttonisConnected) {
            // Turn lime green
            connectLed.setColorFilter(getResources().getColor(R.color.lime_green));
        } else {
            // Turn red
            connectLed.setColorFilter(getResources().getColor(R.color.gray));
        }
    }

    private void saveConnectionStatus(boolean isConnected) {
        SharedPreferences sharedPreferences = getSharedPreferences("connection_status", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("device_connected", isConnected);
        editor.apply();
    }

    private void saveBatteryPercentage(int batteryper) {
        SharedPreferences sharedPreferences = getSharedPreferences("batteryPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("batteryPercentage", batteryper);
        editor.apply();

    }


    // =========================================================================================
    // Methods to process the bluetooth data

    // Adds the angle to the buffer so they can be processed
    private void addAngleToBuffer(AngleData angleData) {
        synchronized (angleBuffer) {
            if (angleBuffer.size() >= BUFFER_SIZE) { // If the buffer gets fulls, update it with a new sample
                angleBuffer.poll(); // Remove the oldest sample
            }
            angleBuffer.offer(angleData); // Add the new sample
            sampleCount++;

            if (sampleCount >= BUFFER_SIZE) {
                processBlock();
                sampleCount = 0; // Reset the sample count for the next block
            }
        }
    }

    // Processing each block of data
    private void processBlock() {
        double sum = 0;
        int count = 0;
        final int[] goodPostureTimeWrapper = {0};
        final int[] badPostureTimeWrapper = {0};
        final String[] dateWrapper = {null};
        final String[] timeWrapper = {null};

        // Calculate the mean of the angles in the buffer
        synchronized (angleBuffer) {

            // Get the date and time from the first element in the buffer
            AngleData firstAngleData = angleBuffer.peek();
            if (firstAngleData != null) {
                dateWrapper[0] = firstAngleData.getDate();
                timeWrapper[0] = firstAngleData.getTime();
            }

            while (!angleBuffer.isEmpty()) { // Update buffer if it's empty
                AngleData angleData = angleBuffer.poll();
                if(angleData != null){
                    sum += angleData.getOverallAngle();
                    count++;
                }
            }
        }

        // If there are samples in the buffer, process them
        if (count > 0) {

            // Calculates the mean angle in the buffer
            double meanAngle = sum / count;
            boolean isGoodPosture = meanAngle > ANGLE_LOWER_BOUND && meanAngle < ANGLE_HIGHER_BOUND; // good posture is defined within a range

            // Processess data for notifications
            SharedPreferences sharedPreferencesstate = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
            boolean alertState = sharedPreferencesstate.getBoolean(VIBRATION_KEY, true);

            if (!isGoodPosture && alertState) {
                // If bad posture, call PostureMonitor to track it and notify if necessary
                postureMonitor.addPostureData(true);
            }

            // Storing data in the database and displaying the angle to the user

            // Determine good or bad posture and increment the appropriate time counter
            if (isGoodPosture) {
                goodPostureTimeWrapper[0] += 10;
            } else {
                badPostureTimeWrapper[0] += 10;
            }

            // Update UI with the mean angle
            runOnUiThread(() -> {
                angleBubble.setVisibility(View.VISIBLE);
                noConnectionIcon.setVisibility(View.GONE);
                overallAngleText.setVisibility(View.VISIBLE);
                overallAngleText.setText(String.format("%.0fº", meanAngle));

                if(isGoodPosture){ // Posture is correct
                    angleBubble.setColorFilter(Color.parseColor("#64BD45"), android.graphics.PorterDuff.Mode.SRC_IN);
                    angleCaption.setText("All good");
                }
                else{ // Bad posture
                    angleBubble.setColorFilter(Color.parseColor("#F0505B"), android.graphics.PorterDuff.Mode.SRC_IN);
                    angleCaption.setText("Bad posture");
                }

                Log.d("BlockProcessing", "Mean Overall Angle: " + meanAngle);
            });


            // Update the database with the good/bad posture time
            if (dateWrapper[0] != null && timeWrapper[0] != null) {
                int hour = Integer.parseInt(timeWrapper[0].split(":")[0]); // Extract hour from time

                    dbHelper.updatePostureData(
                            dateWrapper[0],
                            hour,
                            goodPostureTimeWrapper[0],
                            badPostureTimeWrapper[0]
                    );
            }
        }
    }

    // =========================================================================================
    // Methods to select the date for the charts
    public void showDatePicker(BarChartManager barChartManager) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this, // Context (activity)
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        String selectedDate = String.format("%02d-%02d-%04d", dayOfMonth, monthOfYear + 1, year);

                        // Call method to update chart with selected date
                        barChartManager.updateChartsWithSelectedDate(barChartWeek, barChartDay, selectedDate, txtDate, weeklyTime, goodPosture_secondScreen, dbHelper);
                    }
                }, year, month, dayOfMonth
        );
        datePickerDialog.show();
    }

    // =========================================================================================
    // Other helper methods

    public String getCurrentDate(){
        long currentTimestamp = System.currentTimeMillis();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        return dateFormat.format(new Date(currentTimestamp));
    };

    public float totalGoodPostureTime(List<BarEntry> barEntries) {
        float sum = 0;
        for (BarEntry entry : barEntries) {
            sum += entry.getY();
        }
        return sum;
    }

}
