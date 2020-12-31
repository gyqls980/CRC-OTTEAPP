/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.otte.connection;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;


import com.example.otte.base.Constants;
import com.example.otte.sendData.ApiService;
import com.example.otte.sendData.ConnectionOTTE;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    // JWT Token from OTTE server
    String accessToken = "";

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Timer clickTimer = new Timer();
    private boolean isStartTimer = false;
    private int nClickAction = 0;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

//    public final static UUID UUID_HEART_RATE_MEASUREMENT =
//            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public BluetoothGattCharacteristic characteristic;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
            Log.i("BLEService", "onServicesDiscovered");
            setButtonNotification(true);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
//            byte[] value = characteristic.getValue();
//            String v = new String(value);
//            Toast.makeText(getApplicationContext(), "onCharacteristicRead", Toast.LENGTH_SHORT).show();
            Log.i("BLEService", "onCharacteristicRead");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            setCharacteristicNotification(characteristic, true);
            final byte[] dataInput = characteristic.getValue();
//            Log.i("BLEService", "onCharacteristicChanged : "+dataInput);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            if(inData.contains("00")){
                //intent.putExtra(EXTRA_DATA, "SingleClick");
//                ConnectionScene.actions("SingleClick");
//                String chooseColor = Constants.basicColor.substring(1);
//                String co = "11";
//                for(int i=2; i<8;i++){
//                    int a = 0xf - Integer.parseInt(String.valueOf(chooseColor.charAt(i)),16);
//                    co+=Integer.toHexString(a);
//                }
//                writeColorCharacteristic(co);
//                Log.i("BLEService", "writeColor");
            }
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    String inData = "";
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.d("BLEService", "broadcastUpdate");
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
//        if (SampleGattAttributes.SERVICE_COLOR_UUID.equals(characteristic.getUuid())) {
//            int flag = characteristic.getProperties();
//            int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
////            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//        } else {
//            // For all other profiles, writes the data formatted in HEX.
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for(byte byteChar : data)
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//            }
//        }


        // get access token from OTTE server
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://") // ADD EC2 URL ADDRESS
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService service1 = retrofit.create(ApiService.class);

        ConnectionOTTE token = new ConnectionOTTE("", ""); // ADD OTTE ID, PWD
        Call<ConnectionOTTE> call = service1.getToken(token);

        call.enqueue(new Callback<ConnectionOTTE>() {
            @Override
            public void onResponse(Call<ConnectionOTTE> call, Response<ConnectionOTTE> response) {
                if(response.isSuccessful()){
                    ConnectionOTTE result = response.body();
                    Log.d("getToken", response.body().toString());
                    // JWT Token은 "Bearer"를 추가
                    accessToken = "Bearer " + result.access;
                }
                else{
                    Log.d("getTokenFail", response.toString());
                }
            }

            @Override
            public void onFailure(Call<ConnectionOTTE> call, Throwable t) {
                Log.d("onFailure", t.getMessage());
            }
        });

        if(UUID.fromString(SampleGattAttributes.SERVICE_NOTI_DATA).equals(characteristic.getUuid())){
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                inData = stringBuilder.toString();
                Log.d(TAG, "Received: "+ inData);
            }

            // new test
            if(inData.contains("01")) {
                Constants.beforSignal ="01";
                if (Constants.beforSignal.contains("01")) {
                    long time = System.currentTimeMillis();
                    long elapsedtime = time - Constants.beforClickTime;

                    if (isStartTimer) { //default = false
                        nClickAction = 2;
                    } else {
                        isStartTimer = true;
                        nClickAction = 1;
                        clickTimer = new Timer();
                        TimerTask clickTimerTask = new TimerTask() {

                            @Override
                            public void run() {
                                if (nClickAction == 2) {
                                    Log.e("eleutheria", "DoubleClick");
                                    ConnectionScene.actions(accessToken, "DoubleClick");
                                } else {
                                    Log.e("eleutheria", "SingleClick");
                                    ConnectionScene.actions(accessToken, "SingleClick");
                                }
                                nClickAction = 0;
                                isStartTimer = false;
                                clickTimer.cancel();
                                clickTimer.purge();

                            }
                        };
                        clickTimer.schedule(clickTimerTask, 500);
                    }

                    Constants.beforClickTime = time;
                }

                Constants.beforSignal = "00";

                //다시 색 출력
                String chooseColor = Constants.basicColor.substring(1);
                String co = "11";
                for (int i = 2; i < 8; i++) {
                    int a = 0xf - Integer.parseInt(String.valueOf(chooseColor.charAt(i)), 16);
                    co += Integer.toHexString(a);
                }
                writeColorCharacteristic(co);
                Log.i("BLEService", "writeColor");
            }



