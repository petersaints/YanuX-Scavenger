/*
 * Copyright (c) 2016 Pedro Albuquerque Santos.
 *
 * This file is part of YanuX Scavenger.
 *
 * YanuX Scavenger is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * YanuX Scavenger is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with YanuX Scavenger.  If not, see <https://www.gnu.org/licenses/gpl.html>
 */

package pt.unl.fct.di.novalincs.yanux.scavenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BluetoothCollector;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BluetoothDetectedDevice;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BluetoothLeCollector;

public class BluetoothLeActivity extends AppCompatActivity {

    private BluetoothLeCollector bluetoothLeCollector;
    private ListView bluetoothLeDevices;
    private ArrayAdapter<BluetoothDetectedDevice> bluetoothLeDevicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_le);

        bluetoothLeDevices = (ListView) findViewById(R.id.bluetooth_le_devices);
        bluetoothLeDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        bluetoothLeDevices.setAdapter(bluetoothLeDevicesAdapter);

        bluetoothLeCollector = new BluetoothLeCollector(this, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (intent.getAction()) {
                    case BluetoothLeCollector.ACTION_BLUETOOTH_LE_DEVICE_FOUND:
                        BluetoothDetectedDevice bluetoothDetectedDevice = intent.getParcelableExtra(BluetoothLeCollector.EXTRA_BLUETOOTH_LE_DEVICE);
                        bluetoothLeDevicesAdapter.remove(bluetoothDetectedDevice);
                        bluetoothLeDevicesAdapter.add(bluetoothDetectedDevice);
                        break;
                    case BluetoothLeCollector.ACTION_BLUETOOTH_LE_SCAN_FINISHED:
                        bluetoothLeDevicesAdapter.clear();
                        TextView bluetoothDiscoveryElapsedTime = (TextView) findViewById(R.id.bluetooth_le_discovery_elapsed_time);
                        bluetoothDiscoveryElapsedTime.setText(intent.getLongExtra(BluetoothLeCollector.EXTRA_BLUETOOTH_LE_SCAN_ELAPSED_TIME, 0) + " ms");
                        bluetoothLeCollector.scan();
                        break;
                    default:
                        break;

                }
            }
        });

        if (!bluetoothLeCollector.isEnabled()) {
            BluetoothCollector.enableBluetooth(this);
        }

        bluetoothLeCollector.scan();

        TextView bluetoothName = (TextView) findViewById(R.id.bluetooth_le_name);
        bluetoothName.setText(bluetoothLeCollector.getName());

        TextView bluetoothAddress = (TextView) findViewById(R.id.bluetooth_le_address);
        bluetoothAddress.setText(bluetoothLeCollector.getAddress());

    }

    @Override
    protected void onResume() {
        super.onResume();
        bluetoothLeCollector.scan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bluetoothLeCollector.cancelScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case BluetoothCollector.REQUEST_CODE_ENABLE_BLUETOOTH:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.bluetooth_not_enabled, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case BluetoothCollector.REQUEST_CODE_ENABLE_BLUETOOTH_DISCOVERABILITY:
                if (resultCode != RESULT_CANCELED) {
                    Toast.makeText(this, R.string.bluetooth_discoverability_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.bluetooth_discoverability_not_enabled, Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
                break;
        }
    }
}