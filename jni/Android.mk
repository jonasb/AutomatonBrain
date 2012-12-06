LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := decoder-jni
LOCAL_CPPFLAGS := -D__STDC_CONSTANT_MACROS
LOCAL_C_INCLUDES := ffmpeg-android/build/ffmpeg/${TARGET_ARCH_ABI}/include
LOCAL_SRC_FILES := stamp.c decoder-jni.c
LOCAL_STATIC_LIBRARIES := 

LOCAL_LDLIBS += -Lffmpeg-android/build/ffmpeg/${TARGET_ARCH_ABI}/lib \
    -ljnigraphics \
    -lavformat \
    -lavcodec \
    -lavcore \
    -lavutil \
    -lswscale \
#

$(LOCAL_PATH)/stamp.c: $(LOCAL_PATH)/ffmpeg-android/build/ffmpeg/armeabi
	echo "Generate file"
	touch stamp.c

$(LOCAL_PATH)/ffmpeg-android/build/ffmpeg/${TARGET_ARCH_ABI}: $(LOCAL_PATH)/ffmpeg-android/ffmpeg
	echo ANDROIDNDK="$(NDK_ROOT)"
	cd ffmpeg-android && ANDROIDNDK="$(NDK_ROOT)" FFMPEG_ARCHS="armv7a" ./build-h264-aac.sh
	cd ffmpeg-android/build/ffmpeg/ && ln -s armeabi-v7a ${TARGET_ARCH_ABI}

$(LOCAL_PATH)/ffmpeg-android/ffmpeg: $(LOCAL_PATH)/ffmpeg-android
	cd ffmpeg-android && ./extract.sh

$(LOCAL_PATH)/ffmpeg-android:
	git clone git://github.com/tito/ffmpeg-android.git

include $(BUILD_SHARED_LIBRARY)

