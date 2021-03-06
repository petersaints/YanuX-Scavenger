/*
 * Copyright (c) 2020 Pedro Albuquerque Santos.
 *
 * This file is part of YanuX Scavenger.
 *
 * YanuX Scavenger is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * YanuX Scavenger is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with YanuX Scavenger. If not, see <https://www.gnu.org/licenses/gpl.html>
 */

package pt.unl.fct.di.novalincs.yanux.scavenger.common.logging;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import pt.unl.fct.di.novalincs.yanux.scavenger.common.file.StorageType;
import pt.unl.fct.di.novalincs.yanux.scavenger.common.utilities.Constants;

public class JsonFileLogger extends AbstractFileLogger {
    public static final String DEFAULT_FILENAME = "log.json";
    private static final String LOG_TAG = Constants.LOG_TAG + "_JSON_LOGGER";

    private long entryIdCounter;
    private LogFile logFile;
    private LogSession currentLogSession;


    public JsonFileLogger(Context context, String directory, String filename, StorageType storageType) {
        super(context, directory, filename, storageType);
        entryIdCounter = 0;
    }

    public JsonFileLogger(Context context, String filename, StorageType storageType) {
        this(context, DEFAULT_DIRECTORY, filename, storageType);
    }

    public JsonFileLogger(Context context, String directory, String filename) {
        this(context, directory, filename, DEFAULT_STORAGE_TYPE);
    }

    public JsonFileLogger(Context context, String filename) {
        this(context, DEFAULT_DIRECTORY, filename, DEFAULT_STORAGE_TYPE);
    }

    public JsonFileLogger(Context context) {
        this(context, DEFAULT_DIRECTORY, DEFAULT_FILENAME, DEFAULT_STORAGE_TYPE);
    }

    public JsonFileLogger(String directory, String filename, StorageType storageType) {
        this(null, directory, filename, storageType);
    }

    public JsonFileLogger(String directory, String filename) {
        this(null, directory, filename, DEFAULT_STORAGE_TYPE);
    }

    public JsonFileLogger(String filename, StorageType storageType) {
        this(null, DEFAULT_DIRECTORY, filename, storageType);
    }

    public JsonFileLogger(String filename) {
        this(null, DEFAULT_DIRECTORY, filename, DEFAULT_STORAGE_TYPE);
    }

    public JsonFileLogger() {
        this(null, DEFAULT_DIRECTORY, DEFAULT_FILENAME, DEFAULT_STORAGE_TYPE);
    }


    @Override
    public void open() throws IOException {
        try {
            if (storageType == StorageType.ANDROID_URI) {
                Uri uri = Uri.parse(getStoragePath());
                Log.d(LOG_TAG, "Open Existing Log:" + uri);
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                logFile = Constants.OBJECT_MAPPER.readValue(inputStream, LogFile.class);
                inputStream.close();
            } else {
                logFile = Constants.OBJECT_MAPPER.readValue(new File(getStoragePath()), LogFile.class);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
            logFile = new LogFile(filename);
        }
        super.open();
        //Add new session
        currentLogSession = new LogSession();
        logFile.getSessions().add(currentLogSession);
    }

    @Override
    public void log(long id, IReading loggable) {
        currentLogSession.getEntries().add(new LogEntry(id, System.currentTimeMillis(), loggable));
    }

    @Override
    public void log(IReading loggable) {
        log(entryIdCounter++, loggable);
    }

    @Override
    public void close() throws IOException {
        currentLogSession = null;
        Constants.OBJECT_MAPPER.writeValue(fileOutputStream, logFile);
        super.close();
    }
}
