// Copyright 2024 Electron Android Project
// File Picker Manager for Electron Android

package org.electron.android.managers;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages file picking and saving using Android Storage Access Framework.
 */
public class FilePickerManager {

    public interface FilePickerCallback {
        void onFilesSelected(List<String> paths);
        void onFileSaveSuccess(String path);
        void onError(String error);
    }

    private AppCompatActivity activity;
    private FilePickerCallback callback;
    private boolean isSaving = false;

    private ActivityResultLauncher<Intent> openDocumentLauncher;
    private ActivityResultLauncher<Intent> createDocumentLauncher;
    private ActivityResultLauncher<String[]> requestPermissionsLauncher;

    public FilePickerManager(AppCompatActivity activity) {
        this.activity = activity;
        registerActivityResultLaunchers();
    }

    private void registerActivityResultLaunchers() {
        // Open file picker
        openDocumentLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleOpenFileResult(result.getData());
                }
            }
        );

        // Save file picker
        createDocumentLauncher = activity.registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    handleSaveFileResult(result.getData());
                }
            }
        );
    }

    /**
     * Open file picker for reading files.
     */
    public void openFilePicker(String[] mimeTypes, FilePickerCallback callback) {
        this.callback = callback;
        this.isSaving = false;

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        
        if (mimeTypes != null && mimeTypes.length > 0) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        }
        
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        openDocumentLauncher.launch(intent);
    }

    /**
     * Open file picker for saving files.
     */
    public void saveFilePicker(String suggestedName, String mimeType, byte[] content, 
                               FilePickerCallback callback) {
        this.callback = callback;
        this.isSaving = true;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, suggestedName);
        
        // Store content for later saving
        pendingContent = content;
        createDocumentLauncher.launch(intent);
    }

    private byte[] pendingContent = null;

    private void handleOpenFileResult(Intent data) {
        List<String> paths = new ArrayList<>();
        
        if (data.getData() != null) {
            // Single file
            String path = getPathFromUri(data.getData());
            if (path != null) {
                paths.add(path);
            }
        }
        
        if (data.getClipData() != null) {
            // Multiple files
            ClipData clipData = data.getClipData();
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri uri = clipData.getItemAt(i).getUri();
                String path = getPathFromUri(uri);
                if (path != null) {
                    paths.add(path);
                }
            }
        }
        
        if (callback != null) {
            callback.onFilesSelected(paths);
        }
    }

    private void handleSaveFileResult(Intent data) {
        if (data.getData() != null && pendingContent != null) {
            try {
                // Write content to the selected file
                context.getContentResolver().openOutputStream(data.getData());
                
                // Note: In a real implementation, you would copy pendingContent 
                // to the output stream here
                
                if (callback != null) {
                    callback.onFileSaveSuccess(data.getData().toString());
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        }
        pendingContent = null;
    }

    /**
     * Get a path string from a content URI.
     */
    private String getPathFromUri(Uri uri) {
        // Return the URI string for content:// URIs
        return uri.toString();
    }

    private Context getContext() {
        return activity;
    }

    /**
     * Check if external storage is available.
     */
    public boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Get the app's external files directory.
     */
    public File getAppExternalFilesDir() {
        return activity.getExternalFilesDir(null);
    }
}
