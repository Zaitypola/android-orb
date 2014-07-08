LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /home/zaity/downloads/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := camera_test
LOCAL_SRC_FILES := lsh.cpp
LOCAL_LDLIBS    += -lm -llog -landroid
LOCAL_STATIC_LIBRARIES += android_native_app_glue

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/native_app_glue)
