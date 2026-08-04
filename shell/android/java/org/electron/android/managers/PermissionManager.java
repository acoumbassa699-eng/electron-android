// Copyright 2024 Electron Android Project
// Permission Manager for Electron Android

package org.electron.android.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages runtime permissions for Electron Android.
 */
public class PermissionManager {

    public interface PermissionCallback {
        void onPermissionResult(String permission, boolean granted);
        void onAllPermissionsResult(Map<String, Boolean> results);
    }

    private AppCompatActivity activity;
    private PermissionCallback callback;
    private Map<String, Boolean> pendingPermissions;

    private ActivityResultLauncher<String[]> requestPermissionLauncher;

    public PermissionManager(AppCompatActivity activity) {
        this.activity = activity;
        this.pendingPermissions = new HashMap<>();
        registerActivityResultLauncher();
    }

    private void registerActivityResultLauncher() {
        requestPermissionLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {
                if (callback != null) {
                    for (Map.Entry<String, Boolean> entry : results.entrySet()) {
                        callback.onPermissionResult(entry.getKey(), entry.getValue());
                        pendingPermissions.put(entry.getKey(), entry.getValue());
                    }
                    callback.onAllPermissionsResult(new HashMap<>(results));
                }
            }
        );
    }

    /**
     * Check if a permission is granted.
     */
    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) 
            == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request a single permission.
     */
    public void requestPermission(String permission, PermissionCallback callback) {
        if (hasPermission(permission)) {
            Map<String, Boolean> result = new HashMap<>();
            result.put(permission, true);
            callback.onAllPermissionsResult(result);
            return;
        }

        this.callback = callback;
        requestPermissionLauncher.launch(new String[]{permission});
    }

    /**
     * Request multiple permissions.
     */
    public void requestPermissions(String[] permissions, PermissionCallback callback) {
        // Filter out already granted permissions
        List<String> toRequest = new ArrayList<>();
        Map<String, Boolean> alreadyGranted = new HashMap<>();

        for (String permission : permissions) {
            if (hasPermission(permission)) {
                alreadyGranted.put(permission, true);
            } else {
                toRequest.add(permission);
            }
        }

        if (toRequest.isEmpty()) {
            callback.onAllPermissionsResult(alreadyGranted);
            return;
        }

        this.callback = callback;
        pendingPermissions.putAll(alreadyGranted);
        requestPermissionLauncher.launch(toRequest.toArray(new String[0]));
    }

    /**
     * Check if we should show rationale for a permission.
     */
    public boolean shouldShowRationale(String permission) {
        return activity.shouldShowRequestPermissionRationale(permission);
    }

    /**
     * Common permission checkers.
     */
    public boolean hasCameraPermission() {
        return hasPermission(Manifest.permission.CAMERA);
    }

    public boolean hasMicrophonePermission() {
        return hasPermission(Manifest.permission.RECORD_AUDIO);
    }

    public boolean hasLocationPermission() {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
               hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    public boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermission(Manifest.permission.READ_MEDIA_IMAGES) ||
                   hasPermission(Manifest.permission.READ_MEDIA_VIDEO) ||
                   hasPermission(Manifest.permission.READ_MEDIA_AUDIO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true; // Scoped storage
        } else {
            return hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return hasPermission(Manifest.permission.POST_NOTIFICATIONS);
        }
        return true; // Not required before Android 13
    }

    public boolean hasBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPermission(Manifest.permission.BLUETOOTH_CONNECT) &&
                   hasPermission(Manifest.permission.BLUETOOTH_SCAN);
        }
        return hasPermission(Manifest.permission.BLUETOOTH);
    }
}
