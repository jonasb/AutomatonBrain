LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := decoder-jni
LOCAL_CPPFLAGS := -D__STDC_CONSTANT_MACROS
LOCAL_C_INCLUDES := ffmpeg-android/build/ffmpeg/${TARGET_ARCH_ABI}/include
LOCAL_SRC_FILES := decoder-jni.c
LOCAL_STATIC_LIBRARIES += \
    avcodec \
    avcore \
    avutil \
    swscale \
#
LOCAL_LDLIBS += \
    -ljnigraphics \
#
include $(BUILD_SHARED_LIBRARY)

include ffmpeg-android/Android.mk
