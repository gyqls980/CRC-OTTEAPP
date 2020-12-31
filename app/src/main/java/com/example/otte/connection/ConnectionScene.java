package com.example.otte.connection;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.otte.MainActivity;
import com.example.otte.R;
import com.example.otte.base.Constants;

public class ConnectionScene extends AppCompatActivity implements LocationListener {

    //    TextView ReceiveData;
    public static Uri myPicture = null;
    private static final String TAG = "ConnectionScene";
    public static AudioManager mAudioManager;
    public static Button start;

    private static Camera mCamera;
    private static CameraManager camManager;
    private static Camera.Parameters parameters;

    //Bluetooth
    static BluetoothAdapter mBluetoothAdapter;
    private static Vibrator mVibe;
    final static long[] mVibPattern = new long[]{500, 200, 500, 200};
    final static int[] mAmplitudes = new int[]{128, 255, 128, 255};
    private static long[] parcelArray = {0, 1000, 500};
    //recorder
    private static MediaRecorder mRecorder;
    public static boolean isPlaying = false;
    private String mConnectedDeviceName = null;

    private boolean isGpsEnabled;
    private boolean isNetworkEnabled;
    private int count = 0;
    //GPS
    private LocationManager mLocationManager;
    private String mProvider;
    private Location location;
    public static double latitude;
    public static double longitude;
    public static double altitude;

    //ble
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static String device_address;
    public static String device_name;
    public static BluetoothLeService mBluetoothLeService;
    public static boolean mConnected = false;
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(device_address);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    public String mdata="";
    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnected = true;
                        connectDevice();
                        start.setVisibility(View.VISIBLE);
                    }
                });
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        start.setVisibility(View.INVISIBLE);
                    }
                });
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
//                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                Log.d("ConnectionScene", "Click?");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                //데이터 읽어서 뿌리는 부분
                mdata = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private int cameraId;

    //Find phone
//    private Spinner mSminner;
//    public static MediaPlayer mAudio = null;
//    public static boolean isAudioPlay = false;
//    public static int streamType = AudioManager.STREAM_RING;

//    private GoogleApiClient client;
    public static Context context;
    static Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        mActivity = this;

        setContentView(R.layout.scence_connection);

        getSupportActionBar().hide();
        start = (Button)findViewById(R.id.startButton);

//        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        mProvider = mLocationManager.getBestProvider(criteria, false);

        //무음모드 에러
