// Copyright 2024 Electron Android Project
// Delegate pour les interactions Android

#ifndef ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_ANDROID_DELEGATE_H_
#define ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_ANDROID_DELEGATE_H_

#include "base/memory/raw_ptr.h"
#include "content/public/browser/android/browser_state.h"

namespace electron {

class ElectronBrowserContext;

namespace android {

// Delegate qui gère les interactions spécifiques Android
class ElectronAndroidDelegate {
 public:
  ElectronAndroidDelegate();
  virtual ~ElectronAndroidDelegate();

  // Called when the Android app is created
  virtual void OnAndroidAppCreate();

  // Called when the Android app is terminating
  virtual void OnAndroidAppTerminate();

  // Get the browser context for Electron
  content::BrowserState* GetBrowserState();

  // Permissions
  void RequestAndroidPermission(const std::string& permission);
  bool HasAndroidPermission(const std::string& permission);

  // Intent handling
  void HandleAndroidIntent(const std::string& action,
                          const std::string& uri);

  // File access
  void OpenAndroidFilePicker();
  void SaveAndroidFile(const std::string& path, const std::string& content);

  // Notifications
  void ShowAndroidNotification(const std::string& title,
                              const std::string& body,
                              const std::string& icon);
  void CancelAndroidNotification(const std::string& id);

  // Hardware back button
  void SetHardwareBackButtonEnabled(bool enabled);

 private:
  raw_ptr<content::BrowserState> browser_state_;
};

}  // namespace android
}  // namespace electron

#endif  // ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_ANDROID_DELEGATE_H_