//            if(inData.contains("01")){
//                    Constants.beforSignal ="01";
//            }if(inData.contains("02")){
//                Constants.beforSignal ="02";
//            }if(inData.contains("00")) {
//                if(Constants.beforSignal.contains("01")){
////                    ConnectionScene.actions("SingleClick");
//                    long time = System.currentTimeMillis();
//                    long elapsedtime = time-Constants.beforClickTime;
//
//                    if(isStartTimer) {
//                        nClickAction = 2;
//                    } else {
//                        isStartTimer = true;
//                        nClickAction = 1;
//                        clickTimer = new Timer();
//                        TimerTask clickTimerTask = new TimerTask() {
//
//                            @Override
//                            public void run() {
//                                if(nClickAction == 2) {
//                                    Log.e("eleutheria", "DoubleClick");
//                                    ConnectionScene.actions(accessToken, "DoubleClick");
//                                } else {
//                                    Log.e("eleutheria", "SingleClick");
//                                    ConnectionScene.actions(accessToken, "SingleClick");
//                                }
//                                nClickAction = 0;
//                                isStartTimer = false;
//                                clickTimer.cancel();
//                                clickTimer.purge();
//
//                            }
//                        };
//                        clickTimer.schedule(clickTimerTask,500);
//                    }
//
////                    Log.e("eleutheria", "time : %ld" + elapsedtime);
////                    if((time-Constants.beforClickTime)/1000.0 < 1){
////                        ConnectionScene.actions("DoubleClick");
////                    }else{
////                        ConnectionScene.actions("SingleClick");
////                    }
//                    Constants.beforClickTime=time;
//                }if(Constants.beforSignal.contains("02")){
//                    ConnectionScene.actions(accessToken, "Hold");
////                }if(Constants.beforSignal.contains("03")){
////                    ConnectionScene.actions("DoubleClick");
//                }
//
//                Constants.beforSignal ="00";
//
//                //다시 색 출력
//                String chooseColor = Constants.basicColor.substring(1);
//                String co = "11";
//                for(int i=2; i<8;i++){
//                    int a = 0xf - Integer.parseInt(String.valueOf(chooseColor.charAt(i)),16);
//                    co+= Integer.toHexString(a);
//                }
//                writeColorCharacteristic(co);
//                Log.i("BLEService", "writeColor");
//            }
            intent.putExtra(EXTRA_DATA, String.valueOf(inData));
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                ConnectionScene.mConnected = true;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        ConnectionScene.mConnected = true;
        return true;
    }
    private String color = "No Data";
    public void readColorCharacteristic(){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.SERVICE_COLOR));
        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        BluetoothGattCharacteristic mReadCharacteristic = mCustomService.getCharacteristic(UUID.fromString(SampleGattAttributes.SERVICE_COLOR_DATA));
        if(!mBluetoothGatt.readCharacteristic(mReadCharacteristic)){
            Log.w(TAG, "Failed to read characteristic");
        }else{
            final byte[] data = mReadCharacteristic.getValue();
            if(data != null && data.length >0){
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X", byteChar));

                color = stringBuilder.toString();
            }
        }
    }
    public String readColor(){
        readColorCharacteristic();
        return color;
    }
    public void writeColorCharacteristic(String value) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        /*check if the service is available on the device*/
        BluetoothGattService mCustomService = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.SERVICE_COLOR));
        if(mCustomService == null){
            Log.w(TAG, "Custom BLE Service not found");
            return;
        }
        /*get the read characteristic from the service*/
        BluetoothGattCharacteristic mWriteCharacteristic = mCustomService.getCharacteristic(UUID.fromString(SampleGattAttributes.SERVICE_COLOR_DATA));
        mWriteCharacteristic.setValue(new BigInteger(value, 16).toByteArray());
        if(!mBluetoothGatt.writeCharacteristic(mWriteCharacteristic)){
            Log.w(TAG, "Failed to write characteristic");
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public final static UUID UUID_SERVICE_NOTI =
            UUID.fromString(SampleGattAttributes.SERVICE_COLOR);
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }
    public void setButtonNotification(boolean enable){
        String uuidBCharacteristic = SampleGattAttributes.SERVICE_NOTI_DATA;

        BluetoothGattService mBluetoothLeService = null;
        BluetoothGattCharacteristic mBluetoothGattCharacteristic = null;
        for(BluetoothGattService service : mBluetoothGatt.getServices()){
            if((service==null)||(service.getUuid()==null)) continue;;
            if(SampleGattAttributes.SERVICE_COLOR.equalsIgnoreCase(service.getUuid().toString())){
                mBluetoothLeService = service;
            }
        }
        if(mBluetoothLeService!=null){
            mBluetoothGattCharacteristic = mBluetoothLeService.getCharacteristic(UUID.fromString(uuidBCharacteristic));
        }
        else{
            Log.i("BLEService","mBluetoothLeService is null");
        }
        if(mBluetoothGattCharacteristic!=null){
            setCharacteristicNotification(mBluetoothGattCharacteristic, enable);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
    public int getConntectionState(){
        return mConnectionState;
    }
}
