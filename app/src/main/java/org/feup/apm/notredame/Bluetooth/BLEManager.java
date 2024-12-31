package org.feup.apm.notredame.Bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Objects;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;

public class BLEManager extends BleManager {
    private static final String TAG = "Bluetooth";

    private static final UUID SERVICE_UUID = UUID.fromString("6f30b86e-bb28-48a6-b68d-4ae2e60e512a");
    private static final UUID DATA_UUID = UUID.fromString("14181dce-eb95-46c5-8431-3b4fe0e0a12d");
    private static final UUID BATTERY_UUID = UUID.fromString("d9c7628a-e98d-413d-b1a8-9e0fcb24b7e8");

    private BluetoothGattCharacteristic dataCharacteristic;
    private BluetoothGattCharacteristic batteryCharacteristic;

    public BLEManager(@NonNull Context context, Handler handler) {
        super(context);
        BluetoothDataParser.setHandler(handler);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyBleManagerGattCallback();
    }

    private class MyBleManagerGattCallback extends BleManagerGattCallback {

        // Searches for services
        @Override
        public boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {

//            // Log discovered services
//            for (BluetoothGattService service : gatt.getServices()) {
//                Log.d(TAG, "Service UUID: " + service.getUuid());
//                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
//                    Log.d(TAG, "  Characteristic UUID: " + characteristic.getUuid());
//                }
//            }

            // Searches and connects to the correct service
            BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                Log.d(TAG, "Service UUID: " + SERVICE_UUID);

                // Retrieves the notifications sent by the bluetooth service

                // Sensors characteristic
                dataCharacteristic = service.getCharacteristic(DATA_UUID);
                if (dataCharacteristic != null) {
                    Log.d(TAG, "  DATA UUID: " + DATA_UUID);
                    int properties = dataCharacteristic.getProperties();
                    Log.d(TAG, "  DATA properties: " + properties);

                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                        return false;
                    }
                }

                // Battery characteristic characteristic
                batteryCharacteristic = service.getCharacteristic(BATTERY_UUID);
                if (batteryCharacteristic != null) {
                    Log.d(TAG, "  BATTERY UUID: " + BATTERY_UUID);
                    int properties = batteryCharacteristic.getProperties();
                    Log.d(TAG, "  BATTERY properties: " + properties);

                    if ((properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                        return false;
                    }
                }

                return true;
            }
            return false;
        }

        @Override
        protected void initialize() {

            // Processes the sensors characteristic (parses it and sends it to the main activity)
            if (dataCharacteristic != null) {
                setNotificationCallback(dataCharacteristic)
                        .with((device, data) -> {
                            Log.d(TAG, "Notification received: " + data);
                            BluetoothDataParser.parseImuForceData(Objects.requireNonNull(data.getValue()));

                        });

                enableNotifications(dataCharacteristic)
                        .done(device -> Log.d(TAG, "Notifications enabled"))
                        .fail((device, status) -> Log.e(TAG, "Failed to enable notifications: " + status))
                        .enqueue();
            }

            // Processes the battery characteristic (parses it and sends it to the main activity)
            if (batteryCharacteristic != null) {
                setNotificationCallback(batteryCharacteristic)
                        .with((device, data) -> {
                            // Handle second characteristic data
                            BluetoothDataParser.parseImuBattery(Objects.requireNonNull(data.getValue()));
                        });

                enableNotifications(batteryCharacteristic)
                        .done(device -> Log.d(TAG, "Notifications enabled for second characteristic"))
                        .fail((device, status) -> Log.e(TAG, "Failed to enable notifications for second characteristic: " + status))
                        .enqueue();
            }
        }

        @Override
        protected void onServicesInvalidated() {
            Log.d(TAG, "Services invalidated");
        }
    }
}
