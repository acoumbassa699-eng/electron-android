// Copyright 2024 Electron Android Project
// JavaScript Interface for WebView communication

package org.electron.android.module;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.electron.android.managers.DeviceInfo;
import org.electron.android.managers.FilePickerManager;
import org.electron.android.managers.KeyboardManager;
import org.electron.android.managers.NotificationManager;
import org.electron.android.managers.PermissionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * JavaScript interface for Electron Android.
 * Allows web content to access native Android features.
 */
public class ElectronJSInterface {

    private WebView webView;
    private Context context;
    private Handler mainHandler;
    
    // Managers
    private NotificationManager notificationManager;
    private FilePickerManager filePickerManager;
    private PermissionManager permissionManager;
    private KeyboardManager keyboardManager;
    private DeviceInfo deviceInfo;

    public ElectronJSInterface(WebView webView, Context context) {
        this.webView = webView;
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize managers
        this.notificationManager = new NotificationManager(context);
        this.filePickerManager = new FilePickerManager((androidx.appcompat.app.AppCompatActivity) context);
        this.permissionManager = new PermissionManager((androidx.appcompat.app.AppCompatActivity) context);
        this.keyboardManager = new KeyboardManager((androidx.appcompat.app.AppCompatActivity) context);
        this.deviceInfo = new DeviceInfo(context);
    }

    // ============= Device Info =============

    @JavascriptInterface
    public String getDeviceInfo() {
        Map<String, Object> info = deviceInfo.getDeviceInfo();
        return mapToJson(info);
    }

    @JavascriptInterface
    public int getScreenWidth() {
        return deviceInfo.getScreenSize()[0];
    }

    @JavascriptInterface
    public int getScreenHeight() {
        return deviceInfo.getScreenSize()[1];
    }

    @JavascriptInterface
    public String getPlatform() {
        return "android";
    }

    @JavascriptInterface
    public String getVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    // ============= Notifications =============

    @JavascriptInterface
    public String showNotification(String title, String body) {
        return notificationManager.showNotification(title, body, null);
    }

    @JavascriptInterface
    public void cancelNotification(String id) {
        notificationManager.cancelNotification(id);
    }

    @JavascriptInterface
    public void cancelAllNotifications() {
        notificationManager.cancelAllNotifications();
    }

    // ============= Keyboard =============

    @JavascriptInterface
    public void showKeyboard() {
        mainHandler.post(() -> keyboardManager.showKeyboard(webView));
    }

    @JavascriptInterface
    public void hideKeyboard() {
        mainHandler.post(() -> keyboardManager.hideKeyboard());
    }

    @JavascriptInterface
    public void toggleKeyboard() {
        mainHandler.post(() -> keyboardManager.toggleKeyboard());
    }

    // ============= Permissions =============

    @JavascriptInterface
    public boolean hasPermission(String permission) {
        return permissionManager.hasPermission(permission);
    }

    @JavascriptInterface
    public boolean hasCameraPermission() {
        return permissionManager.hasCameraPermission();
    }

    @JavascriptInterface
    public boolean hasMicrophonePermission() {
        return permissionManager.hasMicrophonePermission();
    }

    @JavascriptInterface
    public boolean hasStoragePermission() {
        return permissionManager.hasStoragePermission();
    }

    @JavascriptInterface
    public boolean hasLocationPermission() {
        return permissionManager.hasLocationPermission();
    }

    // ============= File System =============

    @JavascriptInterface
    public void openFilePicker(String mimeTypesJson) {
        String[] types = parseMimeTypes(mimeTypesJson);
        mainHandler.post(() -> {
            filePickerManager.openFilePicker(types, new FilePickerManager.FilePickerCallback() {
                @Override
                public void onFilesSelected(java.util.List<String> paths) {
                    evaluateJs("window.Electron.onFilesSelected(" + arrayToJson(paths) + ")");
                }

                @Override
                public void onFileSaveSuccess(String path) {
                    evaluateJs("window.Electron.onFileSaved('" + path + "')");
                }

                @Override
                public void onError(String error) {
                    evaluateJs("window.Electron.onError('" + error + "')");
                }
            });
        });
    }

    @JavascriptInterface
    public void saveFile(String filename, String mimeType, String dataBase64) {
        byte[] data = android.util.Base64.decode(dataBase64, android.util.Base64.DEFAULT);
        mainHandler.post(() -> {
            filePickerManager.saveFilePicker(filename, mimeType, data, new FilePickerManager.FilePickerCallback() {
                @Override
                public void onFilesSelected(java.util.List<String> paths) {}

                @Override
                public void onFileSaveSuccess(String path) {
                    evaluateJs("window.Electron.onFileSaved('" + path + "')");
                }

                @Override
                public void onError(String error) {
                    evaluateJs("window.Electron.onError('" + error + "')");
                }
            });
        });
    }

    // ============= Clipboard =============

    @JavascriptInterface
    public void copyToClipboard(String text) {
        android.content.ClipData clip = android.content.ClipData.newPlainText("Electron", text);
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    @JavascriptInterface
    public String getFromClipboard() {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            android.content.ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                return clip.getItemAt(0).getText().toString();
            }
        }
        return "";
    }

    // ============= Utilities =============

    private void evaluateJs(String script) {
        mainHandler.post(() -> {
            if (webView != null) {
                webView.evaluateJavascript(script, null);
            }
        });
    }

    private String mapToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    private String[] parseMimeTypes(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        // Simple parsing - in production use a JSON library
        return json.replace("[", "").replace("]", "").replace("\"", "").split(",");
    }

    private String arrayToJson(java.util.List<String> list) {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (String item : list) {
            if (!first) json.append(",");
            json.append("\"").append(item.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"");
            first = false;
        }
        json.append("]");
        return json.toString();
    }
}
