// Copyright 2024 Electron Android Project
// Device Info utility for Electron Android

package org.electron.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Provides device information for Electron.
 */
public class DeviceInfo {

    private Context context;

    public DeviceInfo(Context context) {
        this.context = context;
    }

    /**
     * Get all device information as a map.
     */
    public Map<String, Object> getDeviceInfo() {
        Map<String, Object> info = new HashMap<>();
        
        info.put("platform", "android");
        info.put("version", Build.VERSION.RELEASE);
        info.put("versionSDK", Build.VERSION.SDK_INT);
        info.put("manufacturer", Build.MANUFACTURER);
        info.put("model", Build.MODEL);
        info.put("device", Build.DEVICE);
        info.put("product", Build.PRODUCT);
        info.put("brand", Build.BRAND);
        info.put("hardware", Build.HARDWARE);
        
        // Display metrics
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        info.put("screenWidth", metrics.widthPixels);
        info.put("screenHeight", metrics.heightPixels);
        info.put("screenDensity", metrics.density);
        info.put("screenDensityDpi", metrics.densityDpi);
        
        // Memory info
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        info.put("totalMemory", memInfo.totalMem);
        info.put("availableMemory", memInfo.availMem);
        info.put("lowMemory", memInfo.lowMemory);
        
        // Orientation
        Configuration config = context.getResources().getConfiguration();
        info.put("orientation", config.orientation == Configuration.ORIENTATION_LANDSCAPE ? "landscape" : "portrait");
        info.put("locale", config.locales.get(0).getLanguage() + "_" + config.locales.get(0).getCountry());
        
        // CPU info
        info.put("supportedAbis", Build.SUPPORTED_ABIS);
        
        return info;
    }

    /**
     * Get screen dimensions.
     */
    public int[] getScreenSize() {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new int[]{metrics.widthPixels, metrics.heightPixels};
    }

    /**
     * Get device DPI.
     */
    public int getScreenDensity() {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * Check if device is a tablet (sw600dp or larger).
     */
    public boolean isTablet() {
        Configuration config = context.getResources().getConfiguration();
        return config.smallestScreenWidthDp >= 600;
    }

    /**
     * Check if device is in landscape mode.
     */
    public boolean isLandscape() {
        Configuration config = context.getResources().getConfiguration();
        return config.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Get available memory in MB.
     */
    public long getAvailableMemoryMB() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        return memInfo.availMem / (1024 * 1024);
    }

    /**
     * Get total memory in MB.
     */
    public long getTotalMemoryMB() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memInfo);
        return memInfo.totalMem / (1024 * 1024);
    }

    /**
     * Get user agent string.
     */
    public String getUserAgent() {
        return System.getProperty("http.agent");
    }
}
