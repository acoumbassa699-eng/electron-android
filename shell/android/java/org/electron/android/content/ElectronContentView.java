// Copyright 2024 Electron Android Project
// ElectronContentView - Vue de contenu Web pour Electron Android

package org.electron.android.content;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;

import org.electron.android.bridge.ElectronBridge;

/**
 * Vue de contenu WebView pour afficher le contenu Electron.
 * Utilise WebView avec les settings optimisés pour Electron.
 */
public class ElectronContentView extends WebView {

    private ElectronBridge bridge;
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;

    public interface NavigationCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onPageError(int errorCode, String description, String failingUrl);
        void onProgressChanged(int progress);
    }

    private NavigationCallback navigationCallback;

    public ElectronContentView(Context context) {
        super(context);
        init();
    }

    public ElectronContentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElectronContentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        configureSettings();
        setupClients();
        enableHardwareAcceleration();
    }

    private void configureSettings() {
        WebSettings settings = getSettings();

        // JavaScript (required for Electron)
        settings.setJavaScriptEnabled(true);
        
        // DOM storage
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        
        // Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);
        
        // Viewport
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        
        // Media
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // File access
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // Security
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        
        // User agent
        settings.setUserAgentString(settings.getUserAgentString() + 
            " Electron/" + getElectronVersion());
        
        // Layout
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
    }

    private void setupClients() {
        webViewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (navigationCallback != null) {
                    navigationCallback.onPageStarted(url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (navigationCallback != null) {
                    navigationCallback.onPageFinished(url);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame() && navigationCallback != null) {
                    navigationCallback.onPageError(
                        (int) error.getErrorCode(),
                        error.getDescription().toString(),
                        request.getUrl().toString()
                    );
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Handle custom protocols
                String scheme = request.getUrl().getScheme();
                if (scheme != null && !scheme.equals("http") && !scheme.equals("https")) {
                    // Handle custom scheme (electron://, etc.)
                    return true;
                }
                return false;
            }
        };

        webChromeClient = new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (navigationCallback != null) {
                    navigationCallback.onProgressChanged(newProgress);
                }
            }
        };

        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
    }

    private void enableHardwareAcceleration() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    public void setBridge(ElectronBridge bridge) {
        this.bridge = bridge;
    }

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    public void loadUrl(String url) {
        loadUrl(url, null);
    }

    @Override
    public void loadUrl(String url, java.util.Map<String, String> additionalHttpHeaders) {
        // Configure cookies
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true);
        
        super.loadUrl(url, additionalHttpHeaders);
    }

    /**
     * Enable/disable devtools for debugging.
     */
    public void setDevToolsEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(enabled);
        }
    }

    /**
     * Get the Electron version string.
     */
    private String getElectronVersion() {
        // This would be replaced with actual version from native code
        return "31.0.0";
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle hardware back button
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDetachedFromWindow() {
        // Clean up
        clearHistory();
        clearCache(true);
        super.onDetachedFromWindow();
    }
}
