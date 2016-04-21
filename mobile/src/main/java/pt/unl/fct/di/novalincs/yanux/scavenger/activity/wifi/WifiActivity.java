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

package pt.unl.fct.di.novalincs.yanux.scavenger.activity.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pt.unl.fct.di.novalincs.yanux.scavenger.R;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.logging.IFileLogger;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.logging.JsonFileLogger;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.logging.SensorLog;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.logging.WifiLogEntry;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.permissions.PermissionManager;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.preferences.Preferences;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.sensors.SensorCollector;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.sensors.SensorWrapper;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.utilities.Constants;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.wifi.WifiCollector;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.wifi.WifiConnectionInfo;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.wifi.WifiResult;
import pt.unl.fct.di.novalincs.yanux.scavenger.dialog.logging.LogDialogFragment;

public class WifiActivity extends AppCompatActivity implements LogDialogFragment.LogDialogListerner {
    private ListView wifiAccessPoints;
    private ArrayAdapter<WifiResult> wifiAccessPointsAdapter;
    private Switch logSwitch;
    private TextView sampleCounter;

    private PermissionManager permissionManager;
    private Preferences preferences;
    private WifiCollector wifiCollector;
    private BroadcastReceiver broadcastReceiver;
    private SensorCollector sensorCollector;
    private SensorLog sensorLog;
    private List<SensorWrapper> loggedSensors;
    private IFileLogger logger;

    private int sampleId;
    private int numberOfSamplesToLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        //Wi-Fi Access Points List View
        wifiAccessPoints = (ListView) findViewById(R.id.wifi_access_points);
        //Wi-Fi Access Points List View Adapter
        wifiAccessPointsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        wifiAccessPoints.setAdapter(wifiAccessPointsAdapter);
        //Log switch
        logSwitch = (Switch) findViewById(R.id.log_switch);
        logSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DialogFragment logDialogFragment = new LogDialogFragment();
                    logDialogFragment.show(getSupportFragmentManager(), "WIFI_LOGGING");
                } else {
                    disableLogging();
                }
            }
        });
        //Sample Counter
        sampleCounter = (TextView) findViewById(R.id.log_sample_counter);

        //Permission Manager
        permissionManager = new PermissionManager(this);
        if (Constants.API_LEVEL >= Build.VERSION_CODES.M) {
            permissionManager.requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //Preferences
        preferences = new Preferences(this);
        //Wi-Fi Collector
        wifiCollector = new WifiCollector(this);
        //Check if Wi-Fi scanning is always available
        if (!preferences.hasAskedForWifiScanningAlwaysAvailable()
                && !wifiCollector.isScanAlwaysAvailable()) {
            WifiCollector.enableScanIsAlwaysAvailable(this);
        }
        //Wi-Fi Collector Broadcast Receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifiAccessPointsAdapter.clear();
                List<WifiResult> wifiResults = wifiCollector.getScanResults();
                wifiAccessPointsAdapter.addAll(wifiResults);
                if (logger != null && logger.isOpen()) {
                    if (sampleId < numberOfSamplesToLog) {
                        for (WifiResult wifiResult : wifiResults) {
                            logger.log(new WifiLogEntry(sampleId, wifiResult, wifiCollector.getConnectionInfo(), sensorLog.getCurrentReadings()));
                        }
                        sensorLog.clear();
                        sampleId++;
                        sampleCounter.setText(Integer.toString(sampleId));
                    } else {
                        logSwitch.setChecked(false);
                    }
                }
                wifiCollector.scan(broadcastReceiver);
                updateConnectionInfo();
            }
        };
        sensorCollector = new SensorCollector(this);
        sensorLog = new SensorLog();
        loggedSensors = new ArrayList<>();
        //Add a few sensors to log (remember that a sensor may not be available).
        //TODO: Remove some sensors that are not that useful
        if (sensorCollector.hasOrientation()) {
            loggedSensors.add(sensorCollector.getOrientation());
        }
        if (sensorCollector.hasRotationVector()) {
            loggedSensors.add(sensorCollector.getRotationVector());
        }
        if (sensorCollector.hasAccelerometer()) {
            loggedSensors.add(sensorCollector.getAccelerometer());
        }
        if (sensorCollector.hasGravity()) {
            loggedSensors.add(sensorCollector.getGravity());
        }
        if (sensorCollector.hasLinearAcceleration()) {
            loggedSensors.add(sensorCollector.getLinearAcceleration());
        }
        if (sensorCollector.hasGyroscope()) {
            loggedSensors.add(sensorCollector.getGyroscope());
        }
        if (sensorCollector.hasMagneticField()) {
            loggedSensors.add(sensorCollector.getMagneticField());
        }
        if (sensorCollector.hasPressure()) {
            loggedSensors.add(sensorCollector.getPressure());
        }
        if (sensorCollector.hasLight()) {
            loggedSensors.add(sensorCollector.getLight());
        }
        if (sensorCollector.hasProximity()) {
            loggedSensors.add(sensorCollector.getProximity());
        }
        logger = new JsonFileLogger();

        disableLogging();
        updateConnectionInfo();
        wifiCollector.scan(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiCollector.cancelScan(broadcastReceiver);
        disableLogging();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PermissionManager.REQUEST_PERMISSION_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (PermissionManager.werePermissionsGranted(grantResults)) {
                    Toast.makeText(getApplicationContext(), R.string.permission_location_allowed, Toast.LENGTH_SHORT).show();
                    // Permission was granted, yay!
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), R.string.permission_location_allowed, Toast.LENGTH_SHORT).show();
                }
                break;
            // other 'case' lines to check for other permissions this app might request
            case PermissionManager.REQUEST_PERMISSION_GENERIC:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case WifiCollector.REQUEST_CODE_SCAN_ALWAYS_AVAILABLE:
                preferences.setHasAskedForWifiScanningAlwaysAvailable(true);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, R.string.wifi_scan_always_available_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.wifi_scan_always_available_not_enabled, Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    private void updateConnectionInfo() {
        WifiConnectionInfo wifiConnectionInfo = wifiCollector.getConnectionInfo();
        TextView wifiConnectionInfoView = (TextView) findViewById(R.id.wifi_connection_info);
        wifiConnectionInfoView.setText(wifiConnectionInfo.getSsid() + " [" + wifiConnectionInfo.getBssid() + "]\nRSSI: " + wifiConnectionInfo.getRssi());
    }

    @Override
    public void onDialogPositiveClick(LogDialogFragment dialog) {
        disableLogging();
        enableLogging(dialog.getLogName(), dialog.getNumberOfSamples());
    }

    @Override
    public void onDialogNegativeClick(LogDialogFragment dialog) {
        disableLogging();
        dialog.getDialog().cancel();
    }

    private void enableLogging(String logName, int numberOfSamplesToLog) {
        try {
            logger.setFilename(logName + ".json");
            logger.open();
            this.numberOfSamplesToLog = numberOfSamplesToLog;
            sampleId = 0;
            for (SensorWrapper sensor : loggedSensors) {
                sensor.registerListener(sensorLog);
            }
            findViewById(R.id.log_sample_counter_label).setVisibility(View.VISIBLE);
            sampleCounter.setVisibility(View.VISIBLE);
            sampleCounter.setText(Integer.toString(sampleId));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void disableLogging() {
        if (logger.isOpen()) {
            try {
                logger.close();
                numberOfSamplesToLog = 0;
                sampleId = 0;
                for (SensorWrapper sensor : loggedSensors) {
                    sensor.unregisterListener(sensorLog);
                }
                findViewById(R.id.log_sample_counter_label).setVisibility(View.GONE);
                sampleCounter.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}