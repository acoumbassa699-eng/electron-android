// Copyright 2024 Electron Android Project
// ElectronActivity - Activité principale pour Electron sur Android

package org.electron.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import org.electron.android.bridge.ElectronBridge;
import org.electron.android.content.ElectronContentView;
import org.electron.android.view.ElectronRootView;

/**
 * Activité principale d'Electron pour Android.
 * Contient le WebView et communique avec le moteur Chromium via JNI.
 */
public class ElectronActivity extends AppCompatActivity {

    public static final String TAG = "ElectronActivity";
    
    // Permissions request codes
    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_FILE_PICKER = 1002;
    private static final int REQUEST_CODE_SAVE_FILE = 1003;

    // Views
    private ElectronRootView rootView;
    private ElectronContentView contentView;

    // Bridge to native code
    private ElectronBridge bridge;

    // State
    private boolean isFullscreen = false;
    private int currentOrientation = Configuration.ORIENTATION_UNDEFINED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure window
        configureWindow();
        
        // Initialize bridge
        bridge = new ElectronBridge(this);
        bridge.initialize();
        
        // Setup views
        setupViews();
        
        // Handle intent
        handleIntent(getIntent());
        
        // Register back press handler
        setupBackPressHandler();
    }

    private void configureWindow() {
        // Full screen by default
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Transparent status bar if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    private void setupViews() {
        rootView = new ElectronRootView(this);
        contentView = new ElectronContentView(this);
        
        rootView.addView(contentView);
        setContentView(rootView);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Tell native code to handle back press
                if (!bridge.onBackPressed()) {
                    // If native doesn't handle it, finish the activity
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bridge.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bridge.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        bridge.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bridge.onStop();
    }

    @Override
    protected void onDestroy() {
        bridge.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bridge.onConfigurationChanged(newConfig);
        currentOrientation = newConfig.orientation;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            // Re-apply immersive mode
            configureWindow();
        }
    }

    // Intent handling
    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();
        Uri data = intent.getData();

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            bridge.loadUrl(data.toString());
        } else if (Intent.ACTION_MAIN.equals(action)) {
            bridge.loadUrl("file:///android_asset/index.html");
        }
    }

    // Permission handling
    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, 
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            for (int i = 0; i < permissions.length; i++) {
                boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                bridge.onPermissionResult(permissions[i], granted);
            }
        }
    }

    // Public API for native code
    public void setFullscreen(boolean enabled) {
        isFullscreen = enabled;
        runOnUiThread(() -> configureWindow());
    }

    public void showLoadingScreen() {
        runOnUiThread(() -> rootView.showLoadingScreen());
    }

    public void hideLoadingScreen() {
        runOnUiThread(() -> rootView.hideLoadingScreen());
    }

    // Public API for native code
    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }

    public void setNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(color);
        }
    }
}
