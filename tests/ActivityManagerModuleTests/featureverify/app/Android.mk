LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

# Build the test APKs using their own makefiles
include $(call all-makefiles-under,$(LOCAL_PATH))
