// Copyright 2024 Electron Android Project
// ElectronBridge - JNI Bridge pour la communication Java/C++

package org.electron.android.bridge;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Keep;

/**
 * Bridge JNI pour communiquer avec le code C++ natif d'Electron.
 * Gère les événements et transmets les appels entre Java et C++.
 */
public class ElectronBridge {

    private Activity activity;
    private long nativePointer = 0;

    // Load native library
    static {
        System.loadLibrary("electron");
    }

    public ElectronBridge(Activity activity) {
        this.activity = activity;
    }

    /**
     * Initialise le bridge natif.
     * Doit être appelé après la création de l'activité.
     */
    public void initialize() {
        nativePointer = nativeInit(activity.getWindow().getDecorView().getWindowToken());
    }

    /**
     * Charge une URL dans le WebView.
     */
    public void loadUrl(String url) {
        nativeLoadUrl(nativePointer, url);
    }

    /**
     * Called when the activity starts.
     */
    public void onStart() {
        nativeOnStart(nativePointer);
    }

    /**
     * Called when the activity resumes.
     */
    public void onResume() {
        nativeOnResume(nativePointer);
    }

    /**
     * Called when the activity pauses.
     */
    public void onPause() {
        nativeOnPause(nativePointer);
    }

    /**
     * Called when the activity stops.
     */
    public void onStop() {
        nativeOnStop(nativePointer);
    }

    /**
     * Called when the activity is destroyed.
     */
    public void onDestroy() {
        nativeOnDestroy(nativePointer);
        nativePointer = 0;
    }

    /**
     * Called when the configuration changes (rotation, etc).
     */
    public void onConfigurationChanged(Configuration config) {
        nativeOnConfigurationChanged(nativePointer, config.orientation);
    }

    /**
     * Gère le bouton retour.
     * @return true si l'événement a été géré, false sinon
     */
    public boolean onBackPressed() {
        return nativeOnBackPressed(nativePointer);
    }

    /**
     * Transmet un événement de toucher au code natif.
     */
    public void onTouchEvent(MotionEvent event) {
        nativeOnTouchEvent(nativePointer, 
                          event.getAction(), 
                          event.getX(), 
                          event.getY());
    }

    /**
     * Transmet le résultat d'une requête de permission.
     */
    public void onPermissionResult(String permission, boolean granted) {
        nativeOnPermissionResult(nativePointer, permission, granted);
    }

    /**
     * Retourne l'activité associée.
     */
    public Activity getActivity() {
        return activity;
    }

    // ============= Native methods (JNI) =============

    // Initialize native bridge
    private native long nativeInit(long windowToken);

    // Navigation
    private native void nativeLoadUrl(long nativePointer, String url);
    private native void nativeGoBack(long nativePointer);
    private native void nativeGoForward(long nativePointer);
    private native void nativeReload(long nativePointer);
    private native void nativeStop(long nativePointer);

    // Lifecycle
    private native void nativeOnStart(long nativePointer);
    private native void nativeOnResume(long nativePointer);
    private native void nativeOnPause(long nativePointer);
    private native void nativeOnStop(long nativePointer);
    private native void nativeOnDestroy(long nativePointer);
    private native void nativeOnConfigurationChanged(long nativePointer, int orientation);

    // Back press
    private native boolean nativeOnBackPressed(long nativePointer);

    // Input
    private native void nativeOnTouchEvent(long nativePointer, int action, float x, float y);
    private native void nativeOnKeyEvent(long nativePointer, int keyCode, int action);

    // Permissions
    private native void nativeOnPermissionResult(long nativePointer, String permission, boolean granted);

    // Window
    private native void nativeSetFullscreen(long nativePointer, boolean fullscreen);
    private native void nativeSetStatusBarColor(long nativePointer, int color);
    private native void nativeSetNavigationBarColor(long nativePointer, int color);
}
