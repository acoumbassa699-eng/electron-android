// Copyright 2024 Electron Android Project
// Vue de fenêtre native pour Android

#ifndef ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_NATIVE_WINDOW_VIEW_ANDROID_H_
#define ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_NATIVE_WINDOW_VIEW_ANDROID_H_

#include <jni.h>

#include "base/android/scoped_java_ref.h"
#include "base/memory/raw_ptr.h"
#include "ui/android/window_android.h"
#include "ui/gfx/geometry/rect.h"
#include "ui/views/widget/widget.h"

namespace electron {

class ElectronNativeWindowAndroid : public views::Widget,
                                   public ui::WindowAndroid::Observer {
 public:
  explicit ElectronNativeWindowAndroid(
      gfx::NativeWindow native_window,
      const gfx::Rect& bounds);
  ~ElectronNativeWindowAndroid() override;

  // Widget overrides
  void OnSizeChanged(bool minimized) override;

  // WindowAndroid::Observer overrides
  void OnWindowAndroidDestroyed(ui::WindowAndroid* window) override;

  // Android-specific methods
  void SetStatusBarColor(SkColor color);
  void SetNavigationBarColor(SkColor color);
  void SetImmersiveMode(bool enabled);
  void SetKeepScreenOn(bool on);

  // Orientation
  void SetOrientation(int rotation);
  void LockOrientation(const std::vector<int>& orientations);
  void UnlockOrientation();

  // Soft keyboard
  void ShowSoftKeyboard();
  void HideSoftKeyboard();
  void SetKeyboardResizeMode(bool resize);

  // Accessibility
  void SetAccessibilityEnabled(bool enabled);

  // Java callbacks
  void OnBackPressed(JNIEnv* env, jobject obj);
  void OnConfigurationChanged(JNIEnv* env, jobject obj, jint newConfig);

 private:
  raw_ptr<ui::WindowAndroid> window_android_;
  base::android::ScopedJavaGlobalRef<jobject> java_window_;
  gfx::Rect bounds_;
  bool is_fullscreen_;
};

}  // namespace electron

#endif  // ELECTRON_SHELL_BROWSER_ANDROID_ELECTRON_NATIVE_WINDOW_VIEW_ANDROID_H_
