// Copyright 2024 Electron Android Project
// JNI Bridge pour la communication Java/C++ avec Android

#ifndef ELECTRON_SHELL_ANDROID_ELECTRON_ANDROID_JNI_BRIDGE_H_
#define ELECTRON_SHELL_ANDROID_ELECTRON_ANDROID_JNI_BRIDGE_H_

#include <jni.h>
#include <string>

#include "base/android/jni_generator/jni_generator_helper.h"
#include "base/android/library_loader/library_loader_hooks.h"
#include "base/android/scoped_java_ref.h"
#include "content/public/app/content_jni_onload.h"
#include "content/public/browser/android/browser_state.h"
#include "shell/browser/android/electron_android_delegate.h"

namespace electron {
namespace android {

// Initialise le JNI pour Electron Android
bool InitElectronAndroidJni(JNIEnv* env);

// Crée une fenêtre Android native
class ElectronAndroidWindow {
 public:
  ElectronAndroidWindow();
  ~ElectronAndroidWindow();

  // Lifecycle
  void OnCreate();
  void OnResume();
  void OnPause();
  void OnDestroy();

  // View management
  void SetContentView(jobject view);
  jobject GetContentView();

  // Touch input handling
  void OnTouchEvent(jint action, jfloat x, jfloat y);
  void OnMultiTouchEvent(jint action, jobjectArray touches);

  // Window callbacks
  void OnBackPressed();
  void OnConfigurationChanged(jint newConfig);

 private:
  jobject java_window_;
  base::android::ScopedJavaGlobalRef<jobject> java_ref_;
};

// Electron Android Application
class ElectronAndroidApp {
 public:
  ElectronAndroidApp();
  ~ElectronAndroidApp();

  static ElectronAndroidApp* GetInstance();

  // Application lifecycle
  void OnCreate();
  void OnTerminate();

  // Get the delegate
  ElectronAndroidDelegate* GetDelegate();

 private:
  static ElectronAndroidApp* instance_;
  std::unique_ptr<ElectronAndroidDelegate> delegate_;
};

}  // namespace android
}  // namespace electron

#endif  // ELECTRON_SHELL_ANDROID_ELECTRON_ANDROID_JNI_BRIDGE_H_
