// Copyright 2024 Electron Android Project
// Preload script for Electron Android
// Exposes native Android APIs to renderer process

const { contextBridge, ipcRenderer } = require('electron');

// Device info cache
let deviceInfo = null;

// Get device info from native
function getDeviceInfo() {
  if (typeof window.Android !== 'undefined') {
    try {
      const info = JSON.parse(window.Android.getDeviceInfo());
      deviceInfo = info;
      return info;
    } catch (e) {
      console.error('Failed to get device info:', e);
    }
  }
  return deviceInfo || {
    platform: 'android',
    version: 'unknown'
  };
}

// Notification API
const Notification = {
  show: (title, body) => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.showNotification(title, body);
    }
    return null;
  },
  cancel: (id) => {
    if (typeof window.Android !== 'undefined') {
      window.Android.cancelNotification(id);
    }
  },
  cancelAll: () => {
    if (typeof window.Android !== 'undefined') {
      window.Android.cancelAllNotifications();
    }
  }
};

// File picker API
const FilePicker = {
  open: (options = {}) => {
    return new Promise((resolve, reject) => {
      if (typeof window.Android !== 'undefined') {
        window.Electron = window.Electron || {};
        window.Electron.onFilesSelected = (files) => {
          resolve(files);
          delete window.Electron.onFilesSelected;
        };
        window.Electron.onError = (error) => {
          reject(new Error(error));
          delete window.Electron.onError;
        };
        
        const mimeTypes = options.accept || ['*/*'];
        window.Android.openFilePicker(JSON.stringify(mimeTypes));
      } else {
        reject(new Error('File picker not available'));
      }
    });
  },
  
  save: (filename, data) => {
    return new Promise((resolve, reject) => {
      if (typeof window.Android !== 'undefined') {
        window.Electron = window.Electron || {};
        window.Electron.onFileSaved = (path) => {
          resolve(path);
          delete window.Electron.onFileSaved;
        };
        window.Electron.onError = (error) => {
          reject(new Error(error));
          delete window.Electron.onError;
        };
        
        const base64 = btoa(String.fromCharCode.apply(null, new Uint8Array(data)));
        window.Android.saveFile(filename, 'application/octet-stream', base64);
      } else {
        reject(new Error('File saver not available'));
      }
    });
  }
};

// Keyboard API
const Keyboard = {
  show: () => {
    if (typeof window.Android !== 'undefined') {
      window.Android.showKeyboard();
    }
  },
  hide: () => {
    if (typeof window.Android !== 'undefined') {
      window.Android.hideKeyboard();
    }
  },
  toggle: () => {
    if (typeof window.Android !== 'undefined') {
      window.Android.toggleKeyboard();
    }
  }
};

// Clipboard API
const Clipboard = {
  read: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.getFromClipboard();
    }
    return '';
  },
  write: (text) => {
    if (typeof window.Android !== 'undefined') {
      window.Android.copyToClipboard(text);
    }
  }
};

// Permissions API
const Permissions = {
  has: (permission) => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.hasPermission(permission);
    }
    return false;
  },
  hasCamera: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.hasCameraPermission();
    }
    return false;
  },
  hasMicrophone: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.hasMicrophonePermission();
    }
    return false;
  },
  hasStorage: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.hasStoragePermission();
    }
    return false;
  },
  hasLocation: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.hasLocationPermission();
    }
    return false;
  }
};

// Screen API
const Screen = {
  getWidth: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.getScreenWidth();
    }
    return window.innerWidth;
  },
  getHeight: () => {
    if (typeof window.Android !== 'undefined') {
      return window.Android.getScreenHeight();
    }
    return window.innerHeight;
  }
};

// Expose APIs to renderer
contextBridge.exposeInMainWorld('electron', {
  // Core
  platform: 'android',
  version: process.versions.electron,
  
  // APIs
  device: getDeviceInfo(),
  notification: Notification,
  filePicker: FilePicker,
  keyboard: Keyboard,
  clipboard: Clipboard,
  permissions: Permissions,
  screen: Screen,
  
  // IPC (for future use)
  ipcRenderer: {
    send: (channel, ...args) => ipcRenderer.send(channel, ...args),
    on: (channel, callback) => ipcRenderer.on(channel, (event, ...args) => callback(...args)),
    once: (channel, callback) => ipcRenderer.once(channel, (event, ...args) => callback(...args)),
    removeListener: (channel, callback) => ipcRenderer.removeListener(channel, callback)
  }
});

// Expose Android namespace for direct access
contextBridge.exposeInMainWorld('Android', {
  getDeviceInfo: () => {
    if (deviceInfo) return JSON.stringify(deviceInfo);
    return '{}';
  },
  getScreenWidth: () => window.innerWidth,
  getScreenHeight: () => window.innerHeight,
  getPlatform: () => 'android',
  getVersion: () => process.versions.electron,
  showNotification: (title, body) => Notification.show(title, body),
  cancelNotification: (id) => Notification.cancel(id),
  cancelAllNotifications: () => Notification.cancelAll(),
  showKeyboard: () => Keyboard.show(),
  hideKeyboard: () => Keyboard.hide(),
  toggleKeyboard: () => Keyboard.toggle(),
  copyToClipboard: (text) => Clipboard.write(text),
  getFromClipboard: () => Clipboard.read(),
  hasPermission: (p) => Permissions.has(p),
  hasCameraPermission: () => Permissions.hasCamera(),
  hasMicrophonePermission: () => Permissions.hasMicrophone(),
  hasStoragePermission: () => Permissions.hasStorage(),
  hasLocationPermission: () => Permissions.hasLocation(),
  openFilePicker: (mimeTypes) => FilePicker.open({ accept: JSON.parse(mimeTypes) }),
  saveFile: (name, type, data) => FilePicker.save(name, Uint8Array.from(atob(data), c => c.charCodeAt(0)))
});

console.log('Electron Android preload script loaded');
