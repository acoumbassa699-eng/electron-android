// Copyright 2024 Electron Android Project
// ElectronContentView - Vue de contenu Web pour Electron Android avec Monaco Editor

package org.electron.android.content;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.electron.android.bridge.ElectronBridge;
import org.electron.android.module.ElectronJSInterface;

/**
 * Vue de contenu WebView pour afficher le contenu Electron avec Monaco Editor.
 * Utilise WebView avec les settings optimisés pour VS Code/Monaco.
 */
public class ElectronContentView extends WebView {

    private ElectronBridge bridge;
    private ElectronJSInterface jsInterface;
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;
    private boolean monacoLoaded = false;
    private String initialContent = "";

    public interface NavigationCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
        void onPageError(int errorCode, String description, String failingUrl);
        void onProgressChanged(int progress);
        void onMonacoLoaded();
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

    @SuppressLint("SetJavaScriptEnabled")
    private void init() {
        configureSettings();
        setupClients();
        enableHardwareAcceleration();
        
        // Add JavaScript interface for VS Code API
        jsInterface = new ElectronJSInterface(this, getContext());
        addJavascriptInterface(jsInterface, "Android");
        
        // Add VS Code API interface
        addJavascriptInterface(new VSCodeAPIInterface(), "vscode");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureSettings() {
        WebSettings settings = getSettings();

        // JavaScript (required for Monaco/VS Code)
        settings.setJavaScriptEnabled(true);
        
        // DOM storage (required for Monaco)
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        
        // Cache
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAppCacheEnabled(true);
        
        // Viewport - critical for Monaco
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        
        // Media
        settings.setMediaPlaybackRequiresUserGesture(false);
        
        // File access for local resources
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        
        // Security
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // User agent for VS Code compatibility
        settings.setUserAgentString(getVSCodeUserAgent());
        
        // Layout
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        
        // Text zoom for accessibility
        settings.setTextZoom(100);
    }

    private void setupClients() {
        webViewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (navigationCallback != null) {
                    navigationCallback.onPageStarted(url);
                }
                
                // Inject Monaco initialization when page loads
                if ("about:blank".equals(url)) {
                    loadMonacoEditor();
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
                String scheme = request.getUrl().getScheme();
                if (scheme != null && !scheme.equals("http") && !scheme.equals("https")) {
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

    private String getVSCodeUserAgent() {
        return "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36 VSCode/1.85.0";
    }

    public void setBridge(ElectronBridge bridge) {
        this.bridge = bridge;
    }

    public void setNavigationCallback(NavigationCallback callback) {
        this.navigationCallback = callback;
    }

    /**
     * Load Monaco Editor with VS Code workbench.
     */
    public void loadMonacoEditor() {
        if (monacoLoaded) return;
        
        String html = getMonacoHTML();
        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
        monacoLoaded = true;
    }

    private String getMonacoHTML() {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">\n" +
            "<title>VS Code</title>\n" +
            "<style>\n" +
            "html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; }\n" +
            "#container { width: 100%; height: 100%; }\n" +
            "</style>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div id=\"container\"></div>\n" +
            "<script>\n" +
            "// Monaco Editor Loader\n" +
            "var vscode = window.vscode || {};\n" +
            "var Android = window.Android || {};\n" +
            "\n" +
            "// Initialize Monaco when loaded\n" +
            "function initMonaco() {\n" +
            "    console.log('Monaco initializing...');\n" +
            "    require.config({ paths: { vs: 'file:///android_asset/lib/vs' }});\n" +
            "    require(['vs/editor/editor.main'], function() {\n" +
            "        console.log('Monaco loaded successfully');\n" +
            "        " + (initialContent.isEmpty() ? "" : "createEditor('" + initialContent.replace("'", "\\'") + "');") + "\n" +
            "        if (window.vscodeAPI) {\n" +
            "            window.vscodeAPI = {\n" +
            "                postMessage: function(msg) {\n" +
            "                    if (window.vscode && window.vscode.postMessage) {\n" +
            "                        window.vscode.postMessage(msg);\n" +
            "                    }\n" +
            "                }\n" +
            "            };\n" +
            "        }\n" +
            "    });\n" +
            "}\n" +
            "\n" +
            "function createEditor(content) {\n" +
            "    monaco.editor.create(document.getElementById('container'), {\n" +
            "        value: content || '// Welcome to VS Code for Android\\n',\n" +
            "        language: 'javascript',\n" +
            "        theme: 'vs-dark',\n" +
            "        automaticLayout: true,\n" +
            "        minimap: { enabled: true },\n" +
            "        fontSize: 14,\n" +
            "        lineNumbers: 'on',\n" +
            "        roundedSelection: true,\n" +
            "        scrollBeyondLastLine: false,\n" +
            "        wordWrap: 'on'\n" +
            "    });\n" +
            "}\n" +
            "\n" +
            "// Android API bridge\n" +
            "window.Android = {\n" +
            "    getDeviceInfo: function() { return JSON.stringify({ platform: 'android' }); },\n" +
            "    getScreenWidth: function() { return window.innerWidth; },\n" +
            "    getScreenHeight: function() { return window.innerHeight; },\n" +
            "    getPlatform: function() { return 'android'; },\n" +
            "    getVersion: function() { return '1.0.0'; },\n" +
            "    showNotification: function(title, body) { console.log('Notification:', title, body); },\n" +
            "    showKeyboard: function() { },\n" +
            "    hideKeyboard: function() { },\n" +
            "    copyToClipboard: function(text) { },\n" +
            "    getFromClipboard: function() { return ''; },\n" +
            "    hasPermission: function(p) { return true; }\n" +
            "};\n" +
            "\n" +
            "// Load Monaco loader script\n" +
            "var script = document.createElement('script');\n" +
            "script.src = 'file:///android_asset/lib/vs/loader.js';\n" +
            "script.onload = initMonaco;\n" +
            "document.head.appendChild(script);\n" +
            "</script>\n" +
            "</body>\n" +
            "</html>";
    }

    /**
     * Set initial content to load in editor.
     */
    public void setInitialContent(String content) {
        this.initialContent = content != null ? content : "";
    }

    /**
     * Load content into the editor.
     */
    public void loadContent(String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(
                "if (window.editor) { window.editor.setValue(" + 
                "JSON.stringify(" + content.replace("\\", "\\\\").replace("'", "\\'") + ")); }",
                null
            );
        }
    }

    /**
     * Get content from the editor.
     */
    public void getContent(ValueCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(
                "JSON.stringify(window.editor ? window.editor.getValue() : '')",
                callback
            );
        } else {
            callback.onReceiveValue("");
        }
    }

    /**
     * Execute command in editor.
     */
    public void executeCommand(String command) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(command, null);
        }
    }

    /**
     * Enable/disable devtools for debugging.
     */
    public void setDevToolsEnabled(boolean enabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(enabled);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDetachedFromWindow() {
        clearHistory();
        clearCache(true);
        super.onDetachedFromWindow();
    }

    /**
     * VS Code API interface for Android.
     */
    public class VSCodeAPIInterface {
        @JavascriptInterface
        public void postMessage(String message) {
            // Handle messages from VS Code
            if (bridge != null) {
                bridge.onVSCodeMessage(message);
            }
        }

        @JavascriptInterface
        public String getState() {
            return "{}";
        }

        @JavascriptInterface
        public void setState(String state) {
            // Store state
        }
    }
}
