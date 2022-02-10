LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := tests

LOCAL_MODULE := CtsAMSFeatureVerifyTestCases

LOCAL_JAVA_LIBRARIES := cts-tradefed tradefed compatibility-host-util

LOCAL_SRC_FILES := $(call all-java-files-under, src)

# tag this module as a cts test artifact
LOCAL_COMPATIBILITY_SUITE := cts arcts vts general-tests

include $(BUILD_CTS_HOST_JAVA_LIBRARY)

# Build the test APKs using their own makefiles
include $(call all-makefiles-under,$(LOCAL_PATH))