//        NotificationManager notificationManager =
//                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            //퍼미션 상태 확인
//            if (!hasPermissions(PERMISSIONS)) {
//                //퍼미션 허가 안되어있다면 사용자에게 요청
//                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
//            }
//            if (!notificationManager.isNotificationPolicyAccessGranted()){
//                mActivity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(context, context.getString(R.string.str_toast_turn_off_do_not_disturb), Toast.LENGTH_SHORT).show();
//                    }
//                });
//                Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                startActivity(intent);
//            }
//        }
//        cameraId = findFrontCameraId();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//        isGpsEnabled = mLocationManager
//                .isProviderEnabled(LocationManager.GPS_PROVIDER);
//
//        isNetworkEnabled = mLocationManager
//                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//
//        if(isGpsEnabled) {
//            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Log.d(TAG, "GPSProvider");
//        }
//        else if(isNetworkEnabled){
//            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            Log.d(TAG, "NetworkProvider");
//        }
//
//        if(location!= null) {
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            altitude = location.getAltitude();
//            if(isGpsEnabled){
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.LOCATION_UPDATE_NETWORK_TIME_MILLISEC, Constants.LOCATION_UPDATE_NETWORK_DIST_METER, this);
//                Log.d(TAG, "GPSENABLED");
//            } else if(isNetworkEnabled) {
//                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.LOCATION_UPDATE_GPS_TIME_MILLISEC, Constants.LOCATION_UPDATE_GPS_DIST_METER, this);
//                Log.d(TAG, "NETWORKENABLED");
//            }
//            onLocationChanged(location);
//        }
        //BLE지원여부
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.ble_not_supported), Toast.LENGTH_SHORT).show();
                }
            });
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //Bluetooth지원여부
        if (mBluetoothAdapter == null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.error_bluetooth_not_supported), Toast.LENGTH_SHORT).show();
                }
            });
            finish();
            return;
        }
        //bluetooth활성화
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 2);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
    }
    //여기서부턴 퍼미션 관련 메소드
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    static final int PERMISSIONS_REQUEST_CODE = 1000;
    String[] PERMISSIONS  = {"android.permission.INTERNET"
            , "android.permission.ACCESS_FINE_LOCATION"
            , "android.permission.BLUETOOTH", "android.permission.BLUETOOTH_ADMIN"
            , "android.permission.SEND_SMS", "android.permission.READ_PHONE_STATE"
            , "android.permission.ACCESS_NOTIFICATION_POLICY"
            , "android.permission.READ_CONTACTS", "android.permission.CAMERA"
            , "android.permission.RECORD_AUDIO", "android.permission.WRITE_EXTERNAL_STORAGE"
            , "android.permission.CALL_PHONE", "android.permission.RECEIVE_SMS"
            , "android.permission.ACCESS_FINE_LOCATION"};

    private boolean hasPermissions(String[] permissions) {
        int result;
        //스트링 배열에 있는 퍼미션들의 허가 상태 여부 확인
        for (String perms : permissions){
            result = ContextCompat.checkSelfPermission(this, perms);
            if (result == PackageManager.PERMISSION_DENIED){
                //허가 안된 퍼미션 발견
                return false;
            }
        }
        //모든 퍼미션이 허가되었음
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean cameraPermissionAccepted = grantResults[0]
                            == PackageManager.PERMISSION_GRANTED;
                    if (!cameraPermissionAccepted)
                        showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");
                }
                break;
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder( ConnectionScene.this);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id){
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                finish();
            }
        });
        builder.create().show();
    }
    private int findFrontCameraId() {
        int cameraId = -1;
//        // Search for the front facing camera
//        int numberOfCameras = Camera.getNumberOfCameras();
//        for (int i = 0; i < numberOfCameras; i++) {
//            Camera.CameraInfo info = new Camera.CameraInfo();
//            Camera.getCameraInfo(i, info);
//            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                cameraId = i;
//                break;
//            }
//        }
        return cameraId;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(mBluetoothLeService!=null)
            mBluetoothLeService.disconnect();
        if(event.getActionMasked()== MotionEvent.ACTION_DOWN){
            Log.d(TAG, "Scan Device");
            setScene();
        }

        return super.onTouchEvent(event);
    }

    public void setScene() {

        Intent i = new Intent(getApplicationContext(), DeviceScanActivity.class);
        startActivityForResult(i, Constants.REQUEST_CONNECT_DEVICE);

    }

    public static Intent data;
    public static void connectDevice() {
        device_name = data.getExtras().getString(EXTRAS_DEVICE_NAME);
        device_address = data.getExtras().getString(EXTRAS_DEVICE_ADDRESS);
        Log.d(TAG, device_name);
        mBluetoothLeService.connect(device_address);
        if(mConnected){
            Log.d(TAG, "Connect finish");
            mConnected = true;
            //add UI scene
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(mainIntent);
        }else{
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, context.getString(R.string.str_toast_bluetooth_is_not_connected), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "super called");
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "requestCode:" + requestCode);
        Log.d(TAG, "resultCode:" + resultCode);

        switch (requestCode) {
            case Constants.REQUEST_CONNECT_DEVICE://2
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "BluetoothDevice scan");
                    Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                    bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                    this.data = data;
                    start.setVisibility(View.VISIBLE);
                }
                break;
            case Constants.CAMERA_CAPTURE:
                Log.d(TAG, "CAMERA CAPTURE");
//                onCaptureImageResult(data);
//                break;

                if (resultCode == RESULT_OK)
                    try {
                        data.getExtras().get("data"); //섬네일
                    } catch (Exception e) {
                    }
                break;
        }
    }

    private static int conv_message;

    public static void actions(final String accessToken, final String message) {//throws ClassNotFoundException, NoSuchMethodException, RemoteException, IllegalAccessException, InvocationTargetException {

        Log.d(TAG, "actions function called : " + message);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((MainActivity)MainActivity.mContext).receiveData(accessToken, message);
            }
        }, 0);
    }

    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    public void onStartButtonClicked(View v){
        connectDevice();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBluetoothLeService!=null) {
            mBluetoothLeService.disconnect();
            unbindService(mServiceConnection);
        }
        mBluetoothLeService = null;
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(device_address);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
