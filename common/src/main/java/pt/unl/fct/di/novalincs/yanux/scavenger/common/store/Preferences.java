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

package pt.unl.fct.di.novalincs.yanux.scavenger.common.store;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    public static final String INVALID = null;
    private static final String PREFERENCE_ASKED_WIFI_SCANNING_ALWAYS_AVAILABLE = "asked_wifi_scanning_always_available";

    private final Context context;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor preferencesEditor;

    public Preferences(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.preferencesEditor = preferences.edit();
    }

    public boolean hasAskedForWifiScanningAlwaysAvailable() {
        return preferences.getBoolean(PREFERENCE_ASKED_WIFI_SCANNING_ALWAYS_AVAILABLE, false);
    }

    public void setHasAskedForWifiScanningAlwaysAvailable(boolean flag) {
        preferencesEditor.putBoolean(PREFERENCE_ASKED_WIFI_SCANNING_ALWAYS_AVAILABLE, flag);
        preferencesEditor.apply();
    }
}