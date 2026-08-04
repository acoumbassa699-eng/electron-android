// Copyright 2024 Electron Android Project
// ElectronRootView - Vue racine pour Electron Android

package org.electron.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import org.electron.android.bridge.ElectronBridge;

/**
 * Vue racine qui contient la vue de contenu Electron.
 * Gère les gestes globaux et l'indicateur de chargement.
 */
public class ElectronRootView extends FrameLayout {

    private ElectronBridge bridge;
    private ProgressBar loadingIndicator;
    private View loadingView;
    private boolean isLoading = false;

    private GestureDetector gestureDetector;

    public ElectronRootView(Context context) {
        super(context);
        init();
    }

    public ElectronRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElectronRootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Setup gesture detector
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // Handle swipe gestures
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Handle double tap
                return true;
            }
        });

        // Create loading view
        loadingView = new ProgressBar(getContext());
        loadingView.setVisibility(View.GONE);
        addView(loadingView);
    }

    public void setBridge(ElectronBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Let the gesture detector process the event
        gestureDetector.onTouchEvent(ev);
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Forward touch events to the bridge
        if (bridge != null) {
            bridge.onTouchEvent(event);
        }
        return true;
    }

    /**
     * Show the loading indicator.
     */
    public void showLoadingScreen() {
        if (!isLoading) {
            isLoading = true;
            loadingView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide the loading indicator.
     */
    public void hideLoadingScreen() {
        if (isLoading) {
            isLoading = false;
            loadingView.setVisibility(View.GONE);
        }
    }

    /**
     * Check if currently showing loading screen.
     */
    public boolean isLoading() {
        return isLoading;
    }
}
