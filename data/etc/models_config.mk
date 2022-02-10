LOCAL_PATH:= vendor/sprd/platform/frameworks/base/data/etc

# must include the Android.mk in the board config file
#$(call inherit-product, vendor/sprd/platform/frameworks/base/data/etc/models_config.mk)

#copy the model files to system/etc
ifneq ($(filter 1 2, $(strip $(TARGET_BOARD_GALLERY_DISCOVER_SUPPORT))),)
model_files := $(shell ls $(LOCAL_PATH)/models/$(strip $(TARGET_BOARD_GALLERY_DISCOVER_SUPPORT)) )
PRODUCT_COPY_FILES += $(foreach file, $(model_files), \
        $(LOCAL_PATH)/models/$(strip $(TARGET_BOARD_GALLERY_DISCOVER_SUPPORT))/$(file):system/etc/models/$(file))
smarterase_model_files := $(shell ls $(LOCAL_PATH)/smarterase_models)
PRODUCT_COPY_FILES += $(foreach file, $(smarterase_model_files), \
        $(LOCAL_PATH)/smarterase_models/$(file):system/etc/models/$(file))
endif
