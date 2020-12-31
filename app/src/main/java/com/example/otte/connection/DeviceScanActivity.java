package com.example.otte.connection;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.otte.R;

import java.util.ArrayList;

/**
 * Created by 이나영 on 2018-03-06.
 */

public class DeviceScanActivity extends AppCompatActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private boolean mScanning;
    private Handler mHandler;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    ListView list;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // scene_device_list 레이아웃 속성 지정 및 메모리에 올리기
        setContentView(R.layout.scene_device_list);
        Log.d("DeviceScanActivity", "create");
        // 블루투스 디바이스 검색 시 app bar 제목 설정
        getSupportActionBar().setTitle("Bluetooth Connect");
        mHandler = new Handler();
        list = (ListView) findViewById(R.id.device_list_view);
        list.setOnItemClickListener(mLeDeviceClickListener);

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
//        final BluetoothManager bluetoothManager =
//                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothAdapter = ConnectionScene.mBluetoothAdapter;
//        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


//        ConnectionOTTE retrofitClient = new ConnectionOTTE();
//        Call<ConnectionOTTE> call = retrofitClient.service1.getToken();
//        call.enqueue(new Callback<ConnectionOTTE>() {
//            @Override
//            public void onResponse(Call<ConnectionOTTE> call, Response<ConnectionOTTE> response) {
//                if(response.isSuccessful()) {
//                    ConnectionOTTE request = response.body();
//                    Log.d("onResponse : 성공, 결과 : ", request.toString());
//                }else{
//                    Log.d("onResponse : ", "실패");
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<ConnectionOTTE> call, Throwable t) {
//                Log.d("onResponse : 오류, 원인 : ", t.getMessage());
//            }
//        });

    }
    //bt le scanner문제해결 ~100
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, yay! Start the Bluetooth device scan.
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                }
            }
        }
    }

    // 블루투스 기기 refresh/scan/stop 화면 구성
    // 최초에 메뉴키가 눌렸을 때 호출
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(
                    R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    // menu item이 클릭되었을 때 호출
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    // 사용자와 상호작용 직전에 호출
    @Override
    protected void onResume() {
        super.onResume();

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Log.d("DeviceScanActivity", "enabled?");
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        list.setAdapter(mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    // main액티비티에서 sub액티비티를 호출하여 넘어갔다가, 다시 main 액티비티로 돌아올때 사용되는 기본 메소드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 다른 액티비티가 보여질 때 호출
    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        mLeDeviceListAdapter.clear();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//    }

    private AdapterView.OnItemClickListener mLeDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            final BluetoothDevice device = mLeDeviceListAdapter.getDevice(arg2);
            if (device == null) return;
            // 액티비티 간 이동을 위해서 Intent 이용
            final Intent intent = new Intent();
            // putExtra() : 액티비티 이동과 동시에 값 전달. key-value 형태.
            intent.putExtra(ConnectionScene.EXTRAS_DEVICE_NAME, device.getName()); // 디바이스 명
            intent.putExtra(ConnectionScene.EXTRAS_DEVICE_ADDRESS, device.getAddress()); // 디바이스 주소

            // 원하는 기기 발견 시 즉시 스캐닝 stop. 메모리 사용이 크기 떄문
            if (mScanning) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                // mBluetoothLeScanner.stopScan(ScanCallback);
                mScanning = false;
            }

            Log.d("DeviceScanActivity", "Choose!");

            // Set result and finish this Activity
            // setResult() : 돌려줄 결과 저장
            setResult(Activity.RESULT_OK, intent);
            // finish() : 해당 activity 종료
            finish();
        }
    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    // onPrepareOptionsMenu()를 호출 -> 옵션메뉴가 화면에 보여질때마다 호출
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            // startLeScan() : BLE기기 검색
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // addDevice() : 블루투스 장치 추가
                            mLeDeviceListAdapter.addDevice(device);
                            // notifyDataSetChanged() : 갱신
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }
}