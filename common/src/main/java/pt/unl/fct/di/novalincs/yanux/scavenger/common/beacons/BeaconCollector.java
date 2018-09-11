/*
 * Copyright (c) 2018 Pedro Albuquerque Santos.
 *
 * This file is part of YanuX Scavenger.
 *
 * YanuX Scavenger is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * YanuX Scavenger is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with YanuX Scavenger. If not, see <https://www.gnu.org/licenses/gpl.html>
 */

package pt.unl.fct.di.novalincs.yanux.scavenger.common.beacons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

import pt.unl.fct.di.novalincs.yanux.scavenger.common.utilities.Constants;

public class BeaconCollector {
    public static final String ACTION_BEACON_MONITOR_ENTER_REGION = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.ACTION_BEACON_MONITOR_ENTER_REGION";
    public static final String ACTION_BEACON_MONITOR_EXIT_REGION = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.ACTION_BEACON_MONITOR_EXIT_REGION";
    public static final String ACTION_BEACON_MONITOR_DETERMINED_REGION_STATE = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.ACTION_BEACON_MONITOR_DETERMINED_REGION_STATE";
    public static final String ACTION_BEACON_RANGE_BEACONS = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.ACTION_BEACON_RANGE_BEACONS";
    public static final String EXTRA_BEACON_REGION = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.EXTRA_BEACON_REGION";
    public static final String EXTRA_BEACON_REGION_STATE = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.EXTRA_BEACON_REGION_STATE";
    public static final String EXTRA_BEACONS = "pt.unl.fct.di.novalincs.yanux.scavenger.common.bluetooth.BeaconCollector.EXTRA_BEACONS";
    private static final String LOG_TAG = Constants.LOG_TAG + "_BEACON_COLLECTOR";
    //iBeacon Beacon Layout
    private static final String IBEACON_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String REGION_UUID = "113069EC-6E64-4BD3-6810-DE01B36E8A3E";

    private final Context context;
    private final BeaconConsumer beaconConsumer;
    private final BroadcastReceiver broadcastReceiver;
    private final IntentFilter intentFilter;
    private final BeaconManager beaconManager;
    private Region region;

    private long startRangingTime;
    private boolean ranging;
    private boolean monitoring;

    public BeaconCollector(BeaconConsumer beaconConsumer, BroadcastReceiver broadcastReceiver) {
        this.beaconConsumer = beaconConsumer;
        context = beaconConsumer.getApplicationContext();
        this.broadcastReceiver = broadcastReceiver;
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_BEACON_MONITOR_ENTER_REGION);
        intentFilter.addAction(ACTION_BEACON_MONITOR_EXIT_REGION);
        intentFilter.addAction(ACTION_BEACON_MONITOR_DETERMINED_REGION_STATE);
        intentFilter.addAction(ACTION_BEACON_RANGE_BEACONS);

        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BeaconParser.URI_BEACON_LAYOUT));

        /**
         * TODO:
         * If needed and possible, implement a better distance calculator.
         */
        //Beacon.setDistanceCalculator(new CustomDistanceCalculator());

        beaconManager.setBackgroundMode(false);
        //beaconManager.setForegroundScanPeriod(0);
        //beaconManager.setForegroundBetweenScanPeriod(0);
        //beaconManager.setBackgroundScanPeriod(0);
        //beaconManager.setBackgroundBetweenScanPeriod(0);

        beaconManager.bind(this.beaconConsumer);

        //Monitor Notifier
        setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.d(LOG_TAG, "Region Entered");
                Intent intent = new Intent();
                intent.setAction(ACTION_BEACON_MONITOR_ENTER_REGION);
                intent.putExtra(EXTRA_BEACON_REGION, (Parcelable) region);
                context.sendBroadcast(intent);
            }

            @Override
            public void didExitRegion(Region region) {
                Log.d(LOG_TAG, "Region Exit");
                Intent intent = new Intent();
                intent.setAction(ACTION_BEACON_MONITOR_EXIT_REGION);
                intent.putExtra(EXTRA_BEACON_REGION, (Parcelable) region);
                context.sendBroadcast(intent);
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.d(LOG_TAG, "Region State Changed: " + state);
                Intent intent = new Intent();
                intent.setAction(ACTION_BEACON_MONITOR_DETERMINED_REGION_STATE);
                intent.putExtra(EXTRA_BEACON_REGION, (Parcelable) region);
                intent.putExtra(EXTRA_BEACON_REGION_STATE, state);
                context.sendBroadcast(intent);
            }
        });
        //Ranging Notifier
        setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                ArrayList<BeaconWrapper> beaconWrapperArrayList = new ArrayList<>(beacons.size());
                for (Beacon beacon : beacons) {
                    beaconWrapperArrayList.add(new BeaconWrapper(beacon));
                }
                Intent intent = new Intent();
                intent.setAction(ACTION_BEACON_RANGE_BEACONS);
                intent.putParcelableArrayListExtra(EXTRA_BEACONS, beaconWrapperArrayList);
                intent.putExtra(EXTRA_BEACON_REGION, (Parcelable) region);
                context.sendBroadcast(intent);
                startRangingTime = SystemClock.elapsedRealtime();
            }
        });
        setRegion(new Region(REGION_UUID, null, null, null));
        startRangingTime = -1;
        ranging = false;
        monitoring = false;
    }

    public void setRangeNotifier(RangeNotifier notifier) {
        beaconManager.addRangeNotifier(notifier);
    }

    public void setMonitorNotifier(MonitorNotifier notifier) {
        beaconManager.addMonitorNotifier(notifier);
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public void startMonitoring() {
        startMonitoring(region);
    }

    public void stopMonitoring() {
        stopMonitoring(region);
    }

    public void startRanging() {
        startRanging(region);
    }

    public void stopRanging() {
        stopRanging(region);
    }

    public void startMonitoring(Region region) {
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
            context.registerReceiver(broadcastReceiver, intentFilter);
            monitoring = true;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    public void stopMonitoring(Region region) {
        try {
            monitoring = false;
            context.unregisterReceiver(broadcastReceiver);
            beaconManager.stopMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    public void startRanging(Region region) {
        try {
            beaconManager.startRangingBeaconsInRegion(region);
            context.registerReceiver(broadcastReceiver, intentFilter);
            ranging = true;
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    public void stopRanging(Region region) {
        try {
            ranging = false;
            startRangingTime = -1;
            context.unregisterReceiver(broadcastReceiver);
            beaconManager.stopRangingBeaconsInRegion(region);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    public boolean isRanging() {
        return ranging;
    }

    public boolean isMonitoring() {
        return monitoring;
    }

    public long getStartRangingTime() {
        return startRangingTime;
    }

    public long getRangingElapsedTime() {
        if (startRangingTime < 0) {
            return -1;
        } else {
            return SystemClock.elapsedRealtime() - startRangingTime;
        }
    }

    public void unbind() {
        beaconManager.unbind(beaconConsumer);
    }
}