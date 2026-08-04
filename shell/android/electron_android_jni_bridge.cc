// Copyright 2024 Electron Android Project
// JNI Bridge implementation

#include "shell/android/electron_android_jni_bridge.h"

#include <memory>

#include "base/android/jni_android.h"
#include "base/logging.h"
#include "content/public/browser/android/browser_state.h"

namespace electron {

namespace android {

namespace {

// Clés pour le registre de JNI
const char kElectronBridgeClassName[] =
    "org/electron/android/bridge/ElectronBridge";

}  // anonymous namespace

bool InitElectronAndroidJni(JNIEnv* env) {
  // Register JNI methods
  return true;
}

// Implémentation de ElectronAndroidWindow
ElectronAndroidWindow::ElectronAndroidWindow() : java_window_(nullptr) {}

ElectronAndroidWindow::~ElectronAndroidWindow() = default;

void ElectronAndroidWindow::OnCreate() {
  // Called when window is created
}

void ElectronAndroidWindow::OnResume() {
  // Called when window resumes
}

void ElectronAndroidWindow::OnPause() {
  // Called when window pauses
}

void ElectronAndroidWindow::OnDestroy() {
  // Called when window is destroyed
}

void ElectronAndroidWindow::SetContentView(jobject view) {
  java_window_ = view;
}

jobject ElectronAndroidWindow::GetContentView() {
  return java_window_;
}

void ElectronAndroidWindow::OnTouchEvent(jint action, jfloat x, jfloat y) {
  // Handle touch event
}

void ElectronAndroidWindow::OnMultiTouchEvent(jint action, jobjectArray touches) {
  // Handle multi-touch event
}

void ElectronAndroidWindow::OnBackPressed() {
  // Handle back button
}

void ElectronAndroidWindow::OnConfigurationChanged(jint newConfig) {
  // Handle configuration change
}

// Implémentation de ElectronAndroidApp
ElectronAndroidApp* ElectronAndroidApp::instance_ = nullptr;

ElectronAndroidApp::ElectronAndroidApp()
    : delegate_(std::make_unique<ElectronAndroidDelegate>()) {
  instance_ = this;
}

ElectronAndroidApp::~ElectronAndroidApp() {
  instance_ = nullptr;
}

ElectronAndroidApp* ElectronAndroidApp::GetInstance() {
  return instance_;
}

void ElectronAndroidApp::OnCreate() {
  delegate_->OnAndroidAppCreate();
}

void ElectronAndroidApp::OnTerminate() {
  delegate_->OnAndroidAppTerminate();
}

ElectronAndroidDelegate* ElectronAndroidApp::GetDelegate() {
  return delegate_.get();
}

}  // namespace android
}  // namespace electron
