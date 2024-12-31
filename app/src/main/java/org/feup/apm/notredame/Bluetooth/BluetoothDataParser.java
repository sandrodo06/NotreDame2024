package org.feup.apm.notredame.Bluetooth;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothDataParser {
    private static final String TAG = "BTDataParser";

    private static final float ALPHA = 0.98f;
    private static float roll = 0.0f;
    private static float pitch = 0.0f;
    private static double overallAngle = 0.0f;
    private static long lastTimestamp = 0;

    private static Handler handler;

    // sets the handler to send data to the main activity
    public static void setHandler(Handler handler) {
        BluetoothDataParser.handler = handler;
    }

    // Parses the sensors data and sends it to the main activity
    public static void parseImuForceData(byte[] data) {

        // Makes sure it parses a whole characteristic
        if (data.length < 44) {
            Log.e(TAG, "Invalid data length: " + data.length);
            return;
        }

        // Updates the time stamps and calculates dt with each iterations
        long currentTimestamp = System.currentTimeMillis();

        // Calculate dt
        float dt = 0;
        if (lastTimestamp != 0) {
            dt = (currentTimestamp - lastTimestamp) / 1000.0f;
        }

        lastTimestamp = currentTimestamp;

        // Wrap the byte array and set the order to little-endian
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);


        // Process each 44-byte reading
        while (buffer.remaining() >= 44) {

            // REFERENTIAL ADJUSTMENT

            // Parse Accelerometer (bytes 0–11)
            float accY = -buffer.getFloat();
            float accZ = -buffer.getFloat();
            float accX = -buffer.getFloat();

            // Parse Gyroscope (bytes 12–23)
            float gyroY = -buffer.getFloat();
            float gyroZ = -buffer.getFloat();
            float gyroX = -buffer.getFloat();

            // Parse Magnetometer (bytes 24–35)
            float magY = -buffer.getFloat();
            float magZ = -buffer.getFloat();
            float magX = -buffer.getFloat();

            // Parse Force 1 (bytes 36–39)
            float force1 = buffer.getFloat();

            // Parse Force 2 (bytes 40–43)
            float force2 = buffer.getFloat();

            // Log the parsed data
            Log.d(TAG, "RAW DATA");
            Log.d(TAG, "Accelerometer: X=" + accX + ", Y=" + accY + ", Z=" + accZ);
            Log.d(TAG, "Gyroscope: X=" + gyroX + ", Y=" + gyroY + ", Z=" + gyroZ);
            Log.d(TAG, "Magnetometer: X=" + magX + ", Y=" + magY + ", Z=" + magZ);
            Log.d(TAG, "======================================");
            Log.d(TAG, "======================================");

            // =====================================================================================
            // Calculate Roll and Pitch

            // Calculate Roll and Pitch using complementary filter

            // 1. Calculate angles from the accelerometer
            float accelRoll = (float) Math.atan2(accY, accZ);
            float accelPitch = (float) Math.atan2(accX, Math.sqrt(accY * accY + accZ * accZ));

            // Convert roll and pitch to degrees
            accelRoll = (float) Math.toDegrees(accelRoll);
            accelPitch = (float) Math.toDegrees(accelPitch);

            // 2. Update roll and pitch using the complementary filter
            roll = ALPHA * (roll + gyroX * dt) + (1 - ALPHA) * accelRoll;
            pitch = ALPHA * (pitch + gyroY * dt) + (1 - ALPHA) * accelPitch;

            Log.d("EULER_ANGLES", "Roll (degrees): " + roll);
            Log.d("EULER_ANGLES", "Pitch (degrees): " + pitch);
            Log.d("EULER_ANGLES", "======================================");
            Log.d("EULER_ANGLES", "======================================");

            // =====================================================================================
            // Calculate overall angle
            overallAngle = Math.sqrt(roll * roll + pitch * pitch);

            Log.d("overallAngle", "overallAngle (degrees): " + overallAngle);
            Log.d("overallAngle", "======================================");
            Log.d("overallAngle", "======================================");

            // Send data to the main activity
            if (handler != null) {
                Message message = Message.obtain();
                message.what = 1; // Define a custom code for IMU data
                message.obj = new float[]{roll, pitch, (float) overallAngle};
                handler.sendMessage(message);
            }

        }

        // Check if there reading was incomplete
        if (buffer.remaining() > 0) {
            Log.w(TAG, "Incomplete reading at the end: " + buffer.remaining() + " bytes");
        }
    }

    // Parses the battery data and sends it to the main activity
    public static void parseImuBattery(byte[] data) {

        // Makes sure it parses a whole characteristic
        if (data.length == 0) {
            Log.e("parseImuBattery", "Received empty data");
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

        try {
            // Parse temperature (bytes 0–3)
            float temperature = buffer.getFloat();

            // Parse battery (bytes 4–7)
            float battery = buffer.getFloat();

            // Log the parsed values
            Log.d("parseImuBattery", "Parsed Values");
            Log.d("parseImuBattery", "Temperature: " + temperature);
            Log.d("parseImuBattery", "Battery: " + battery);
            Log.d("parseImuBattery", "======================================");

            // Send data to the main activity
            if (handler != null) {
                Message message = Message.obtain();
                message.what = 2; // Define a custom code for battery data
                message.obj = new float[]{battery};
                handler.sendMessage(message);
            }

        } catch (Exception e) {
            Log.e("parseImuBattery", "Error parsing data: " + e.getMessage());
        }
    }
}
