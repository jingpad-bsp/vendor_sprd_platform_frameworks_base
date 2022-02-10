LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := CtsAMSFeatureAppsTests

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE_PATH := $(TARGET_OUT_DATA_APPS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := junit android.test.base.stubs

LOCAL_STATIC_JAVA_LIBRARIES = \
    android-support-test \
	compatibility-device-util
	
#in order to schedule the hide interface
LOCAL_PRIVATE_PLATFORM_APIS := true

# tag this module as a cts test artifact
LOCAL_COMPATIBILITY_SUITE := cts vts general-tests

include $(BUILD_CTS_PACKAGE)
