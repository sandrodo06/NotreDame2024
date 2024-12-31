package org.feup.apm.notredame.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import org.feup.apm.notredame.R;

public class DeviceActivity extends BaseActivity {
    // Keys used for SharedPreferences to store variables
    private static final String PREFERENCE_NAME = "app_preferences";
    private static final String VIBRATION_KEY = "vibration_enabled";
    private static final String REPEAT_ALERTS_KEY = "repeat_alerts_enabled";

    private Switch vibrationSwitch, repeatAlertsSwitch; // Switch for vibration and repeat alerts
    private TextView ConnectionState;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_device);

        // Setup navigation toolbar
        setupNavigation();


        // Get the connection status of the device
        SharedPreferences sharedPreferencesConnect = getSharedPreferences("connection_status", MODE_PRIVATE);
        boolean isDeviceConnected = sharedPreferencesConnect.getBoolean("device_connected", false);

        // Get the battery percentage of the device
        SharedPreferences sharedPreferencesbattery = getSharedPreferences("batteryPreferences", MODE_PRIVATE);
        int batteryPercentage = sharedPreferencesbattery.getInt("batteryPercentage", 0); // Default to 0 if not found

        // obtain the expected battery
        float expectedbattery = (batteryPercentage / 100.0f) * 13; // Divide by 100.0 to use float division
        int expectedBatteryInt = Math.round(expectedbattery);
        String batterymessage = batteryPercentage + "% - Expected Battery: " + expectedBatteryInt + " hours";

        // Find the TextView for battery percentage and expected battery and update the text
        TextView BatterystatusTextView=findViewById(R.id.BatteryState);
        BatterystatusTextView.setText(batterymessage);

        // Find the TextView for connection status and update the text
        TextView connectionStatusTextView = findViewById(R.id.ConnectionState);

        if (isDeviceConnected) {
            connectionStatusTextView.setText("Device is connected");
        } else {
            connectionStatusTextView.setText("Device is not connected");
        }

        // Initialize notification switches
        vibrationSwitch = findViewById(R.id.vibrationSwitch2);
        repeatAlertsSwitch = findViewById(R.id.vibrationSwitch3);

        // Load the notification configuration for the sharedpreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        boolean isVibrationEnabled = sharedPreferences.getBoolean(VIBRATION_KEY, true);
        boolean isRepeatAlertsEnabled = sharedPreferences.getBoolean(REPEAT_ALERTS_KEY, false);

        // Set the initial state of switches
        vibrationSwitch.setChecked(isVibrationEnabled);
        repeatAlertsSwitch.setChecked(isRepeatAlertsEnabled);

        //  Listener for the first switch
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(VIBRATION_KEY, isChecked); // Store the vibration state
            editor.apply(); // Asynchronously apply the changes
        });

        // Listener for the second switch
        repeatAlertsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(REPEAT_ALERTS_KEY, isChecked); // Store the repeat alerts state
            editor.apply(); // Apply the changes
            Toast.makeText(this, isChecked ? "Repeat alerts activated" : "Repeat alerts deactivated", Toast.LENGTH_SHORT).show(); // Provide feedback to the user
        });



    }
}