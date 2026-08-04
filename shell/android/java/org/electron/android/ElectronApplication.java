// Copyright 2024 Electron Android Project
// ElectronApplication - Application Android pour Electron

package org.electron.android;

import android.app.Application;
import android.content.Context;

import org.electron.android.bridge.ElectronBridge;

/**
 * Application principale pour Electron Android.
 * Gère le cycle de vie global et initialise les composants natifs.
 */
public class ElectronApplication extends Application {

    private static ElectronApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Initialize any global native components
        initializeNative();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    /**
     * Get the singleton instance.
     */
    public static ElectronApplication getInstance() {
        return instance;
    }

    /**
     * Initialize native libraries and components.
     */
    private native void initializeNative();

    /**
     * Cleanup native resources.
     */
    public native void cleanupNative();
}
