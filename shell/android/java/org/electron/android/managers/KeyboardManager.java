// Copyright 2024 Electron Android Project
// Keyboard Manager for Electron Android

package org.electron.android.managers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

/**
 * Manages soft keyboard interactions for Electron Android.
 */
public class KeyboardManager {

    private Activity activity;
    private View currentFocus;
    private KeyboardListener listener;

    public interface KeyboardListener {
        void onKeyboardShown(int height);
        void onKeyboardHidden();
    }

    public KeyboardManager(Activity activity) {
        this.activity = activity;
    }

    /**
     * Show the soft keyboard.
     */
    public void showKeyboard(View view) {
        if (view == null) return;
        
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) 
            activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * Hide the soft keyboard.
     */
    public void hideKeyboard() {
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = activity.getWindow().getDecorView();
        }
        
        InputMethodManager imm = (InputMethodManager) 
            activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Toggle keyboard visibility.
     */
    public void toggleKeyboard() {
        View view = activity.getCurrentFocus();
        if (view == null) return;
        
        InputMethodManager imm = (InputMethodManager) 
            activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        if (imm != null) {
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            } else {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    /**
     * Set keyboard listener.
     */
    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
    }

    /**
     * Check if keyboard is currently visible.
     */
    public boolean isKeyboardVisible() {
        View view = activity.getCurrentFocus();
        if (view == null) return false;
        
        InputMethodManager imm = (InputMethodManager) 
            activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        
        return imm != null && imm.isActive(view);
    }

    /**
     * Configure WebView for keyboard interaction.
     */
    public void configureWebView(WebView webView) {
        webView.requestFocus();
        webView.requestFocusFromTouch();
    }
}
